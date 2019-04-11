package com.cgfay.cameralibrary.widget;

/**
 * @author Harrison 唐广森
 * @description: 这个是编辑特效进度条颜色的
 * @date :2019/4/11 15:08
 *
 */
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/**
 * Created by musk.
 */

public class EffectProgressDrawable extends Drawable {

    private Paint mPaint;
    private int mWidth;
    private Bitmap mBitmap;

    public EffectProgressDrawable(Bitmap bitmap) {
        mBitmap = bitmap;
        //着色器，设置横向和纵向的着色模式为平铺
        BitmapShader bitmapShader = new BitmapShader(mBitmap,
                Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setShader(bitmapShader);
        mWidth = Math.min(mBitmap.getWidth(), mBitmap.getHeight());
    }

    //绘制
    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(mWidth / 2, mWidth / 2, mWidth / 2, mPaint);
    }

    //设置透明度值
    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    //设置颜色过滤器
    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    //返回不透明度
    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    //返回图片实际的宽高
    @Override
    public int getIntrinsicWidth() {
        return mWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mWidth;
    }
}
