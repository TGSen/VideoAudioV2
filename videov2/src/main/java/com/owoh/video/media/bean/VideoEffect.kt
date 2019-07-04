package com.owoh.video.media.bean

import android.support.annotation.IntDef

import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * @author Harrison 唐广森
 * @description: 带有时间点的特效
 * @date :2019/4/12 15:18
 */
const val COLOR_RED = android.R.color.holo_red_dark
const val COLOR_GREED = android.R.color.holo_green_dark
const val COLOR_BLUE = android.R.color.holo_blue_dark
const val COLOR_WHILE = android.R.color.white
const val COLOR_BLACK = android.R.color.black
const val COLOR_DEFUALT = android.R.color.transparent

class VideoEffect {
    private var startTime: Int = 0
    private var endTime: Int = 0
    //滤镜的索引
    private var dynamicColorId: Int = 0
    private var dynamicColor: DynamicColor? = null

    var effectName: String? = null
    //每种特效代表一种颜色
    private var resColorId: Int = 0
    //是否是经过一次完整的了
    var isHasAll: Boolean = false

    fun getDynamicColor(): DynamicColor? {
        return dynamicColor
    }

    fun setDynamicColor(dynamicColor: DynamicColor): VideoEffect {
        this.dynamicColor = dynamicColor
        return this
    }

    @IntDef(COLOR_RED, COLOR_GREED, COLOR_BLUE, COLOR_WHILE, COLOR_BLACK)
    @Retention(RetentionPolicy.SOURCE)
    annotation class EffectColor

    fun getResColorId(): Int {
        return resColorId
    }

    fun setResColorId(resColorId: Int): VideoEffect {
        this.resColorId = when (resColorId) {
            0 -> COLOR_WHILE
            1 -> COLOR_GREED
            2 -> COLOR_BLUE
            3 -> COLOR_RED
            4 -> COLOR_BLACK
            else -> COLOR_DEFUALT
        }
        return this
    }

    fun getDynamicColorId(): Int {
        return dynamicColorId
    }

    fun setDynamicColorId(dynamicColorId: Int): VideoEffect {
        this.dynamicColorId = dynamicColorId
        return this
    }

    fun getStartTime(): Int {
        return startTime
    }

    fun setStartTime(startTime: Int): VideoEffect {
        this.startTime = startTime
        return this
    }

    fun getEndTime(): Int {
        return endTime
    }

    fun setEndTime(endTime: Int): VideoEffect {
        this.endTime = endTime
        return this
    }
}

