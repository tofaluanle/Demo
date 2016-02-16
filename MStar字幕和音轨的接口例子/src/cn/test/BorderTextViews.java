package cn.test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 带边框的textView
 * 
 * @author 蒋华全 (jianghq@biaoqi.com.cn)
 * @since 1.0
 * @date 2012-2-25
 */
public class BorderTextViews extends TextView {
    private Paint paint = null;
    private int color;

    public BorderTextViews(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // 设置边框颜色
    public void setPaintColor(int color) {
        this.color = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawClip(canvas);
    }

    private void drawClip(Canvas canvas) {
        if (this.getText().length() >= 1) {
            paint = new Paint();
            // 给边框设置颜色
            paint.setColor(color);
            // 上
            canvas.drawLine(0, 0, this.getWidth() - 1, 0, paint);
            // 左
            canvas.drawLine(0, 0, 0, this.getHeight() - 1, paint);
            // 下
            canvas.drawLine(0, this.getHeight() - 1, this.getWidth() - 1, this.getHeight() - 1, paint);
            // 右
            canvas.drawLine(this.getWidth() - 1, 0, this.getWidth() - 1, this.getHeight() - 1, paint);
        }
    }
}
