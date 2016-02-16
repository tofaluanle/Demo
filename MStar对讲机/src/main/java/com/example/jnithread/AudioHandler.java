package com.example.jnithread;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

//=========================TCP语音传输模块==================================================================
// 基于Tcp语音传输模块
public class AudioHandler {

    static {
        System.loadLibrary("jnithread");
        System.loadLibrary("tvqe");
    }

    jni mjni = new jni();
    AudioTrack audio = null;
    cn.thinkit.libtvqe.TVQEJNI tvqe = new cn.thinkit.libtvqe.TVQEJNI();

    private Handler handler;
    // private static final int AUDIO_PORT = 4454;
    private String target_ip;
    private int target_port;
    private int myPort;
    public boolean isStopTalk = false;
    public boolean isStopSend = false;
    private int totalSend = 0;
    private int totalReceive = 0;
    private int audio_source;
    private InetAddress target_address;

    public AudioHandler(Integer audio_source, Handler handler, String ip,
                        int port, int myPort) {
        this.audio_source = audio_source;
        this.handler = handler;
        target_ip = ip;
        target_port = port;
        this.myPort = myPort;
        try {
            target_address = InetAddress.getByName(target_ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void startReceive() {
        new AudioReceive().start();
    }

    public class AudioReceive extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                tvqe.TVqeInit(16000, 0, 1, 1, 1, "");
                int minBuffSize = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                audio = new AudioTrack(AudioManager.STREAM_MUSIC, 16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, minBuffSize, AudioTrack.MODE_STREAM);
                // 设置喇叭音量
                audio.setStereoVolume(1.0f, 1.0f);
                audio.play();
                // 开始播放声音
                byte[] audioBuffer = new byte[640];// 音频读取缓存
                short[] micsbuf = new short[160];
                short[] refsbuf = new short[160];
                short[] osbuf = new short[160];
                int length = 0;
                int count = 0;
                DatagramSocket socket = new DatagramSocket(myPort);

                while (!isStopTalk) {
                    count++;
                    DatagramPacket packet = new DatagramPacket(audioBuffer, audioBuffer.length);
                    socket.receive(packet);
                    length = packet.getData().length;// 从网络读取音频数据
                    totalReceive += length;
                    if (length > 0 && length % 2 == 0) {
                        for (int i = 0; i < 160; i++) {
                            micsbuf[i] = (short) ((audioBuffer[4 * i] & 0xff) | (audioBuffer[4 * i + 1] << 8));
                            refsbuf[i] = (short) ((audioBuffer[4 * i + 2] & 0xff) | (audioBuffer[4 * i + 3] << 8));
                        }
                        tvqe.TVqeProcess(micsbuf, refsbuf, osbuf, 0);

                        audio.write(osbuf, 0, 160);
                        if ((count % 50) == 0) {
                            sendMsg("收到了" + TextFormat.formatByte(totalReceive),
                                    1);
                        }
                    }
                }
                audio.stop();
                audio.release();
                tvqe.TVqeFree();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    // 用来启动音频发送子线程
    public void audioSend() {
        new AudioSend().start();
    }

    // 音频发送线程
    public class AudioSend extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                DatagramSocket socket = new DatagramSocket();
                DatagramPacket packet = null;
                sendMsg("准备获取录音", 2);
                byte[] bbuf = new byte[640];
                short[] micsbuf = new short[160];
                short[] refsbuf = new short[160];
                short[] osbuf = new short[160];
                int tryinit = 20;
                while ((tryinit-- >= 0) && (0 != mjni.init(1, 0))) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (tryinit <= 0) {
                    Log.e("java layer", "try 20times");
                    return;
                }
                sendMsg("成功获取麦克,麦克准备好录音了", 2);
                int count = 0;
                while (!isStopSend) {
                    if (0 != mjni.read640(bbuf)) {
                        count++;
                        packet = new DatagramPacket(bbuf, bbuf.length,
                                target_address, target_port);
                        socket.send(packet);
                        if ((count % 100) == 0) {
                            sendMsg("正在发送录音第" + count + "次", 2);
                        }
                    } else {
                        Log.e("java layer", "read 0");
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // int length = 0;
                // while (!isStopSend) {
                // // if ((count % 100) == 0) {
                // // sendMsg("正在获取麦克的声音,第 " + count + " 次");
                // // }
                // count++;
                // length = recorder.read(readBuffer, 0, 640);// 从mic读取音频数据
                // totalSend += length;
                //
                // int v = 0;
                // // 将 buffer 内容取出，进行平方和运算
                // for (int i = 0; i < readBuffer.length; i++) {
                // // 这里没有做运算的优化，为了更加清晰的展示代码
                // v += readBuffer[i] * readBuffer[i];
                // }
                //
                // // int value = (int) (Math.abs((int)(v
                // // /(float)length)/10000) >> 1);
                //
                // // if (length > 0 && length % 2 == 0) {
                // os.write(readBuffer, 0, length);// 写入到输出流，把音频数据通过网络发送给对方
                // if ((count % 100) == 0) {
                // sendMsg("输出：" + TextFormat.formatByte(totalSend)
                // + ", 音量：" + (v / length), 2);
                // // + ", 音量：" + value);
                // }
                // // }
                // }
                mjni.finish();
                socket.close();
                sendMsg("停止发送成功", 2);
            } catch (IOException e) {
                e.printStackTrace();
                sendMsg("发送连接失败", 2);
            }
        }
    }

    public void release() {
        System.out.println("closeRecord()");
        isStopSend = true;
        isStopTalk = true;
    }

}
// =========================TCP语音传输模块结束==================================================================
