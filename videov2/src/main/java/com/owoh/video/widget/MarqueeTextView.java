package com.owoh.video.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.cgfay.filterlibrary.utils.DensityUtils;

/**
 * Created by xRoon on 2016/2/27.
 */
public class MarqueeTextView extends View {
    private final float DEF_TEXT_SIZE = 25.0F;//The default text size
    private float mSpeed = 3.0F; //The default text scroll speed
    private boolean isScroll = true; //The default set as auto scroll
    private Context mContext;
    private Paint mPaint;
    private String mText;//This is to display the content
    private float mTextSize;//This is text size
    private int mTextColor; //This is text color

    private float mCoordinateX;//Draw the starting point of the X coordinate
    private float mCoordinateY;//Draw the starting point of the Y coordinate
    private float mTextWidth; //This is text width
    private int mViewWidth; //This is View width

    public MarqueeTextView(Context context) {
        super(context);
        init(context);

    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);

    }

    /**
     * Initializes the related parameters
     *
     * @param context
     */
    private void init(Context context) {
        this.mContext = context;

        if (TextUtils.isEmpty(mText)) {
            mText = "";
        }
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(DEF_TEXT_SIZE);
    }


    public void setText(String text) {
        mText = text;
        if (TextUtils.isEmpty(mText)) {
            mText = "";
        }
        requestLayout();
        invalidate();
    }

    /**
     * Set the text size, if this value is < 0, set to the default size
     *
     * @param textSize
     */
    public void setTextSize(float textSize) {

        this.mTextSize = textSize;
        mPaint.setTextSize(mTextSize <= 0 ? DEF_TEXT_SIZE : mTextSize);
        requestLayout();
        invalidate();
    }

    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
        mPaint.setColor(mTextColor);
        invalidate();
    }

    /**
     * Set the text scrolling speed, if the value < 0, set to the default is 0
     *
     * @param speed If this value is 0, then stop scrolling
     */
    public void setTextSpeed(float speed) {
        this.mSpeed = speed < 0 ? 0 : speed;
        invalidate();
    }

    public float getTextSpeed() {
        return mSpeed;
    }

    public void setScroll(boolean isScroll) {
        this.isScroll = isScroll;
        invalidate();
    }

    public boolean isScroll() {
        return isScroll;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mTextWidth = mPaint.measureText(mText);
        mCoordinateX = getPaddingLeft();
        mCoordinateY = getPaddingTop() + Math.abs(mPaint.ascent());
        mViewWidth = measureWidth(widthMeasureSpec);
        int mViewHeight = measureHeight(heightMeasureSpec);

        //If you do not call this method, will be thrown "IllegalStateException"
        setMeasuredDimension(mViewWidth, mViewHeight);
    }


    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = (int) mPaint.measureText(mText) + getPaddingLeft()
                    + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = (int) mPaint.getTextSize() + getPaddingTop()
                    + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawText(mText, mCoordinateX, mCoordinateY, mPaint);

        if (!isScroll) {
            return;
        }

        mCoordinateX -= mSpeed;

        if (Math.abs(mCoordinateX) > mTextWidth && mCoordinateX < 0) {
            mCoordinateX = mViewWidth;
        }

        invalidate();

    }

}
