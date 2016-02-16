package com.example.temp;

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.os.Handler;
import android.os.Message;

import com.ryong21.encode.Speex;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

//=========================TCP语音传输模块==================================================================
// 基于Tcp语音传输模块
@TargetApi(16)
public class AudioHandler extends Thread {
    private ServerSocket sSocket = null;
    private Handler handler;
    // private static final int AUDIO_PORT = 4454;
//    private static final int HZ = 8000;
    private static final int HZ = 44100;
    private static final int BUFFER_SIZE = 160;
    private String target_ip;
    private int target_port;
    private int myPort;
    public boolean isStopTalk = false;
    public boolean isStopSend = false;
    private int totalSend = 0;
    private int totalReceive = 0;
    private int flag;
    private int audio_source;
    private AcousticEchoCanceler canceler;
    private AudioTrack player;
    private Speex speex = new Speex();

    public AudioHandler(Integer audio_source, Handler handler, String ip, int port, int myPort, int flag) {
        this.audio_source = audio_source;
        this.handler = handler;
        target_ip = ip;
        target_port = port;
        this.myPort = myPort;
        this.flag = flag;
        init();
    }

    private void init() {
        // 获得录音缓冲区大小
        int bufferSize = AudioRecord.getMinBufferSize(HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

//        initAEC(recorder.getAudioSessionId());

        // 获得音频缓冲区大小
        bufferSize = android.media.AudioTrack.getMinBufferSize(HZ, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);

        // 获得音轨对象
        player = new AudioTrack(AudioManager.STREAM_MUSIC, HZ, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM, recorder.getAudioSessionId());

        speex.init();
        int frameSize = speex.getFrameSize();
        Logger.d("frameSize: " + frameSize);
    }

    @Override
    public void run() {
        super.run();
        try {
            sSocket = new ServerSocket(myPort);// 监听音频端口
            System.out.println("Audio Handler socket started ...");
            sendMsg("准备接受播放请求");
            while (!sSocket.isClosed() && null != sSocket) {
                sendMsg("正在接受播放请求");
                Socket socket = sSocket.accept();
                socket.setSoTimeout(5000);
                audioPlay(socket);
                sendMsg("接收到播放请求了");
            }
        } catch (IOException e) {
            e.printStackTrace();
            sendMsg("接收连接失败");
        }
    }

    private void sendMsg(String str) {
        Message msg = new Message();
        switch (flag) {
            case 1:
                msg.what = MainActivity.CHANGE_STATE_1;
                break;
            case 2:
                msg.what = MainActivity.CHANGE_STATE_2;
                break;
        }
        msg.obj = str;
        handler.sendMessage(msg);
    }

    private void sendMsg(String str, int flag) {
        Message msg = new Message();
        switch (flag) {
            case 1:
                msg.what = MainActivity.CHANGE_STATE_1;
                break;
            case 2:
                msg.what = MainActivity.CHANGE_STATE_2;
                break;
        }
        msg.obj = str;
        handler.sendMessage(msg);
    }

    // 用来启动音频播放子线程
    public void audioPlay(Socket socket) {
        new AudioPlay(socket).start();
    }

    // 用来启动音频发送子线程
    public void audioSend() {
        new AudioSend().start();
    }

    // 音频播线程
    public class AudioPlay extends Thread {
        Socket socket = null;

        public AudioPlay(Socket socket) {
            this.socket = socket;
            // android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        }

        @Override
        public void run() {
            try {
                InputStream is = socket.getInputStream();

                // 设置喇叭音量
                player.setStereoVolume(0.3f, 0.3f);
                // 开始播放声音
                player.play();
                byte[] audio = new byte[BUFFER_SIZE];// 音频读取缓存
                short[] rawData = new short[BUFFER_SIZE];// 音频读取缓存
                byte[] tmp = new byte[BUFFER_SIZE];// 音频读取缓存
                int length = 0;
                int count = 0;
                while (!isStopTalk) {
                    count++;
                    length = is.read(audio, 0, 152);// 从网络读取音频数据
                    Logger.d("recive size: " + length);
                    totalReceive += length;
//                    Arrays.fill(rawData, (short) 0);
                    int time = length / 38;
                    for (int i = 0; i < time; i++) {
                        System.arraycopy(audio, 0 + i * 38, tmp, 0, 38);
                        length = speex.decode(tmp, rawData, 38);
                        Logger.d("decoded size: " + length);
                        player.write(rawData, 0, length);
                    }
//                        player.write(audio, 0, length);// 播放音频数据
                    if ((count % 50) == 0) {
                        sendMsg("收到了" + TextFormat.formatByte(totalReceive, 3));
                    }
                }
                player.stop();
                is.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    AudioRecord recorder = null;

    // 音频发送线程
    public class AudioSend extends Thread {

        @Override
        public void run() {
            super.run();
            Socket socket = null;
            OutputStream os = null;

            try {
                socket = new Socket(target_ip, target_port);
                // socket.setSoTimeout(5000);
                os = socket.getOutputStream();
                sendMsg("开始写入输出流2");
                recorder.startRecording();// 开始录音
                sendMsg("成功获取麦克");
                short[] readBuffer = new short[BUFFER_SIZE];// 录音缓冲区
//                byte[] readBuffer = new byte[BUFFER_SIZE];// 录音缓冲区
                byte[] processedData = new byte[BUFFER_SIZE];// 录音缓冲区
                sendMsg("麦克准备好录音了");
                int length = 0;
                int count = 0;

                while (!isStopSend) {
                    count++;
                    length = recorder.read(readBuffer, 0, readBuffer.length);// 从mic读取音频数据
                    int v = 0;
//                    // 将 buffer 内容取出，进行平方和运算
//                    for (int i = 0; i < readBuffer.length; i++) {
//                        // 这里没有做运算的优化，为了更加清晰的展示代码
//                        v += readBuffer[i] * readBuffer[i];
//                    }
                    length = speex.encode(readBuffer, 0, processedData, length);
                    totalSend += length;

                    Logger.d("encoded size: " + length);
                    os.write(processedData, 0, length);
//                    os.write(readBuffer, 0, length);// 写入到输出流，把音频数据通过网络发送给对方
                    if ((count % 100) == 0) {
                        sendMsg("输出：" + TextFormat.formatByte(totalSend, 3) + ", 音量：" + (v / length), 2);
                    }
                }
                os.close();
                socket.close();
                sendMsg("停止发送成功");
            } catch (IOException e) {
                e.printStackTrace();
                sendMsg("发送连接失败");
            }
        }
    }


    public void release() {
        releaseAEC();
        closeRecord();
        try {
            System.out.println("Audio handler socket closed ...");
            if (null != sSocket)
                sSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeRecord() {
        System.out.println("closeRecord()");
        isStopSend = true;
        isStopTalk = true;
        if (speex != null) {
            speex.close();
        }
        if (null != recorder) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    public boolean initAEC(int audioSession) {
        System.out.println("AcousticEchoCanceler.isAvailable(): " + AcousticEchoCanceler.isAvailable());
        if (canceler != null) {
            return false;
        }
        canceler = AcousticEchoCanceler.create(audioSession);
        canceler.setEnabled(true);
        return canceler.getEnabled();
    }

    public boolean releaseAEC() {
        if (null == canceler) {
            return false;
        }
        canceler.setEnabled(false);
        canceler.release();
        return true;
    }
}
// =========================TCP语音传输模块结束==================================================================

