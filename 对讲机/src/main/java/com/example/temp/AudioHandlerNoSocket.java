package com.example.temp;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;

import com.ryong21.encode.Speex;

import java.util.Arrays;

//=========================TCP语音传输模块==================================================================
// 基于Tcp语音传输模块
public class AudioHandlerNoSocket extends Thread {
	private Handler handler;
	// private static final int AUDIO_PORT = 4454;
	private static final int HZ = 48000;
	// private static final int HZ = 44100;
	public boolean isStopTalk = false;
	public boolean isStopSend = false;
	private int totalSend = 0;
	private int totalReceive = 0;
	private int flag;
	private int audio_source;
	private Speex speex = new Speex();

	public AudioHandlerNoSocket(Integer audio_source, Handler handler, int flag) {
		this.audio_source = audio_source;
		this.handler = handler;
		this.flag = flag;
		init();
	}

	private void init() {
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
		int bufferSize = AudioRecord.getMinBufferSize(HZ,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		recorder = new AudioRecord(audio_source, HZ,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize);
		AudioTrack player = new AudioTrack(AudioManager.STREAM_MUSIC, HZ,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize,
				AudioTrack.MODE_STREAM);

		// 设置喇叭音量
		player.setStereoVolume(0.4f, 0.4f);
		// 开始播放声音
		player.play();
		// AudioFormat.ENCODING_PCM_16BIT, bufferSize * 10);
		sendMsg("成功获取麦克");
		recorder.startRecording();// 开始录音
		sendMsg("麦克准备好录音了");
		int length = 0;
		int count = 0;
		short[] audio = new short[160];// 音频读取缓存
		byte[] processedData = new byte[160];// 音频读取缓存
		int getSize = 0;

		while (!isStopSend) {
			// if ((count % 100) == 0) {
			// sendMsg("正在获取麦克的声音,第 " + count + " 次");
			// }
			count++;
			length = recorder.read(audio, 0, 160);// 从mic读取音频数据
			totalSend += length;
			getSize = speex.encode(audio, 0, processedData, length);
			Logger.d("encoded size: " + getSize);
			Arrays.fill(audio, (short) 0);
			getSize = speex.decode(processedData, audio, getSize);
			Logger.d("decoded size: " + getSize);
			player.write(audio, 0, getSize);// 播放音频数据
//			player.write(audio, 0, length);// 播放音频数据
			if ((count % 100) == 0) {
				sendMsg("输出：" + TextFormat.formatByte(totalSend, 3));
			}
		}
		player.release();
	}

	AudioRecord recorder = null;

	public void closeRecord() {
		System.out.println("closeRecord()");
		isStopSend = true;
		speex.close();
		if (null != recorder) {
			recorder.stop();
			recorder.release();
			recorder = null;
		}
	}

	public void release() {
		closeRecord();
	}
}
// =========================TCP语音传输模块结束==================================================================
