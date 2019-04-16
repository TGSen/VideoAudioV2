package com.cgfay.cameralibrary.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import com.cgfay.cameralibrary.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Harrison 唐广森
 * @description:
 * @date :2019/4/15 18:36
 */
public class VideoEffectSeekBar extends android.support.v7.widget.AppCompatSeekBar {
    private String mVerifyText;
    private int mTextSize;
    private Paint mTextPaint;
    private Paint mProgressPaint;
    private int mRadius = 16;
    private int mMaskThumb;
    private int type = 1;
    private static final int DEFAULT_THUMB = 1;
    private static final int CORRECT_THUMB = 2;
    private static final int WRONG_THUMB = 3;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public VideoEffectSeekBar(Context context) {
        super(context);
        init(context, null);
    }

    public VideoEffectSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public VideoEffectSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.VideoEffectBar);
        int count = array.getIndexCount();
        for (int i = 0; i < count; i++) {
            int index = array.getIndex(i);
            switch (index) {
                case R.styleable.VideoEffectBar_verifyText:
                    mVerifyText = array.getString(index);
                    break;
                case R.styleable.VideoEffectBar_text_Size:
                    mTextSize = array.getDimensionPixelSize(index, 16);
                    break;
            }
        }
        array.recycle();
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(getResources().getColor(R.color.white));
    }

    /**
     * 绘制 "滑动完成验证"的提示文字 * * @param canvas
     */
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getThumb() == null) {
            return;
        }
        canvas.save();
        Rect rect = new Rect();
        int textLength = mVerifyText == null ? 0 : mVerifyText.length();
        mTextPaint.getTextBounds(mVerifyText, 0, textLength, rect);
        int startX = (getWidth() - rect.width()) / 2 + getThumb().getIntrinsicWidth() / 2;
        int startY = getHeight() / 2 + rect.height() / 2;
        // 绘制滑块中的提示文字
        canvas.drawText(mVerifyText, startX, startY, mTextPaint);
        int thumbLeft = getThumb().getBounds().left + 70;
        // thumb按钮的左边
        int top = getThumb().getBounds().top;
        int bottom = getThumb().getBounds().bottom;
        // 初始位置不需要绘制进度
        if (null == getThumb()) {
            canvas.restore();
            return;
        }
        mProgressPaint = new Paint();
        mProgressPaint.setStyle(Paint.Style.FILL);
        //设置填充样式
       // mProgressPaint.setStrokeWidth(15);
        // 设置画笔宽度
        switch (getType()) {
            case DEFAULT_THUMB:
                mProgressPaint.setColor(getResources().getColor(R.color.colorAccent));
                mMaskThumb = R.drawable.ic_camera_blur_selected;
                break;
            case CORRECT_THUMB:
                mProgressPaint.setColor(getResources().getColor(R.color.colorAccent));
                mMaskThumb = R.drawable.ic_camera_blur_selected;
                break;
            case WRONG_THUMB:
                mProgressPaint.setColor(getResources().getColor(R.color.colorAccent));
                mMaskThumb = R.drawable.ic_camera_blur_selected;
                break;
        }
        canvas.drawRoundRect(new RectF(0, top, thumbLeft, bottom), mRadius, mRadius, mProgressPaint);

        // 直接在原来thumb上盖一个一模一样的图标
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), mMaskThumb), getThumb().getBounds().left, top, null);
        if (getType() == WRONG_THUMB) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    setProgress(0);
                    timerTask.cancel();
                }
            };
            new Timer().schedule(timerTask, 500);
        }

    }

    private TimerTask timerTask;

    /**
     * 禁用点击非thumb按钮区域的点击事件 * @param event * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (getThumb() == null) {
            return false;
        }
        Rect thumbRect = getThumb() == null ? null : getThumb().getBounds();
        int thumbLeft = thumbRect.left;
        int thumbRight = thumbRect.right;
        int thumbTop = thumbRect.top;
        int thumbBottom = getThumb().getBounds().bottom;
        int eventX = (int) event.getX();
        int eventY = (int) event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (eventX <= thumbLeft || eventX >= thumbRight || eventY <= thumbTop || eventY >= thumbBottom) {
                return false;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * 清除提示滑动的文字 使用的方法是直接设置画笔颜色透明
     */
    public void dismissDragText() {
        if (getProgress() == 0) {
            mTextPaint.setColor(getResources().getColor(R.color.colorAccent));
        } else {
            mTextPaint.setColor(getResources().getColor(R.color.transparent));
        }
        invalidate();
    }
}
