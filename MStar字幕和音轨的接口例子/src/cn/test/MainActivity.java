package cn.test;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.Metadata;
import android.media.SubtitleTrackInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.MediaController;

import com.mstar.tv.service.skin.AudioSkin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

public class MainActivity extends Activity implements OnClickListener {

	private static final String TAG = "sjj";
	private static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;
	private static final int SPEED_STEP = 10 * 1000;
	private static final int MAX_WIND_SPEED = 16;
	private static final int FREQUENCY_JUMP = 1000;
	private int speed;
	private int windSpeed;
	private Runnable jumpR;
	private int targetPos;
	private int oldTargetPos;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			initVideoView();
		};
	};
	private VideoView vv;
	private BorderTextViews btv;
	private int audioIndex;
	private MediaPlayer mediaPlayer;
	private boolean isSubVisible;
	private boolean isLoadSubtitle;
	private int subIndex = 0;
	private AudioSkin as;
	private Button btn1;
	private Button btn14;
	private Button btn15;
	// String path =
	// "http://172.168.1.3/preview/[电影天堂www.dy2018.net]新少林寺.1024x576.国粤双语.中文字幕.mkv";
	// String path = "http://172.168.1.3/preview/Top001.肖申克的救赎.mkv";
	// String path = "http://172.168.1.3/preview/你是哪里人.avi";
	// String path =
	// "http://172.168.1.3/preview/大上海The.Last.Tycoon.2012.BluRay.1080p.2Audio.DTS-HD.MA.7.1.x264-beAst.mkv";
	// String path =
	// "http://172.168.0.201/preview/Girls.Generation.Interview.2012.BluRay.720p.AC3.x264-CHD.mkv";
	// String path = "http://172.168.11.11/preview/1.WMV";
	// String path = "http://172.168.11.11/preview/金刚狼1280X720.wmv";
	// String path = "http://172.168.11.11/preview/jinyiwei.mkv";
	// String path = "http://172.168.11.11/preview/dabingxiaojiang.mkv";
	// String path =
	// "http://172.168.0.201/preview/功夫熊猫(国英双语)Kung.Fu.Panda.2008.BluRay.720p.x264.AC3-WOFEI.mkv";
	// String path =
	// "http://172.168.0.201/preview/功夫熊猫(国英双语)Kung.Fu.Panda.2008.BluRay.720p.x264.AC3-WOFEI.mp4";
	// String path = "http://172.168.0.201/preview/football.mp4";
	// String path = "http://172.168.0.201/preview/test_sub_2.mkv";
	// String path =
	// "http://172.168.0.201/preview/ٻŮ_Ļ_2(____˫_HAVC480Pdianwanren.mp4";
	// String path =
	// "http://bj.baidupcs.com/file/a0769db336bc8257372d60cc7ded97fc?fid=3926677583-250528-1057108795195266&time=1401781486&sign=FDTAXER-DCb740ccc5511e5e8fedcff06b081203-wvZS8ptfE8Nhu5wj51ggDGddJuo%3D&to=bb&fm=N,B,G,bs&newver=1&expires=1401782086&rt=sh&r=871928223&logid=900179143&sh=1&vuk=3926677583&fn=test_sub_2.mkv";
	// String path = "http://124.205.155.88/film/test_sub_2.mkv";
	String path = "/mnt/usb/sda1/Z狼.mp4";
