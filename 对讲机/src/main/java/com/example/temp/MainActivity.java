package com.example.temp;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends Activity implements OnClickListener {

    protected static final int SUCCESS_GET_IP = 0;
    protected static final int CHANGE_STATE_1 = 1;
    protected static final int CHANGE_STATE_2 = 2;
    protected static final String SJJ_IP = "172.168.66.78";
    protected static final String DEFAULT_IP = "10.0.1.22";
//    protected static final String DEFAULT_IP = "172.168.9.122";
    private AudioHandlerNoSocket audio;
//    private AudioHandlerUDP audio;
//            private AudioHandler audio;
//    private AudioHandlerUDP audio2;
        private AudioHandler audio2;
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            String obj = (String) msg.obj;
            switch (msg.what) {
                case SUCCESS_GET_IP:
                    tv_ip.setText(tv_ip.getText() + obj);
                    if(!obj.equals(SJJ_IP)) {
                        et_ip.setText(SJJ_IP);
                    }
                    break;
                case CHANGE_STATE_1:
                    tv_state_1.setText(obj);
                    break;
                case CHANGE_STATE_2:
                    tv_state_2.setText(obj);
                    break;
            }
        }
    };
    private TextView tv_ip;
    private TextView tv_state_1;
    private TextView tv_state_2;
    private EditText et_my_port;
    private EditText et_audio_source;
    private EditText et_port;
    private EditText et_ip;
    private Button btn_send;
    private Button btn_stop;
    private Button btn_receive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findView();

        init();

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 150, 0);
    }

    private void init() {
        new Thread() {
            @Override
            public void run() {
                try {
                    String ip = NetUtil.getIp();
                    Message msg = new Message();
                    msg.obj = ip;
                    msg.what = SUCCESS_GET_IP;
                    handler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        et_ip.setText(DEFAULT_IP);
    }

    private void findView() {
        tv_ip = (TextView) findViewById(R.id.tv_ip);
        tv_state_1 = (TextView) findViewById(R.id.tv_state_1);
        tv_state_2 = (TextView) findViewById(R.id.tv_state_2);
        et_port = (EditText) findViewById(R.id.et_port);
        et_ip = (EditText) findViewById(R.id.et_ip);
        et_my_port = (EditText) findViewById(R.id.et_my_port);
        et_audio_source = (EditText) findViewById(R.id.et_audio_source);
        btn_receive = (Button) findViewById(R.id.btn_receive);
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_stop = (Button) findViewById(R.id.btn_stop);

        btn_send.setOnClickListener(this);
        btn_receive.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        System.out.println("onDestroy()");
        if (null != audio) {
            audio.isStopTalk = true;
            audio.release();
        }
        if (null != audio2) {
            audio2.isStopTalk = true;
            audio2.release();
        }
        super.onDestroy();
        System.exit(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_receive:
                audio2 = new AudioHandler(Integer.valueOf(et_audio_source.getText().toString()), handler, et_ip.getText().toString(), Integer.valueOf(et_port.getText().toString()), Integer.valueOf(et_my_port.getText().toString()), 1);
                audio2.start();
                break;
            case R.id.btn_send:
                if (null != audio) {
                    audio.closeRecord();
                }
//                audio = new AudioHandler(Integer.valueOf(et_audio_source.getText().toString()), handler, et_ip.getText().toString(), Integer.valueOf(et_port.getText().toString()), Integer.valueOf(et_my_port.getText().toString()), 2);
                audio2.audioSend();
//                audio = new AudioHandlerNoSocket(Integer.valueOf(et_audio_source.getText().toString()), handler, 1);
//                audio.start();
                break;
            case R.id.btn_stop:
                if (null != audio) {
                    audio.closeRecord();
                } else {
                    System.out.println("audio is null");
                }
                if (null != audio2) {
                    audio2.closeRecord();
                } else {
                    System.out.println("audio2 is null");
                }
                break;
        }
    }

    public void onClick4(View v) {
        finish();
    }
}
