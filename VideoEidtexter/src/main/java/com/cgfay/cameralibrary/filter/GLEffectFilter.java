package com.cgfay.cameralibrary.filter;

import android.text.TextUtils;
import android.util.Log;

import com.cgfay.cameralibrary.media.bean.VideoEffect;
import com.cgfay.cameralibrary.mp4compose.filter.GlFilter;
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 这个是从别的库的特效转移到该库
 */

public class GLEffectFilter extends GlFilter {
    //    private List<ItemEffect> itemEffectList = new ArrayList<>();
//当前时间
    private double currentTime;
    private int currentVideoEffectIndex;

    public double getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(double currentTime) {
        this.currentTime = currentTime;
    }

    private List<VideoEffect> mVideoEffects = new ArrayList<>();




    public void setFilters(List<VideoEffect> list) {
        mVideoEffects.clear();
        mVideoEffects.addAll(list);
    }

    @Override
    protected void onDraw() {
        super.onDraw();
        for (int i = 0; i < mVideoEffects.size(); i++) {
            if ((mVideoEffects.get(i).getStartTime() < mVideoEffects.get(i).getEndTime() &&
                    currentTime >= mVideoEffects.get(i).getStartTime() && currentTime < mVideoEffects.get(i).getEndTime()) ||
                    (mVideoEffects.get(i).getStartTime() > mVideoEffects.get(i).getEndTime() &&
                            (currentTime >= mVideoEffects.get(i).getStartTime() || currentTime < mVideoEffects.get(i).getEndTime()))) {
                //在添加
                currentVideoEffectIndex = i;
                VideoEffect videoEffect = mVideoEffects.get(i);
                DynamicColor color = videoEffect.getDynamicColor();
                String fs = OpenGLUtils.getShaderFromFile(color.filterList.get(0).getFsPath());
                Log.e("Harrison","************"+fs);
                if (!TextUtils.isEmpty(fs)) {

                    setFragmentShaderSource(fs);
                    setup();
                } else {
                    Log.e("Harrison","************resetVsFsAndSetUp");
                    super.resetVsFsAndSetUp();
                }
                Log.e("Harrison", "已改变特效" + currentVideoEffectIndex);
                return;
            } else {
                //判断之前有没使用过特效
                if (currentVideoEffectIndex != -1) {
                    VideoEffect videoEffectRemove = mVideoEffects.get(currentVideoEffectIndex);
                    super.resetVsFsAndSetUp();
                    Log.e("Harrison", "clear特效" + currentVideoEffectIndex);
                    currentVideoEffectIndex = -1;
                    return;

                }
            }
        }
    }

    //    private void addEffect(ItemEffect effect) {
//        if (effect != null) {
//            itemEffectList.add(effect);
//        }
//    }


}
