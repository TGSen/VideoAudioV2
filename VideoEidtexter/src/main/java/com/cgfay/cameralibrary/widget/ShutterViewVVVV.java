package com.cgfay.cameralibrary.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.cgfay.cameralibrary.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Harrison 唐广森
 * @description:
 * @date :2019/4/29 15:22
 */
public class ShutterViewVVVV extends View {
    // 外圆背景颜色
    private int mOuterOvalBgColor;
    //内圆颜色
    private int mInerOvalBgColor;
    //内圆半径
    private int mInerOvalRadius;
    //外圆半径
    private int mOuterOvalRadius;
    //当是录制状态的时候，这个是外边圆的圆圈
    private int mOuterOvalWidth;

    private int mCurrentOvalWidth;

    // 控件填充背景
    private Paint mFillPaint;
    private Paint mStrokePaint;


    // 按钮动画
    private ValueAnimator mButtonAnim;

    //点击模式
    public static final int MODE_CLICK_SINGLE = 0x001;
    //长按模式
    public static final int MODE_CLICK_LONG = 0x002;
    private int mMeasuredWidth;
    private int mMeasuredHeight;
    private OnShutterListener mOnShutterListener;
    private boolean mEnableEncoder;
    private int maxTimes = 10 * 1000;


    //设置最大的时间
    public void setMax(int max) {
        this.maxTimes = max;
    }

    public void setProgress(float progress) {
        if (progress >= maxTimes) {
            Log.e("Harrison", "******结束录制");
            mCurrentState = STATE_IDLE;
            stopAnimation();
            if (mOnShutterListener != null) {
                mOnShutterListener.onEndRecord();
            }
        }
    }

