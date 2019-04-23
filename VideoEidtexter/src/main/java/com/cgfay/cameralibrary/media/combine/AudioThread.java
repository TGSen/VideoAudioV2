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
        audioExtractor.setDataSource(audioPath);
        int audioTrackCount = audioExtractor.getTrackCount();
        for (int i = 0; i < audioTrackCount; i++) {
            MediaFormat format = audioExtractor.getTrackFormat(i);
            String mimeType = format.getString(MediaFormat.KEY_MIME);
            if (mimeType.startsWith("audio/")) {
                audioTrackIndex = i;
                break;
            }
        }

        writeAudioTrackIndex = mediaMuxer.addTrack(audioExtractor.getTrackFormat(audioTrackIndex));
        Log.e("Harrison","writeAudioTrackIndex"+writeAudioTrackIndex+"--"+writeAudioTrackIndex);
    }

    public void prepare() throws IOException {
        init();
    }

    @Override
    public void run() {
        startCombine();
    }

    @Override
    public void startCombine() {
        audioExtractor.selectTrack(audioTrackIndex);

        MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
        while (true) {
            int readAudioSampleSize = audioExtractor.readSampleData(byteBuffer, 0);
            if (readAudioSampleSize < 0) {
                break;
            }
            audioBufferInfo.size = readAudioSampleSize;
            audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime() ;
            audioBufferInfo.offset = 0;
            audioBufferInfo.flags = audioExtractor.getSampleFlags();
            mediaMuxer.writeSampleData(writeAudioTrackIndex, byteBuffer, audioBufferInfo);
            audioExtractor.advance();
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
