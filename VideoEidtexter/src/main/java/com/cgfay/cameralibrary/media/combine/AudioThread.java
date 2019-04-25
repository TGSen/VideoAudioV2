package com.cgfay.cameralibrary.media.combine;

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
public class AudioThread extends Thread implements IAudioVideo {
    private int audioTrackIndex = -1;
    private String audioPath;
    private MediaExtractor audioExtractor;
    private MediaMuxer mediaMuxer;

    //视频的时间
    private long mVideoTime;
    //音频背景的循环次数
    private int mAudioCount;
    //音频的截取最后时间
    private long mAudioEndTime;
    private long mAudioTime;

    public long getVideoTime() {
        return mVideoTime;
    }

    public void setVideoTime(long mVideoTime) {
        this.mVideoTime = mVideoTime;
    }


    private VideoAudioCombine.VideAudioCombineListener videAudioCombineListener;
    private int writeAudioTrackIndex;


    public String getAudioPath() {
        return audioPath;
    }

    public AudioThread setAudioPath(String audioPath) {
        this.audioPath = audioPath;
        return this;
    }


    public MediaMuxer getMediaMuxer() {
        return mediaMuxer;
    }

    public AudioThread setMediaMuxer(MediaMuxer mediaMuxer) {
        this.mediaMuxer = mediaMuxer;
        return this;
    }


    @Override
    public void init() throws IOException {
        audioExtractor = new MediaExtractor();
        Log.e("Harrison", "audio path:" + audioPath);
        audioExtractor.setDataSource(audioPath);
        int audioTrackCount = audioExtractor.getTrackCount();
        for (int i = 0; i < audioTrackCount; i++) {
            MediaFormat format = audioExtractor.getTrackFormat(i);
            String mimeType = format.getString(MediaFormat.KEY_MIME);
            Log.e("Harrison", "mimeType:" + mimeType);
            if (mimeType.startsWith("audio/")) {
                audioTrackIndex = i;
                break;
            }
        }
        MediaFormat mediaFormat = audioExtractor.getTrackFormat(audioTrackIndex);
        mAudioTime = mediaFormat.getLong(MediaFormat.KEY_DURATION);
        /**
         * 目前不知道mp3 中的mpeg  ,测试AAC 是可行的
         */
        if (audioTrackIndex >= 0) {
            writeAudioTrackIndex = mediaMuxer.addTrack(mediaFormat);
            Log.e("Harrison", "writeAudioTrackIndex" + writeAudioTrackIndex + "--" + writeAudioTrackIndex);
        } else {
            Log.e("Harrison", "oooo writeAudioTrackIndex" + writeAudioTrackIndex + "--" + writeAudioTrackIndex);
        }
    }

    /**
     * 开始合并音频，如果音频时间>视频时间，那么采用截取的（0，videoTime）
     * 如果是audioTime<videoTime 那么采取音频循环，循环(videoTime/audioTime)+1次，最后一次是videoTime%audioTime
     */
    @Override
    public void startCombinePrepare() {

        if (mAudioTime >= mVideoTime) {
            mAudioCount = 1;
            mAudioEndTime = mVideoTime;
        } else {
            mAudioCount = (int) ((mVideoTime / mAudioTime) + 1);
            mAudioEndTime = mVideoTime % mAudioTime;
        }

        Log.e("Harrison", "mAudioEndTime" + mAudioEndTime + "--mAudioCount" + mAudioCount + "**mAudioTime:" + mAudioTime);

    }

    public void prepare() throws IOException {
        init();
    }

    @Override
    public void run() {
        startCombinePrepare();
        startCombine();
    }

    /**
     * 开始合并音频
     */

//    @Override
//    public void startCombine() {
//        audioExtractor.selectTrack(audioTrackIndex);
//        MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
//        ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
//        while (true) {
//            int readAudioSampleSize = audioExtractor.readSampleData(byteBuffer, 0);
//            if (readAudioSampleSize < 0) {
//                break;
//            }
//            audioBufferInfo.size = readAudioSampleSize;
//            audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
//            audioBufferInfo.offset = 0;
//            audioBufferInfo.flags = audioExtractor.getSampleFlags();
//            mediaMuxer.writeSampleData(writeAudioTrackIndex, byteBuffer, audioBufferInfo);
//            audioExtractor.advance();
//        }
//        endCombine();
//    }
    @Override
    public void startCombine() {
        audioExtractor.selectTrack(audioTrackIndex);
        MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer byteBuffer = ByteBuffer.allocate(200 * 1024);
//        mAudioCount = 1;
        int currentAudio = 0;
        while (mAudioCount > 0) {
            //需要重置一下,要不第二次以后无效
            audioExtractor.unselectTrack(audioTrackIndex);
            audioExtractor.selectTrack(audioTrackIndex);
            //可以使用这个方式
//            audioExtractor.seekTo(0,MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
            //开始循环次数
            while (true) {
                int readAudioSampleSize = audioExtractor.readSampleData(byteBuffer, 0);
                if (readAudioSampleSize < 0) {
                    break;
                }
                //如果是最后一次
                if (mAudioCount == 1) {
                    Log.e("Harrison", "最后一次" + mAudioCount);
                    //截取的时间
                    long timeStamp = audioExtractor.getSampleTime();
                    if (timeStamp > mAudioEndTime && timeStamp - mAudioEndTime >= 0) {
                        break;
                    }
                } else {
                    Log.e("Harrison", "第几次：" + mAudioCount);
                }

                audioBufferInfo.size = readAudioSampleSize;
                audioBufferInfo.presentationTimeUs = currentAudio * mAudioTime + audioExtractor.getSampleTime();
                audioBufferInfo.offset = 0;
                audioBufferInfo.flags = audioExtractor.getSampleFlags();
                mediaMuxer.writeSampleData(writeAudioTrackIndex, byteBuffer, audioBufferInfo);
                audioExtractor.advance();
            }
            currentAudio++;
            mAudioCount--;
        }

        endCombine();
    }


    public void setCombineListener(VideoAudioCombine.VideAudioCombineListener listener) {
        this.videAudioCombineListener = listener;
    }

    @Override
    public void endCombine() {

        if (videAudioCombineListener != null) {
            videAudioCombineListener.combineAudioFinished();
        }

        if (audioExtractor != null) {
            audioExtractor.release();
        }
    }
}