    @IntDef({MODE_CLICK_SINGLE, MODE_CLICK_LONG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ShutterViewMode {

    }

    public void setMaxTimes(int time) {
        this.maxTimes = time;
    }

    //闲置状态
    public static final int STATE_IDLE = 0x001;
    //开始录制状态
    public static final int STATE_START = 0x002;
    //录制结束
    public static final int STATE_END = 0x003;

    private int mCurrentState = STATE_IDLE;

    private int mCurrentMode = MODE_CLICK_SINGLE;


    @IntDef({STATE_IDLE, STATE_START, STATE_END})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ShutterViewState {

    }

    public void setCurrentMode(@ShutterViewMode int mCurrentMode) {
        this.mCurrentMode = mCurrentMode;
    }

    public ShutterViewVVVV(Context context) {
        this(context, null);
    }

    public ShutterViewVVVV(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShutterViewVVVV(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * 初始化属性
     */
    private void init(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ShutterView);
        mOuterOvalBgColor = array.getColor(R.styleable.ShutterView_outer_oval_color, ContextCompat.getColor(context, R.color.white));
        mInerOvalBgColor = array.getColor(R.styleable.ShutterView_iner_oval_color, ContextCompat.getColor(context, R.color.white));
        mInerOvalRadius = array.getDimensionPixelSize(R.styleable.ShutterView_iner_oval_radius, 10);
        mOuterOvalRadius = array.getDimensionPixelSize(R.styleable.ShutterView_outer_oval_radius, 10);
        mOuterOvalWidth = array.getDimensionPixelSize(R.styleable.ShutterView_outer_oval_width, 10);
        array.recycle();
        // 填充背景的Paint
        mFillPaint = new Paint();
        mFillPaint.setAntiAlias(true);

        mFillPaint = new Paint();
        mFillPaint.setAntiAlias(true);

        mStrokePaint = new Paint();
        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setColor(mInerOvalBgColor);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMeasuredWidth = getMeasuredWidth();
        mMeasuredHeight = getMeasuredHeight();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

    }

    //是否开始编码
    private boolean isStart;

    /**
     * 分长按和短按，进行动画
     *
     * @param event
     * @return
     */
    private long mTouchStartTime;
    private long mTouchEndTime;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mEnableEncoder) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartTime = System.currentTimeMillis();
                if (isStart) {
                    stopAnimation();
                    return false;
                }
                if (mCurrentMode == MODE_CLICK_LONG) {
                    Log.e("Harrison","****MODE_CLICK_LONG");
                    mTouchEndTime = System.currentTimeMillis();
                    mCurrentState = STATE_START;
                    isStart = true;
                    startZoomAnim(4, 20);
                    if (mOnShutterListener != null)
                        mOnShutterListener.onStartRecord();
                }
                break;
            // 松开手时，先复位按钮初始状态，如果开始录制，则放大，否则复位
            case MotionEvent.ACTION_UP:
                mTouchEndTime = System.currentTimeMillis();
                if (mCurrentMode == MODE_CLICK_SINGLE) {
                    isStart = true;
                    mCurrentState = STATE_START;
                    //那么开启单击模式
                    startZoomAnim(4, 20);
                    if (mOnShutterListener != null)
                        mOnShutterListener.onStartRecord();
                } else {
                    if ((mTouchEndTime - mTouchStartTime) < 2000) {
                        if (mOnShutterListener != null)
                            mOnShutterListener.onShortRecord();
                    }
                    mCurrentState = STATE_IDLE;
                    stopAnimation();


                }
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    /**
     * 开始缩放动画
     *
     * @param start 起始值
     * @param end   结束值
     */
    private void startZoomAnim(int start, int end) {
        if (mButtonAnim == null || !mButtonAnim.isRunning()) {
            mButtonAnim = ValueAnimator.ofInt(start, end).setDuration(2000);
            mButtonAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mCurrentOvalWidth = (int) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mButtonAnim.setRepeatCount(ValueAnimator.INFINITE);
            mButtonAnim.start();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        switch (mCurrentState) {
            case STATE_IDLE:
                // 绘制外圆背景
                mFillPaint.setColor(mOuterOvalBgColor);
                canvas.drawCircle(mMeasuredWidth / 2, mMeasuredHeight / 2, mOuterOvalRadius, mFillPaint);
                // 绘制内圆颜色
                mFillPaint.setColor(mInerOvalBgColor);
                canvas.drawCircle(mMeasuredWidth / 2, mMeasuredHeight / 2, mInerOvalRadius, mFillPaint);
                break;
            case STATE_START:
                mStrokePaint.setStrokeWidth(mCurrentOvalWidth);
                canvas.drawCircle(mMeasuredWidth / 2, mMeasuredHeight / 2, mOuterOvalRadius - mCurrentOvalWidth / 2, mStrokePaint);
                // 绘制内圆颜色
                mFillPaint.setColor(mInerOvalBgColor);
                canvas.drawCircle(mMeasuredWidth / 2, mMeasuredHeight / 2, mInerOvalRadius, mFillPaint);
                break;
            case STATE_END:

                break;
        }

    }


    public void stopAnimation() {
        if (mButtonAnim != null && mButtonAnim.isRunning()) {
            Log.e("Harrison", "mButtonAnim");
            mButtonAnim.cancel();
        }
        if (mOnShutterListener != null) {
            mOnShutterListener.onStopRecord();
        }
        isStart = false;
        mCurrentState = STATE_IDLE;

        invalidate();
    }

    /**
     * 快门监听回调
     */
    public interface OnShutterListener {
        // 开始录制
        void onStartRecord();

        // 退出录制
        void onStopRecord();

        /**
         * 短时间录制提醒
         */
        void onShortRecord();

        void onEndRecord();

    }

    /**
     * 设置编码器处于可用状态(准备状态和释放状态都不可用)
     *
     * @param enable
     */
    public void setEnableEncoder(boolean enable) {
        mEnableEncoder = enable;
    }

    /**
     * 设置手势监听器
     *
     * @param listener
     */
    public void setOnShutterListener(ShutterViewVVVV.OnShutterListener listener) {
        mOnShutterListener = listener;
    }
}
