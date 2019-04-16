package com.cgfay.cameralibrary.media.bean;

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

    private String effectName;

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
