package com.owoh.video.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

public class ColorSeletedView extends View {

    private Paint mPaint;
    private int radius;

    private int width;
    private int height;
    private boolean isSeleted;

    public ColorSeletedView(Context context) {
        this(context, null);
    }

    public ColorSeletedView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorSeletedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = w;
        height = h;
        radius = Math.min(w, h) / 2;
    }

    public void setSeleted(boolean isSeleted) {
        this.isSeleted = isSeleted;
        postInvalidate();
    }

    public void setColor(String color) {
        if (!TextUtils.isEmpty(color)) {
            try {
                mPaint.setColor(Color.parseColor(color));
                postInvalidate();
            } catch (Exception e) {

            }

        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isSeleted) {
            canvas.drawCircle(width / 2, height / 2, radius, mPaint);
        } else {
            mPaint.setStrokeWidth(4);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(width / 2, height / 2, radius-mPaint.getStrokeWidth(), mPaint);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(width / 2, height / 2, radius / 2, mPaint);
        }

    }
}
