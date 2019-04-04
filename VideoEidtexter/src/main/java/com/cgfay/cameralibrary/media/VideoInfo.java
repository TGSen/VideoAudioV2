package com.cgfay.cameralibrary.media;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/6/29 0029.
 * 视频的信息bean
 */

public class VideoInfo implements Serializable {
    public String path;//路径
    public String outPath;
    public int rotation;//旋转角度
    public int width;//宽
    public int height;//高
    public int bitRate;//比特率
    public int frameRate;//帧率
    public int frameInterval;//关键帧间隔
    public int duration;//时长

    public int expWidth;//期望宽度
    public int expHeight;//期望高度
    public int cutPoint;//剪切的开始点
    public int cutDuration;//剪切的时长

    public String getOutPath() {
        return outPath;
    }

    public void setOutPath(String outPath) {
        this.outPath = outPath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getBitRate() {
        return bitRate;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public int getFrameInterval() {
        return frameInterval;
    }

    public void setFrameInterval(int frameInterval) {
        this.frameInterval = frameInterval;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getExpWidth() {
        return expWidth;
    }

    public void setExpWidth(int expWidth) {
        this.expWidth = expWidth;
    }

    public int getExpHeight() {
        return expHeight;
    }

    public void setExpHeight(int expHeight) {
        this.expHeight = expHeight;
    }

    public int getCutPoint() {
        return cutPoint;
    }

    public void setCutPoint(int cutPoint) {
        this.cutPoint = cutPoint;
    }

    public int getCutDuration() {
        return cutDuration;
    }

    public void setCutDuration(int cutDuration) {
        this.cutDuration = cutDuration;
    }
}
