package com.owoh.video.widget;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * @author Harrison 唐广森
 * @description:
 * @date :2019/4/15 18:46
 */
public class VideoEffectSeekDrawable extends Drawable {
    private Paint mPaint;


    public VideoEffectSeekDrawable(int width,int height) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    public void draw(Canvas canvas) {
        //获取imageView的矩形边框
        Rect rect = getBounds();

        //获取imageView的最短宽高
        int min = Math.min(rect.height(), rect.width());
        //将图片永远画在imageView控件的的中心，min>>1表示圆的半径取控件最小的距离
        canvas.drawCircle(rect.centerX(), rect.centerY(), min >> 1, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

}
