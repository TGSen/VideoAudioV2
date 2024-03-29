package com.cgfay.cameralibrary.media.bean;

import android.support.annotation.IntDef;

import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Harrison 唐广森
 * @description: 带有时间点的特效
 * @date :2019/4/12 15:18
 */
public class VideoEffect {
    private int startTime;
    private int endTime;
    //滤镜的索引
    private int dynamicColorId;
    private DynamicColor dynamicColor;

    public DynamicColor getDynamicColor() {
        return dynamicColor;
    }

    public VideoEffect setDynamicColor(DynamicColor dynamicColor) {
        this.dynamicColor = dynamicColor;
        return this;
    }

    private String effectName;
    //每种特效代表一种颜色
    private int resColorId;
    //是否是经过一次完整的了
    private boolean isHasAll;

    public boolean isHasAll() {
        return isHasAll;
    }

    public void setHasAll(boolean hasAll) {
        this.isHasAll = hasAll;
    }

    public static final int COLOR_RED = android.R.color.holo_red_dark;
    public static final int COLOR_GREED = android.R.color.holo_green_dark;
    public static final int COLOR_BLUE = android.R.color.holo_blue_dark;
    public static final int COLOR_WHILE = android.R.color.white;
    public static final int COLOR_BLACK = android.R.color.black;
    public static final int COLOR_DEFUALT = android.R.color.transparent;

    @IntDef({COLOR_RED, COLOR_GREED, COLOR_BLUE, COLOR_WHILE, COLOR_BLACK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EffectColor {

    }

    public int getResColorId() {
        return resColorId;
    }

    public VideoEffect setResColorId(int resColorId) {
        switch (resColorId) {
            case 0:
                this.resColorId = COLOR_WHILE;
                break;
            case 1:
                this.resColorId = COLOR_GREED;
                break;
            case 2:
                this.resColorId = COLOR_BLUE;
                break;
            case 3:
                this.resColorId = COLOR_RED;
                break;
            case 4:
                this.resColorId = COLOR_BLACK;
                break;
            default:
                this.resColorId = COLOR_DEFUALT;
        }
        return this;
    }

    public int getDynamicColorId() {
        return dynamicColorId;
    }

    public VideoEffect setDynamicColorId(int dynamicColorId) {
        this.dynamicColorId = dynamicColorId;
        return this;
    }

    public int getStartTime() {
        return startTime;
    }

    public VideoEffect setStartTime(int startTime) {
        this.startTime = startTime;
        return this;
    }

    public int getEndTime() {
        return endTime;
    }

    public VideoEffect setEndTime(int endTime) {
        this.endTime = endTime;
        return this;
    }

    public String getEffectName() {
        return effectName;
    }

    public void setEffectName(String effectName) {
        this.effectName = effectName;
    }


}
