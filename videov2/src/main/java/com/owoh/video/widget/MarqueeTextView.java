package com.owoh.video.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class MarqueeTextView extends AppCompatTextView {

    public static final String TAG = "MarqueeTextView";

    /**
     * 字幕滚动的速度 0:慢, 1:普通, 2:快
     */
    public static final int SCROLL_SLOW = 0;
    public static final int SCROLL_NORM = 1;
    public static final int SCROLL_FAST = 2;
    /**
     * 字幕滚动的方向 0:左往右, 1:右往左
     */


    /**
     * 文字的横坐标偏移量
     */
    private float offX = 0f;
    /**
     * 默认的移动速度
     */
    private float mStep = 2f;
    /**
     * 画笔
     */
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * 文本长度
     */
    private float textLength = 0f;
    /**
     * view 的宽度
     */
    private float viewWidth = 0f;
    /**
     * 文字的纵坐标
     */
    private float y = 0f;
    /**
     * 文本和view的长度之和
     */
    private float viewTextWidth = 0.0f;
    /**
     * 滚动文本
     */
    private String text = "";

    public MarqueeTextView(Context context) {
        this(context, null);
        setSingleLine(true);

    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        setSingleLine(true);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSingleLine(true);
    }

    /**
     * 初始化
     */
    public void init() {
        mPaint = getPaint();
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(sp2px(getContext(), 16));

        text = getText().toString();
        textLength = mPaint.measureText(text);

        viewWidth = viewWidth - getLeft() - getLeft();
        // 滚动文本的长度: view 长度 + 文本滑出屏幕的长度
        viewTextWidth = viewWidth + textLength;
        // 滚动文本的绘制起始 y 坐标
        y = getTextSize() + getPaddingTop();

    }

    private int drawableLeft = 0;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
        Drawable[] drawables = getCompoundDrawables();
        if (drawables != null && drawables.length > 0) {
            Drawable drawable = drawables[0];
            drawableLeft = drawable.getIntrinsicWidth();
        }
        setText("");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (offX <= viewTextWidth) {
            /**
             * viewWidth - offX： x 坐标逐渐变小,往左移动
             */
            canvas.save();
            canvas.translate(drawableLeft, 0);
            canvas.drawText(text, viewWidth - offX, y, mPaint);
            canvas.restore();


        } else {
            offX = viewWidth;
        }


        // 滚动文本的偏移量
        offX += mStep;

        // 重绘
        invalidate();
    }

    /**
     * 设置字幕滚动的速度
     *
     * @param scrollMod
     */
    public void setScrollMode(int scrollMod) {
        if (scrollMod == SCROLL_SLOW) {
            mStep = 1f;
        } else if (scrollMod == SCROLL_NORM) {
            mStep = 2f;
        } else {
            mStep = 3f;
        }
    }


    /**
     * 将sp值转换为px值
     *
     * @param context
     * @param spValue
     * @return
     */
    private int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}