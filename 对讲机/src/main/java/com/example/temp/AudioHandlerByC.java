package com.example.temp;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import cn.shine.jni.SocketUtil;

//=========================TCP语音传输模块==================================================================
// 基于Tcp语音传输模块
public class AudioHandlerByC extends Thread {

	private int sSocket = 0;
	private Handler handler;
	// private static final int AUDIO_PORT = 4454;
	private static final int HZ = 8000;
	// private static final int HZ = 44100;
	private String target_ip;
	private int target_port;
	private int myPort;
	public boolean isStopTalk = false;
	public boolean isStopSend = false;
	private int totalSend = 0;
	private int totalReceive = 0;
	private int flag;
	private int audio_source;

	public AudioHandlerByC(Integer audio_source, Handler handler, String ip,
			int port, int myPort, int flag) {
		this.audio_source = audio_source;
		this.handler = handler;
		target_ip = ip + "\0";
		// target_ip = "192.168.43.1\0";
		target_port = port;
		this.myPort = myPort;
		this.flag = flag;
		socketUtil = new SocketUtil();
	}

	@Override
	public void run() {
		sendMsg("准备建立服务器");
		System.out.println("准备建立服务器");
		sSocket = socketUtil.createServer(myPort);// 监听音频端口
		sendMsg("接收到了客户端：,sSocket: " + sSocket);
		System.out.println("接收到了客户端：,sSocket: " + sSocket);
		if (sSocket < 0) {
			sendMsg("建立服务器失败");
			return;
		}
		// sendMsg("接收到播放请求了");
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
		new AudioPlay().start();
	}

	// 用来启动音频发送子线程
	public void audioSend() {
		new AudioSend().start();
	}

	// 音频播线程
	public class AudioPlay extends Thread {

		@Override
		public void run() {
			// 获得音频缓冲区大小
			int bufferSize = android.media.AudioTrack.getMinBufferSize(HZ,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT);

			// 获得音轨对象
			AudioTrack player = new AudioTrack(AudioManager.STREAM_MUSIC, HZ,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT, bufferSize,
					AudioTrack.MODE_STREAM);

			// 设置喇叭音量
			player.setStereoVolume(1.0f, 1.0f);
			// 开始播放声音
			player.play();
			byte[] audio = new byte[160];// 音频读取缓存
			int length = 0;
			int count = 0;

			while (!isStopTalk) {
				length = socketUtil.receive(sSocket, audio, audio.length);
				count++;
				totalReceive += length;
				if (length > 0 && length % 2 == 0) {
					// for(int
					// i=0;i<length;i++)audio[i]=(byte)(audio[i]*2);//音频放大1倍
					player.write(audio, 0, length);// 播放音频数据
					if ((count % 50) == 0) {
						sendMsg("收到了" + TextFormat.formatByte(totalReceive, 3));
					}
				}
			}
			player.stop();
			sendMsg("停止接收成功");
		}
	}

	AudioRecord recorder = null;
	private SocketUtil socketUtil;

	// 音频发送线程
	public class AudioSend extends Thread {

		@Override
		public void run() {
			int socket = socketUtil.createClient(target_ip.getBytes(),
					target_port);
			if (socket < 0) {
				sendMsg("连接服务器失败");
				return;
			}
			sendMsg("开始写入输出流2");
			// 获得录音缓冲区大小
			int bufferSize = AudioRecord.getMinBufferSize(HZ,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT);

			// 获得录音机对象
			// recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, HZ,
			// AudioFormat.CHANNEL_CONFIGURATION_MONO,
			// AudioFormat.ENCODING_PCM_16BIT, bufferSize);
			recorder = new AudioRecord(audio_source, HZ,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT, bufferSize);
			// AudioFormat.ENCODING_PCM_16BIT, bufferSize * 10);
			sendMsg("成功获取麦克");
			recorder.startRecording();// 开始录音
			byte[] readBuffer = new byte[640];// 录音缓冲区
			sendMsg("麦克准备好录音了");
			int length = 0;
			int count = 0;
			int speed = 0;
			long startTime = System.currentTimeMillis();
			while (!isStopSend) {
				// if ((count % 100) == 0) {
				// sendMsg("正在获取麦克的声音,第 " + count + " 次");
				// }
				count++;
				length = recorder.read(readBuffer, 0, 640);// 从mic读取音频数据
				totalSend += length;
				speed += length;
				long now = System.currentTimeMillis() - startTime;
				if (now >= 2000) {
					String formatSpeed = TextFormat.formatByte(speed / 2, 3);
					sendMsg("formatSpeed: " + formatSpeed);
					speed = 0;
					startTime = System.currentTimeMillis();
				}

				int v = 0;
				// 将 buffer 内容取出，进行平方和运算
				for (int i = 0; i < readBuffer.length; i++) {
					// 这里没有做运算的优化，为了更加清晰的展示代码
					v += readBuffer[i] * readBuffer[i];
				}

				// int value = (int) (Math.abs((int)(v
				// /(float)length)/10000) >> 1);

				// if (length > 0 && length % 2 == 0) {
				socketUtil.send(socket, readBuffer, length);
				if ((count % 100) == 0) {
					// sendMsg("输出：" + TextFormat.formatByte(totalSend, 3)
					// + ", 音量：" + (v / length));
					// + ", 音量：" + value);
				}
				// }
			}
			sendMsg("停止发送成功");
		}
	}

	public void release() {
		closeRecord();
	}

	public void closeRecord() {
		System.out.println("closeRecord()");
		isStopSend = true;
		if (null != recorder) {
			recorder.stop();
			recorder.release();
			recorder = null;
		}
	}
}
// =========================TCP语音传输模块结束==================================================================
