package cn.temp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * @auther 宋疆疆
 * @date 2015/10/20.
 */
public class SecondActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecondActivity.this, SecondActivity.class);
                intent.putExtra("hash", SecondActivity.this.toString());
                intent.putExtra("task", SecondActivity.this.getTaskId());
                startActivity(intent);
            }
        });

        TextView tv_class = (TextView) findViewById(R.id.textView2);
        tv_class.setText(this.toString());

        TextView tv_task = (TextView) findViewById(R.id.textView3);
        tv_task.setText(getTaskId() + "");
    }
}
