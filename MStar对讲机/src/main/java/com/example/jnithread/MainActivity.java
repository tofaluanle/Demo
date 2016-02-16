package com.example.jnithread;

import android.app.Activity;
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
    private AudioHandler audio;
    private AudioHandler audio2;
    protected static final int SUCCESS_GET_IP = 0;
    protected static final int CHANGE_STATE_1 = 1;
    protected static final int CHANGE_STATE_2 = 2;
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SUCCESS_GET_IP:
                    tv_ip.setText(tv_ip.getText() + (String) msg.obj);
                    break;
                case CHANGE_STATE_1:
                    tv_state_1.setText((String) msg.obj);
                    break;
                case CHANGE_STATE_2:
                    tv_state_2.setText((String) msg.obj);
                    break;
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findView();
        init();
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

    protected void onDestroy() {
        stopAudio();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_receive:
                audio = new AudioHandler(Integer.valueOf(et_audio_source.getText().toString()), handler, et_ip.getText().toString(), Integer.valueOf(et_port.getText().toString()), Integer.valueOf(et_my_port.getText().toString()));
                audio.startReceive();
                break;
            case R.id.btn_send:
                audio2 = new AudioHandler(Integer.valueOf(et_audio_source.getText().toString()), handler, et_ip.getText().toString(), Integer.valueOf(et_port.getText().toString()), Integer.valueOf(et_my_port.getText().toString()));
                audio2.audioSend();
                break;
            case R.id.btn_stop:
                stopAudio();
                break;
        }
    }

    private void stopAudio() {
        if (null != audio2) {
            audio2.release();
            audio2 = null;
        } else {
            System.out.println("audio2 is null");
        }
        if (null != audio) {
            audio.release();
            audio = null;
        } else {
            System.out.println("audio is null");
        }
    }

}
