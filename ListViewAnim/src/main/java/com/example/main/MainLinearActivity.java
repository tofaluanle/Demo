package com.example.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * 2013年10月25日 17:07:20
 * 
 * @author 洋葱
 * */
public class MainLinearActivity extends Activity implements OnClickListener {
	Button button1, button2, button3, button4, button5, button6, button7;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_linear);
		initView();
	}

	public void initView() {
		button1 = (Button) findViewById(R.id.button1);
		button2 = (Button) findViewById(R.id.button2);
		button3 = (Button) findViewById(R.id.button3);
		button4 = (Button) findViewById(R.id.button4);
		button5 = (Button) findViewById(R.id.button5);
		button6 = (Button) findViewById(R.id.button6);
		button7 = (Button) findViewById(R.id.button7);
		button7.setOnClickListener(this);
		button6.setOnClickListener(this);
		button5.setOnClickListener(this);
		button4.setOnClickListener(this);
		button3.setOnClickListener(this);
		button2.setOnClickListener(this);
		button1.setOnClickListener(this);
	}

	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.button1:
			intent.setClass(this, MainActivity1.class);
			startActivity(intent);
			break;
		case R.id.button2:
			intent.setClass(this, MainActivity2.class);
			startActivity(intent);
			break;
		case R.id.button3:
			intent.setClass(this, MainActivity3.class);
			startActivity(intent);
			break;
		case R.id.button4:
			intent.setClass(this, MainActivity4.class);
			startActivity(intent);
			break;
		case R.id.button5:
			intent.setClass(this, MainActivity5.class);
			startActivity(intent);
			break;
		case R.id.button6:
			intent.setClass(this, MainActivity6.class);
			startActivity(intent);
			break;
		case R.id.button7:
			intent.setClass(this, MainActivity7.class);
			startActivity(intent);
			break;

		default:
			break;
		}
	}

}
