package com.owoh.video.media.combine;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Harrison 唐广森
 * @description:
 * @date :2019/4/22 16:54
 */
public class VideoThread extends Thread implements IAudioVideo {

    private int videoTrackIndex = -1;
    private String videoPath;
    private MediaExtractor videoExtractor;
    private MediaMuxer mediaMuxer;
    private ByteBuffer byteBuffer;


    private VideoAudioCombine.VideAudioCombineListener videAudioCombineListener;
    private int writeVideoTrackIndex;
    private long duration;

    public long getDuration() {
        return duration;
    }


    public String getVideoPath() {
        return videoPath;
    }

    public VideoThread setVideoPath(String videoPath) {
        this.videoPath = videoPath;
        return this;
    }


    public VideoThread setMediaMuxer(MediaMuxer mediaMuxer) {
        this.mediaMuxer = mediaMuxer;
        return this;
    }


    @Override
    public void init1() throws IOException {
        videoExtractor = new MediaExtractor();
        videoExtractor.setDataSource(videoPath);
        int videoTrackCount = videoExtractor.getTrackCount();
        for (int i = 0; i < videoTrackCount; i++) {
            MediaFormat format = videoExtractor.getTrackFormat(i);
            String mimeType = format.getString(MediaFormat.KEY_MIME);
            if (mimeType.startsWith("video/")) {
                this.videoTrackIndex = i;
                break;
            }
        }

        MediaFormat mediaFormat = videoExtractor.getTrackFormat(videoTrackIndex);
        duration = mediaFormat.getLong(MediaFormat.KEY_DURATION);
        writeVideoTrackIndex = mediaMuxer.addTrack(mediaFormat);
        Log.e("Harrison", "writeVideoTrackIndex" + writeVideoTrackIndex + "--" + videoTrackIndex+"***"+duration);
    }

    /**
     * 合并前做些准备
     */
    @Override
    public void startCombinePrepare() {


    }

    public void prepare() throws IOException {
        init1();
        videoExtractor.selectTrack(videoTrackIndex);
        //计算sample
        byteBuffer = ByteBuffer.allocate(500 * 1024);
    }

    @Override
    public void run() {
        startCombinePrepare();
        startCombine();
    }

    @Override
    public void startCombine() {
        if (videoTrackIndex == -1 && mediaMuxer == null) {
            return;
        }
        MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
        videoExtractor.unselectTrack(videoTrackIndex);
        videoExtractor.selectTrack(videoTrackIndex);
        Log.e("Harrison", "开始合并视频");
        while (true) {
            int readVideoSampleSize = videoExtractor.readSampleData(byteBuffer, 0);
            if (readVideoSampleSize < 0) {
                break;
            }
            videoBufferInfo.size = readVideoSampleSize;
            videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
            videoBufferInfo.offset = 0;
            videoBufferInfo.flags = videoExtractor.getSampleFlags();
            mediaMuxer.writeSampleData(writeVideoTrackIndex, byteBuffer, videoBufferInfo);
            videoExtractor.advance();
        }
        endCombine();
    }

    @Override
    public void endCombine() {

        if (videAudioCombineListener != null) {
            videAudioCombineListener.combineVideoFinished();
        }
        if (videoExtractor != null) {
            videoExtractor.release();
        }
        if (byteBuffer != null) {
            byteBuffer = null;
        }
    }

    public void setCombineListener(VideoAudioCombine.VideAudioCombineListener listener) {
        this.videAudioCombineListener = listener;
    }
}
