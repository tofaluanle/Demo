package com.example.jnithread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;

//=========================TCP语音传输模块==================================================================
// 基于Tcp语音传输模块
public class AudioHandlerTCP {

	static {
		System.loadLibrary("jnithread");
		System.loadLibrary("tvqe");
	}

	jni mjni = new jni();
	AudioTrack audio = null;
	cn.thinkit.libtvqe.TVQEJNI tvqe = new cn.thinkit.libtvqe.TVQEJNI();

	private ServerSocket sSocket = null;
	private Handler handler;
	// private static final int AUDIO_PORT = 4454;
	private static final int HZ = 48000;
	// private static final int HZ = 44100;
	private String target_ip;
	private int target_port;
	private int myPort;
	public boolean isStopTalk = false;
	public boolean isStopSend = false;
	private int totalSend = 0;
	private int totalReceive = 0;
	private int audio_source;

	public AudioHandlerTCP(Integer audio_source, Handler handler, String ip,
			int port, int myPort) {
		this.audio_source = audio_source;
		this.handler = handler;
		target_ip = ip;
		target_port = port;
		this.myPort = myPort;
	}

	public void startReceive() {
		new AudioReceive().start();
	}

	public class AudioReceive extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				sSocket = new ServerSocket(myPort);// 监听音频端口
				System.out.println("Audio Handler socket started ...");
				sendMsg("准备接受播放请求", 1);
				while (!sSocket.isClosed() && null != sSocket) {
					sendMsg("正在接受播放请求", 1);
					Socket socket = sSocket.accept();
					socket.setSoTimeout(5000);
					audioPlay(socket);
					sendMsg("接收到播放请求了", 1);
				}
			} catch (IOException e) {
				e.printStackTrace();
				sendMsg("接收连接失败", 1);
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
			super.run();
			try {
				InputStream is = socket.getInputStream();
				tvqe.TVqeInit(16000, 0, 1, 1, 1, "");
				int minBuffSize = AudioTrack.getMinBufferSize(16000,
						AudioFormat.CHANNEL_OUT_MONO,
						AudioFormat.ENCODING_PCM_16BIT);
				android.os.Process
						.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
				audio = new AudioTrack(AudioManager.STREAM_MUSIC, 16000,
						AudioFormat.CHANNEL_OUT_MONO,
						AudioFormat.ENCODING_PCM_16BIT, minBuffSize,
						AudioTrack.MODE_STREAM);
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

				while (!isStopTalk) {
					count++;
					length = is.read(audioBuffer);// 从网络读取音频数据
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
				is.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 音频发送线程
	public class AudioSend extends Thread {

		@Override
		public void run() {
			super.run();
			Socket socket = null;
			OutputStream os = null;

			try {
				socket = new Socket(target_ip, target_port);
				os = socket.getOutputStream();
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
						os.write(bbuf, 0, 640);
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
				os.close();
				socket.close();
				sendMsg("停止发送成功", 2);
			} catch (IOException e) {
				e.printStackTrace();
				sendMsg("发送连接失败", 2);
			}
		}
	}

	public void release() {
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
	}

}
// =========================TCP语音传输模块结束==================================================================
