package com.cgfay.cameralibrary.media.bean;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Harrison 唐广森
 * @description:
 * @date :2019/4/12 10:15
 */
public class VideoEffectType {
    //渲染模式是持续的
    public static final int RENDER_TYPE_CONTINUED = 0x001;
    //渲染模式是
    public static final int RENDER_TYPE_AT_TIME = 0x002;


    @IntDef({RENDER_TYPE_CONTINUED, RENDER_TYPE_AT_TIME})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EffectRenderType {

    }
    //特效模式是单独的，也就是Fileter,color filter,贴纸等，每种都是单一的,类似每种自己切换

    public static final int EFFECT_TYPE_SINGLE= 0x001;
    //特效模式是多种的，也就是Fileter,color filter,贴纸等，每种都可以叠加
    public static final int EFFECT_TYPE_MULTI = 0x002;


    @IntDef({EFFECT_TYPE_SINGLE, EFFECT_TYPE_MULTI})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EffectType {

    }
    //间隔多长时间渲染一次
    private int duration;

    private int currentRendererType;
    private int currentEffectType;

    public int getDuration() {
        return duration;
    }

    public VideoEffectType setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public int getCurrentRendererType() {
        return currentRendererType;
    }

    public VideoEffectType setCurrentRendererType(int currentRendererType) {
        this.currentRendererType = currentRendererType;
        return this;
    }

    public int getCurrentEffectType() {
        return currentEffectType;
    }

    public VideoEffectType setCurrentEffectType(int currentEffectType) {
        this.currentEffectType = currentEffectType;
        return this;
    }
}
