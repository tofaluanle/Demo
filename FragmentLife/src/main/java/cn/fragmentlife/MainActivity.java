package cn.fragmentlife;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        System.out.println("Activity   onCreate");

//        System.out.println(getTaskId() + "\n threadId: " + Thread.currentThread().getId() + "\n process: " + getCurProcessName(this));
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

    @Override
    protected void onRestart() {
        super.onRestart();
        System.out.println("Activity   onRestart");
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("Activity   onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("Activity   onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("Activity   onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("Activity   onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("Activity   onDestroy");
    }
}