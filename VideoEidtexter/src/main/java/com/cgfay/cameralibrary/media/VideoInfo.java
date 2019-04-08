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
    public int frameCount;// 帧的数量

    public int getFrameCount() {
        return frameCount;
    }

    public VideoInfo setFrameCount(int frameCount) {
        this.frameCount = frameCount;
        return this;
    }

    public String getOutPath() {
        return outPath;
    }

    public VideoInfo setOutPath(String outPath) {
        this.outPath = outPath;
        return this;
    }

    public String getPath() {
        return path;
    }

    public VideoInfo setPath(String path) {
        this.path = path;
        return this;
    }

    public int getRotation() {
        return rotation;
    }

    public VideoInfo setRotation(int rotation) {
        this.rotation = rotation;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public VideoInfo setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public VideoInfo setHeight(int height) {
        this.height = height;
        return this;
    }

    public int getBitRate() {
        return bitRate;
    }

    public VideoInfo setBitRate(int bitRate) {
        this.bitRate = bitRate;
        return this;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public VideoInfo setFrameRate(int frameRate) {
        this.frameRate = frameRate;
        return this;
    }

    public int getFrameInterval() {
        return frameInterval;
    }

    public VideoInfo setFrameInterval(int frameInterval) {
        this.frameInterval = frameInterval;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public VideoInfo setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public int getExpWidth() {
        return expWidth;
    }

    public VideoInfo setExpWidth(int expWidth) {
        this.expWidth = expWidth;
        return this;
    }

    public int getExpHeight() {
        return expHeight;
    }

    public VideoInfo setExpHeight(int expHeight) {
        this.expHeight = expHeight;
        return this;
    }

    public int getCutPoint() {
        return cutPoint;
    }

    public VideoInfo setCutPoint(int cutPoint) {
        this.cutPoint = cutPoint;
        return this;
    }

    public int getCutDuration() {
        return cutDuration;
    }

    public VideoInfo setCutDuration(int cutDuration) {
        this.cutDuration = cutDuration;
        return this;
    }
}
