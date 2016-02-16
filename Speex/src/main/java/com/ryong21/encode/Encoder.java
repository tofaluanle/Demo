package com.ryong21.encode;

import android.media.AudioTrack;

import com.ryong21.Logger;

import java.util.Arrays;

public class Encoder implements Runnable {

    private volatile int leftSize = 0;
    private final Object mutex = new Object();
    private Speex speex = new Speex();
    private int frameSize;
    private long ts;
    private byte[] processedData = new byte[1024];
    private short[] rawdata = new short[1024];
    private volatile boolean isRecording;
    AudioTrack player;

    public Encoder() {
        super();
        speex.init();
        frameSize = speex.getFrameSize();
        Logger.d("frameSize: " + frameSize);

//        int bufferSize = android.media.AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
//        player = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
//        player.setStereoVolume(1f, 1f);
//        player.play();
    }

    public void run() {

        android.os.Process
                .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        int getSize = 0;
        while (this.isRecording()) {

            synchronized (mutex) {
                while (isIdle()) {
                    try {
                        mutex.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            synchronized (mutex) {
                getSize = speex.encode(rawdata, 0, processedData, leftSize);
//                player.write(rawdata, 0, getSize);
                //只打印编码结果，没有后续处理
                Logger.d(ts + " encoded size: " + getSize);
                Arrays.fill(rawdata, (short) 0);
                getSize = speex.decode(processedData, rawdata, getSize);
                setIdle();
            }
        }
//        player.release();
    }

    public void putData(long ts, short[] data, int size) {
        synchronized (mutex) {
            this.ts = ts;
            System.arraycopy(data, 0, rawdata, 0, size);
            this.leftSize = size;
            mutex.notify();
        }
    }

    public boolean isIdle() {
        return leftSize == 0 ? true : false;
    }

    public void setIdle() {
        leftSize = 0;
    }

    public void setRecording(boolean isRecording) {
        synchronized (mutex) {
            this.isRecording = isRecording;
            if (this.isRecording) {
                mutex.notify();
            }
        }
    }

    public boolean isRecording() {
        synchronized (mutex) {
            return isRecording;
        }
    }
}