//	String path = "/data/local/tmp/lixiaoli.mp4";
//	String path = "/mnt/usb/sda1/hansoncool@HD.Club-4K-Chimei-inn-4k-60mbps.mp4";
	// String path =
	// "http://sh.ctfs.ftn.qq.com/ftn_handler/186c06158f60da789ee110333faf279af0e8afd74b3a4542c4fbe133b812286195efb536e1a8e8cb7fd3da89ac9103e5ea923452e1cfc9685a070fe0bd26275a/?fname=test_sub_2.mkv&k=2638343065eb24c63711e61644610316525c06015157505a480a5001004c055f560819095307531454590d010107570006005653626f314d004b406f111453665716595b146171&fr=00&&txf_fid=9a881c62cea34523c363909d2c07094a9d58c913&xffz=15365625";
	// String path = "/mnt/usb/sda1/十二生肖.mkv";
	// String path =
	// "/mnt/sdcard/Movies/功夫熊猫(国英双语) Kung.Fu.Panda.2008.BluRay.720p.x264.AC3-WOFEI.mkv";
	// String path = "/mnt/sdcard/Movies/football.mp4";
	// String path = "/mnt/sdcard/Movies/test_sub_2.mkv";
	// String path = "/data/local/tmp/test_sub_2.mkv";
	// String subPath = "http://172.168.0.201/preview/football.ass";
	// String subPath = "/mnt/sdcard/Movies/gongfu1.ssa";
	String subPath = "/data/football.ass";
	// String subPath = "/mnt/sdcard/Movies/football.ass";
	private Button btn5;
	private Button btn6;
	private Button btn8;
	private Button btn7;
	private AudioManager mAm;
	private Button btn9;
	private Button btn11;
	private Button btn10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findView();

		as = new AudioSkin(this);
		as.connect(null);

		windSpeed = 1;
		jumpR = new JumpRunnabl();
		mAm = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		// initPath();

		initVideoView();

	}

	private void initVideoView() {
		// isSubVisible = true;
		// openSubtitle();
		targetPos = 0;
		oldTargetPos = 0;
		mHandler.removeCallbacks(jumpR);
		vv.stopPlayback();
		vv.setVideoPath(path);
		MediaController controller = new MediaController(this);
		vv.setMediaController(controller);

		vv.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				vv.start();
				// if (isLoadSubtitle) {
				// loadExternSubTitle();
				// }
				mediaPlayer = mp;
				mediaPlayer.setOnInfoListener(new OnInfoListener() {

					@Override
					public boolean onInfo(MediaPlayer mp, int what, int extra) {
						// 刷新字幕的代码，字幕实际上是用textview显示的
						if (what == MediaPlayer.MEDIA_INFO_SUBTITLE_UPDATA) {
							String data = mp.getSubtitleData();
							if (0 == extra) {
								data = "";
							}
							btv.setText(data);
							System.out.println("data: " + data);
						}
						return false;
					}
				});

				// mediaPlayer.setOnBufferingUpdateListener(new
				// OnBufferingUpdateListener() {
				//
				// @Override
				// public void onBufferingUpdate(MediaPlayer mp, int percent) {
				// System.out.println("percent: " + percent);
				// }
				// });
			}
		});

		vv.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				System.out.println("onCompletion");
				mHandler.removeCallbacks(jumpR);
				vv.stopPlayback();
				mHandler.sendEmptyMessageDelayed(0, 1000);
			}
		});

		vv.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				System.out.println("onError what: " + what + ", extra: "
						+ extra);
				return false;
			}
		});
	}

	private void findView() {
		vv = (VideoView) findViewById(R.id.videoView1);
		btv = (BorderTextViews) findViewById(R.id.btv);
		btn1 = (Button) findViewById(R.id.button1);
		Button btn2 = (Button) findViewById(R.id.button2);
		Button btn3 = (Button) findViewById(R.id.button3);
		Button btn4 = (Button) findViewById(R.id.button4);
		btn5 = (Button) findViewById(R.id.button5);
		btn6 = (Button) findViewById(R.id.button6);
		btn7 = (Button) findViewById(R.id.button7);
		btn8 = (Button) findViewById(R.id.button8);
		btn9 = (Button) findViewById(R.id.button9);
		btn10 = (Button) findViewById(R.id.button10);
		btn11 = (Button) findViewById(R.id.button11);
		Button btn12 = (Button) findViewById(R.id.button12);
		Button btn13 = (Button) findViewById(R.id.button13);
		btn14 = (Button) findViewById(R.id.button14);
		btn15 = (Button) findViewById(R.id.button15);

		btn1.setOnClickListener(this);
		btn2.setOnClickListener(this);
		btn3.setOnClickListener(this);
		btn4.setOnClickListener(this);
		btn5.setOnClickListener(this);
		btn6.setOnClickListener(this);
		btn7.setOnClickListener(this);
		btn8.setOnClickListener(this);
		btn9.setOnClickListener(this);
		btn10.setOnClickListener(this);
		btn11.setOnClickListener(this);
		btn12.setOnClickListener(this);
		btn13.setOnClickListener(this);
		btn14.setOnClickListener(this);
		btn15.setOnClickListener(this);
	}

	private void initPath() {
		File file = new File("/data/work/path.txt");
		try {
			Reader in = new FileReader(file);
			BufferedReader br = new BufferedReader(in);
			path = br.readLine();
			subPath = br.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		System.out.println("view id: " + v.getId() + "btn: "
				+ (v.getId() - R.id.button1));
		switch (v.getId()) {
		case R.id.button1:
			openSubtitle();
			break;
		case R.id.button2:
			loadExternSubTitle();
			break;
		case R.id.button3:
			audioIndex--;
			if (0 > audioIndex) {
				audioIndex = 0;
			}
			// 设置播放第audioIndex个音轨
			mediaPlayer.setAudioTrack(audioIndex);
			break;
		case R.id.button4:
			audioIndex++;
			// AudioTrackInfo info = mediaPlayer.getAudioTrackInfo(false);
			int count = getAudioTrackCount();
			System.out.println(count);
			if (count - 1 < audioIndex) {
				audioIndex = count - 1;
			}
			// 设置播放第audioIndex个音轨
			mediaPlayer.setAudioTrack(audioIndex);
			break;
		case R.id.button5:
			if (speed < 0) {
				speed = 0;
			}
			speed = speed + SPEED_STEP;
			if (speed > 30 * 1000) {
				speed = 0;
			}
			btn6.setText("跳退   0");
			btn5.setText("跳进   " + speed / SPEED_STEP);
			mHandler.removeCallbacks(jumpR);
			mHandler.post(jumpR);
			break;
		case R.id.button6:
			if (speed > 0) {
				speed = 0;
			}
			speed = speed - SPEED_STEP;
			if (speed < -30 * 1000) {
				speed = 0;
			}
			btn5.setText("跳进   0");
			btn6.setText("跳退   " + Math.abs(speed) / SPEED_STEP);
			mHandler.removeCallbacks(jumpR);
			mHandler.post(jumpR);
			break;
		case R.id.button7:
			volumeUp();
			break;
		case R.id.button8:
			volumeDown();
			break;
		case R.id.button9:
			volumeMute();
			break;
		case R.id.button10:
			playWind();
			break;
		case R.id.button11:
			playRewind();
			break;
		case R.id.button12:
			initVideoView();
			break;
		case R.id.button13:
			temp();
			break;
		case R.id.button14:
			changeInternalSubtitle();
			break;
		case R.id.button15:
			pause();
			break;
		}
	}

	private void openSubtitle() {
		if (mediaPlayer == null) {
			return;
		}
		System.out.println("open or close subtitle");
		if (isSubVisible) {
			// 关闭字幕
			mediaPlayer.offSubtitleTrack();
			isLoadSubtitle = false;
		} else {
			// 开启字幕
			mediaPlayer.offSubtitleTrack();
			mediaPlayer.onSubtitleTrack();
		}
		btn1.setText("字幕" + (isSubVisible ? "关" : "开"));
		// Toast.makeText(this, isSubVisible ? "关" : "开", 0).show();
		isSubVisible = !isSubVisible;
	}

	private void loadExternSubTitle() {
		isSubVisible = false;
		openSubtitle();
		isLoadSubtitle = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 加载外挂字幕
				mediaPlayer.setSubtitleDataSource(subPath);
				// mediaPlayer
				// .setSubtitleDataSource("/mnt/sdcard/test/dabing.srt");
			}
		}).start();
	}

	private void pause() {
		if (vv.isPlaying()) {
			mHandler.removeCallbacks(jumpR);
			vv.pause();
			btn15.setText("继续");
		} else {
			start();
		}
	}

	private void start() {
		btn15.setText("暂停");
		vv.start();
	}

	private void changeInternalSubtitle() {
		isLoadSubtitle = false;
		mediaPlayer.offSubtitleTrack();
		mediaPlayer.onSubtitleTrack();
		SubtitleTrackInfo sinfo = mediaPlayer.getAllSubtitleTrackInfo();// 如果不加这句，无法从外挂字幕切换回内嵌字幕
		int i = sinfo.getAllInternalSubtitleCount();
		mediaPlayer.getSubtitleTrackInfo(subIndex);
		mediaPlayer.setSubtitleTrack(subIndex);
		if (++subIndex >= i) {
			subIndex = 0;
		}
		btn14.setText("内嵌字幕: " + subIndex);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_0:
			temp();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void temp() {
		btn1.requestFocus();
		btn1.performClick();
	}

	private void playWind() {
		windSpeed = mediaPlayer.getPlayMode();
		if (windSpeed < 0) {
			windSpeed = 1;
		}
		windSpeed *= 2;
		if (windSpeed > MAX_WIND_SPEED) {
			windSpeed = 1;
		}
		System.out.println("windSpeed: " + windSpeed);
		mediaPlayer.setPlayMode(windSpeed);
		btn11.setText("快退   " + 0);
		btn10.setText("快进   " + windSpeed);
	}

	private void playRewind() {
		windSpeed = mediaPlayer.getPlayMode();
		if (windSpeed > 0) {
			windSpeed = -1;
		}
		windSpeed *= 2;
		if (windSpeed < -MAX_WIND_SPEED) {
			windSpeed = 1;
		}
		System.out.println("windSpeed: " + windSpeed);
		mediaPlayer.setPlayMode(windSpeed);
		btn10.setText("快进   " + 0);
		btn11.setText("快退   " + windSpeed);
	}

	public int getAudioTrackCount() {
		if (mediaPlayer == null)
			return 1;
		Metadata data = mediaPlayer.getMetadata(true, true);
		if (data != null) {
			int totalTrackNum = 1;
			if (data.has(Metadata.TOTAL_TRACK_NUM)) {
				totalTrackNum = data.getInt(Metadata.TOTAL_TRACK_NUM);
				return totalTrackNum;
			}
		}
		return 1;
	}

	private final class JumpRunnabl implements Runnable {

		@Override
		public void run() {
			if (!vv.isPlaying()) {
				vv.start();
			}
			if (0 != speed) {
				int currentPosition = vv.getCurrentPosition();
				Log.v(TAG, "cur: " + currentPosition);
				targetPos = currentPosition + speed;
				int minus = oldTargetPos - targetPos;
				// Log.v(TAG, "targetPos: " + targetPos + ", oldTargetPos: "
				// + oldTargetPos + ", minus: "
				// + minus);
				Log.v(TAG, "oldTargetPos + speed: " + (oldTargetPos + speed)
						+ ", targetPos: " + targetPos + ", flag: "
						+ (oldTargetPos + speed < targetPos));
				if (oldTargetPos + speed > targetPos) {
					targetPos = oldTargetPos + speed;
				}
				// if (targetPos > mediaPlayer.getDuration()) {
				// mHandler.removeCallbacks(jumpR);
				// } else {
				vv.seekTo(targetPos);
				oldTargetPos = targetPos;
				// }
			}
			mHandler.postDelayed(jumpR, FREQUENCY_JUMP);
		}
	}

	public void volumeUp() {
		as.setMuteFlag(false);
		int volume = mAm.getStreamVolume(STREAM_TYPE);
		volume = volume + 10 > mAm.getStreamMaxVolume(STREAM_TYPE) ? mAm
				.getStreamMaxVolume(STREAM_TYPE) : volume + 10;
		mAm.setStreamVolume(STREAM_TYPE, volume, 0);
		btn7.setText("音量+" + volume);
	}

	public void volumeDown() {
		as.setMuteFlag(false);
		int volume = mAm.getStreamVolume(STREAM_TYPE);
		volume = volume - 10 < 0 ? 0 : volume - 10;
		mAm.setStreamVolume(STREAM_TYPE, volume, 0);
		btn7.setText("音量+" + volume);
	}

	public void volumeMute() {
		// int volume = mAm.getStreamVolume(STREAM_TYPE);
		// if (0 == volume) {
		// mAm.setStreamMute(STREAM_TYPE, false);
		// } else {
		// mAm.setStreamMute(STREAM_TYPE, true);
		// }
		as.setMuteFlag(!as.GetMuteFlag());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		as.disconnect();
		as = null;
	}
}
