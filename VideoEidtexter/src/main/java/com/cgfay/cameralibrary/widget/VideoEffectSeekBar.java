package com.cgfay.cameralibrary.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;

import com.cgfay.cameralibrary.media.bean.VideoEffect;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Harrison 唐广森
 * @description:
 * @date :2019/4/17 10:45
 */


public class VideoEffectSeekBar extends AppCompatSeekBar {

    private Paint mRulerPaint;



    public VideoEffectSeekBar(Context context) {
        super(context);
        init();
    }

    public VideoEffectSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoEffectSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        //创建绘制刻度线的画笔
        mRulerPaint = new Paint();
        mRulerPaint.setAntiAlias(true);
        //Api21及以上调用，去掉滑块后面的背景
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSplitTrack(false);
        }
    }

    List<ItemRect> mRects = new ArrayList<>();

    private class ItemRect {
        private int colorId;
        private Rect rect;

        public int getColorId() {
            return colorId;
        }

        public void setColorId(int colorId) {
            this.colorId = colorId;
        }

        public Rect getRect() {
            return rect;
        }

        public void setRect(Rect rect) {
            this.rect = rect;
        }
    }

    //设置分段的颜色
    public void setPathList(List<VideoEffect> effects,int max) {
        mRects.clear();
        if (effects == null) return;
        int size = effects.size();
        for (int i = 0; i < size; i++) {
            VideoEffect effect = effects.get(i);
            ItemRect itemRect = new ItemRect();
            Rect rect = new Rect();
            rect.left = effect.getStartTime()*getWidth()/max;
            rect.right = effect.getEndTime()*getWidth()/max;
            rect.top = 0;
            rect.bottom = getHeight();
            itemRect.setRect(rect);
            itemRect.setColorId(effect.getResColorId());
            mRects.add(itemRect);
        }

        postInvalidate();
    }

    /**
     * 重写onDraw方法绘制刻度线
     *
     * @param canvas
     */

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
       if(mRects==null|| mRects.size()<=0)return;
       int size = mRects.size();
        //绘制刻度线
        for (int i = 1; i <= size; i++) {
            ItemRect itemRect = mRects.get(i);
            if(itemRect.getRect()!=null){
                mRulerPaint.setColor(ContextCompat.getColor(getContext(),itemRect.getColorId()));
                //进行绘制
                canvas.drawRect(itemRect.getRect(), mRulerPaint);
            }

        }
    }

}
