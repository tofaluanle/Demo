package cn.temp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * @date 2015/10/20.
 */
public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.i(getClass().getName() + " onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.i(getClass().getName() + " onStart");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.i(getClass().getName() + " onResume");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.d(getClass().getName() + " onPause");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.d(getClass().getName() + " onStop");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d(getClass().getName() + " onDestroy");

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.e(getClass().getName() + " onNewIntent");

    }
}
