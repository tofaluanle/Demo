package com.example.temp;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;

import com.ryong21.encode.Speex;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Arrays;

//=========================TCP语音传输模块==================================================================
// 基于Tcp语音传输模块
public class AudioHandlerUDP extends Thread {

    private ServerSocket sSocket = null;
    private Handler handler;
    // private static final int AUDIO_PORT = 4454;
//    private static final int HZ = 8000;
        private static final int HZ = 44100;
    private String target_ip;
    private int target_port;
    private int myPort;
    public boolean isStopTalk = false;
    public boolean isStopSend = false;
    private int totalSend = 0;
    private int totalReceive = 0;
    private int flag;
    private int audio_source;
    AudioRecord recorder = null;
    private int minBufferSize;
    private AudioTrack player;
    private int minTrackbufferSize;
    private Speex speex = new Speex();

    public AudioHandlerUDP(Integer audio_source, Handler handler, String ip, int port, int myPort, int flag) {
        this.audio_source = audio_source;
        this.handler = handler;
        target_ip = ip;
        target_port = port;
        this.myPort = myPort;
        this.flag = flag;
        init();
    }

    private void init() {
        minBufferSize = AudioRecord.getMinBufferSize(HZ,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        minTrackbufferSize = android.media.AudioTrack.getMinBufferSize(HZ, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
        Logger.d("minBufferSize: " + minBufferSize + ", minTrackbufferSize: " + minTrackbufferSize);

        minBufferSize = 320;
        minTrackbufferSize = 320;
        player = createAudioTrack();

        speex.init();
        int frameSize = speex.getFrameSize();
        Logger.d("frameSize: " + frameSize);
    }

    @Override
    public void run() {
        audioPlay();
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

    // 用来启动音频播放子线程
    public void audioPlay() {
        new Thread(new AudioReceive()).start();
    }

    // 用来启动音频发送子线程
    public void audioSend() {
        new Thread(new AudioRecordRunnable()).start();
    }

    public class AudioReceive implements Runnable {

        @Override
        public void run() {
            try {
                byte[] audioRec = new byte[minTrackbufferSize];
                short[] rawData = new short[minTrackbufferSize];
                DatagramSocket socket = new DatagramSocket(myPort);
                DatagramPacket packet = new DatagramPacket(audioRec, audioRec.length);
                int count = 0;
                int getSize = 0;
                while (!isStopTalk) {
                    socket.receive(packet);
                    audioRec = packet.getData();
                    int recLength = packet.getData().length;
                    System.out.println("receive: " + recLength);
                    Arrays.fill(rawData, (short) 0);
                    getSize = speex.decode(audioRec, rawData, recLength);
                    Logger.d("decoded size: " + getSize);
                    player.write(rawData, 0, getSize);
//                    player.write(audioRec, 0, recLength);
                    totalReceive += recLength;
                    System.out.println("play: " + recLength);
//					new AudioPlay(audioRec, recLength).start();
                    count++;
                    if ((count % 10) == 0) {
                        sendMsg("收到了" + TextFormat.formatByte(totalReceive, 3));
                    }
                }
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private AudioTrack createAudioTrack() {
        AudioTrack player = new AudioTrack(AudioManager.STREAM_MUSIC, HZ, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, minTrackbufferSize, AudioTrack.MODE_STREAM);
        player.setStereoVolume(0.3f, 0.3f);
        player.play();
        return player;
    }

    public class AudioRecordRunnable implements Runnable {

        @Override
        public void run() {
            recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
            Logger.d("AudioRecord state2: " + recorder.getState());
            sendMsg("成功获取麦克");
            recorder.startRecording();// 开始录音
            sendMsg("麦克准备好录音了");
            byte[] processedData = new byte[minBufferSize];
//            byte[] audioSend = new byte[minBufferSize];
            short[] audioSend = new short[minBufferSize];
            int count = 0;
            int getSize = 0;
            try {
                DatagramSocket socket = new DatagramSocket();
                InetAddress target_address = InetAddress.getByName(target_ip);
//                DatagramPacket packet = new DatagramPacket(audioSend, audioSend.length, target_address, target_port);
                DatagramPacket packet = new DatagramPacket(processedData, audioSend.length, target_address, target_port);
                while (!isStopSend) {
                    try {
                        int sendLength = recorder.read(audioSend, 0, audioSend.length);
                        totalSend += sendLength;
                        System.out.println("record: " + sendLength);
                        getSize = speex.encode(audioSend, 0, processedData, sendLength);
                        Logger.d("encoded size: " + getSize);
                        packet.setData(processedData, 0, getSize);
//                        packet.setData(audioSend, 0, sendLength);
                        socket.send(packet);
                        System.out.println("send: " + sendLength);
                        count++;
                        if ((count % 10) == 0) {
                            sendMsg("输出：" + TextFormat.formatByte(totalSend, 3) + ", 单次：" + sendLength);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            recorder.stop();
            recorder.release();
        }

    }

    public void release() {
        closeRecord();
        player.stop();
        player.release();
    }

    public void closeRecord() {
        System.out.println("closeRecord()");
        isStopSend = true;
        speex.close();
    }
}
// =========================TCP语音传输模块结束==================================================================
