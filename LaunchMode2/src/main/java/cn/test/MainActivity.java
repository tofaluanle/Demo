package cn.test;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.tvos.common.AudioManager;
import com.tvos.common.TvManager;
import com.tvos.common.vo.TvOsType;


public class MainActivity extends Activity {

    TextView tv;
    VideoView vv;
    ImageView im;
    ImageView im2;
    WebView wv;
    View view;
    MediaPlayer player;
    RelativeLayout rl;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            tv.setText((CharSequence) msg.obj);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findView();
        init();
    }

    private void findView() {
        tv = (TextView) findViewById(R.id.textView);
        im = (ImageView) findViewById(R.id.imageView);
        im2 = (ImageView) findViewById(R.id.imageView2);
        rl = (RelativeLayout) findViewById(R.id.rl);
        vv = (VideoView) findViewById(R.id.videoView);
//        wv = (WebView) findViewById(R.id.webView);
    }

    @TargetApi(16)
    private void init() {
        SpeechEngine.getInstance().init(this);

        tv.setText(toString() + "\n task: " + getTaskId() + "\n threadId: " + Thread.currentThread().getId() + "\n process: " + getCurProcessName(this));
    }

    String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {

                return appProcess.processName;
            }
        }
        return null;
    }


    public void click1(View v) {
        vv.setVideoPath("/data/v_1.mp4");
        vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                vv.start();
            }
        });

        vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                SystemClock.sleep(1000);
                vv.setVideoPath("/data/v_1.mp4");
            }
        });
    }

    public void click2(View v) {
        vv.stopPlayback();
    }

    public void click3(View v) {
    }

    public void click4(View v) {
    }

    public void click5(View v) {
    }

    public void click6(View v) {
    }

    private void audioMain() {
        if (Build.VERSION.SDK_INT == 15) {
            AudioManager audioManager = TvManager.getAudioManager();
            audioManager.setAudioSource(TvOsType.EnumInputSource.E_INPUT_SOURCE_CVBS, AudioManager.EnumAudioProcessorType.E_AUDIO_PROCESSOR_MAIN);
        } else if (Build.VERSION.SDK_INT == 18) {
            com.mstar.android.tvapi.common.AudioManager audioManager = com.mstar.android.tvapi.common.TvManager.getInstance().getAudioManager();
            audioManager.setAudioSource(com.mstar.android.tvapi.common.vo.TvOsType.EnumInputSource.E_INPUT_SOURCE_CVBS, com.mstar.android.tvapi.common.vo.EnumAudioProcessorType.E_AUDIO_PROCESSOR_MAIN);
        }
    }

    private void audioSub() {
        if (Build.VERSION.SDK_INT == 15) {
            AudioManager audioManager = TvManager.getAudioManager();
            audioManager.setAudioSource(TvOsType.EnumInputSource.E_INPUT_SOURCE_CVBS, AudioManager.EnumAudioProcessorType.E_AUDIO_PROCESSOR_SUB);
        } else if (Build.VERSION.SDK_INT == 18) {
            com.mstar.android.tvapi.common.AudioManager audioManager = com.mstar.android.tvapi.common.TvManager.getInstance().getAudioManager();
            audioManager.setAudioSource(com.mstar.android.tvapi.common.vo.TvOsType.EnumInputSource.E_INPUT_SOURCE_CVBS, com.mstar.android.tvapi.common.vo.EnumAudioProcessorType.E_AUDIO_PROCESSOR_SUB);
        }
    }

    private void onCloseScreen() {
        try {
            Context context = createPackageContext("com.shine.vodservice", CONTEXT_IGNORE_SECURITY);
            SharedPreferences sharedPreferences = context.getSharedPreferences("shine", 7);
            boolean state = sharedPreferences.getBoolean("ScreenState", true);
            Intent intent = new Intent();
            if (state) {
                intent.setAction("com_shine_close_screen");
            } else {
                intent.setAction("com_shine_open_screen");
            }
            sendBroadcast(intent);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 258) {
            onCloseScreen();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
