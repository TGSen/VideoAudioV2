package com.cgfay.cameralibrary.media.bean;

/**
 * @author Harrison 唐广森
 * @description: 带有时间点的特效
 * @date :2019/4/12 15:18
 */
public class VideoEffect {
    private int startTime;

    private int endTime;

    private String effectName;

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public String getEffectName() {
        return effectName;
    }

    public void setEffectName(String effectName) {
        this.effectName = effectName;
    }
}
