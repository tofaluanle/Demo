package com.example.jnithread;

public class jni{

	public native int init(int cardin ,int devicein);
	public native int read640(byte[] bytes);
	public native int finish();
	static{
		System.loadLibrary("jnithread");
	}
}




