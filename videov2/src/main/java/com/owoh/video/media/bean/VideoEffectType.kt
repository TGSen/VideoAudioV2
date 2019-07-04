package com.owoh.video.media.bean

import android.support.annotation.IntDef

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * @author Harrison 唐广森
 * @description:
 * @date :2019/4/12 10:15
 */

const val RENDER_TYPE_CONTINUED = 0x001//渲染模式是持续的
const val RENDER_TYPE_AT_TIME = 0x002//渲染模式是
const val EFFECT_TYPE_SINGLE = 0x001 //特效模式是单独的，也就是Fileter,color filter,贴纸等，每种都是单一的,类似每种自己切换
const val EFFECT_TYPE_MULTI = 0x002//特效模式是多种的，也就是Fileter,color filter,贴纸等，每种都可以叠加
class VideoEffectType {
    //间隔多长时间渲染一次
    private var duration: Int = 0
    private var currentRendererType: Int = 0
    private var currentEffectType: Int = 0


    @IntDef(RENDER_TYPE_CONTINUED, RENDER_TYPE_AT_TIME)
    @Retention(RetentionPolicy.SOURCE)
    annotation class EffectRenderType


    @IntDef(EFFECT_TYPE_SINGLE, EFFECT_TYPE_MULTI)
    @Retention(RetentionPolicy.SOURCE)
    annotation class EffectType

    fun getDuration(): Int {
        return duration
    }

    fun setDuration(duration: Int): VideoEffectType {
        this.duration = duration
        return this
    }

    fun getCurrentRendererType(): Int {
        return currentRendererType
    }

    fun setCurrentRendererType(currentRendererType: Int): VideoEffectType {
        this.currentRendererType = currentRendererType
        return this
    }

    fun getCurrentEffectType(): Int {
        return currentEffectType
    }

    fun setCurrentEffectType(currentEffectType: Int): VideoEffectType {
        this.currentEffectType = currentEffectType
        return this
    }


}
