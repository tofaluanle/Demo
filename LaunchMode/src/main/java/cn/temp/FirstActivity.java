package cn.temp;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * @auther 宋疆疆
 * @date 2015/10/20.
 */
public class FirstActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstActivity.this, SecondActivity.class);
                intent.putExtra("hash", FirstActivity.this.toString());
                intent.putExtra("task", FirstActivity.this.getTaskId());

                intent = new Intent();
                ComponentName com = new ComponentName("cn.test", "cn.test.MainActivity");
                intent.setComponent(com);
                startActivity(intent);
            }
        });

        TextView tv_class = (TextView) findViewById(R.id.textView2);
        tv_class.setText(this.toString());

        TextView tv_task = (TextView) findViewById(R.id.textView3);
        tv_task.setText(getTaskId() + "\n threadId: " + Thread.currentThread().getId() + "\n process: " + getCurProcessName(this));
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
}
