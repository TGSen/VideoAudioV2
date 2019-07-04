package com.owoh.video.media.surface;

import android.media.MediaCodec;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.owoh.video.media.bean.VideoInfo;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by guoheng on 2016/8/31.
 */
public class EncodeDecodeSurface {

    private static final String TAG = "EncodeDecodeSurface";
    private static final boolean VERBOSE = false;           // lots of logging


    SurfaceDecoder mDecoder = new SurfaceDecoder();
    SurfaceEncoder mEncoder = new SurfaceEncoder();

    private String videoPath;
    private String outVideoPath;
    private VideoInfo videoInfo;
    private final static long ONE_BILLION = 1000000000;

    public void setVideoPath(String inputPath, String outVideoPath) {
        this.videoPath = inputPath;
        this.outVideoPath = outVideoPath;
    }

    /**
     * test entry point
     */
    public void testEncodeDecodeSurface() throws Throwable {
        EncodeDecodeSurfaceWrapper.runTest(this);
    }

    private static class EncodeDecodeSurfaceWrapper implements Runnable {
        private Throwable mThrowable;
        private EncodeDecodeSurface mTest;

        private EncodeDecodeSurfaceWrapper(EncodeDecodeSurface test) {
            mTest = test;
        }

        @Override
        public void run() {
            try {
                mTest.prepare();
            } catch (Throwable th) {
                mThrowable = th;
            }
        }

        /**
         * Entry point.
         */
        public static void runTest(EncodeDecodeSurface obj) throws Throwable {
            EncodeDecodeSurfaceWrapper wrapper = new EncodeDecodeSurfaceWrapper(obj);
            Thread th = new Thread(wrapper, "codec test");
            th.start();
            //th.join();
            if (wrapper.mThrowable != null) {
                throw wrapper.mThrowable;
            }
        }
    }

    /**
     * 本方法：最主要的是需要frameRate bitRate
     * 能获取bitRate 只有在MediaMetadataRetriever
     * 能获取frameRate 在 mediaFormat = extractor.getTrackFormat(DecodetrackIndex);
     * 所以先调用initDecodeVideoInfo，补全videoInfo 在使用
     */
    private void prepare() throws IOException {
        try {
            File file = new File(videoPath);
            if (!file.canRead()) {
                return;
            }
            //获取原视频的帧率，宽高，等信息并传递给Mediaceodec
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(videoPath);
            String bitrate = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);

            videoInfo = new VideoInfo();
            videoInfo   .setPath(videoPath);
            videoInfo  .setOutPath(outVideoPath);
            videoInfo  .setBitRate(Integer.parseInt(bitrate));
            mmr.release();
            //首先先完善videoInfo,供其他的地方使用
            mDecoder.initDecodeVideoInfo(videoInfo);
            mEncoder.setVideoInfo(videoInfo);
            mEncoder.videoEncodePrepare();
            mDecoder.surfaceDecoderPrePare(mEncoder.getEncoderSurface());
            doExtract();
        } catch (Exception e) {
            Log.e("Harrison", "*********Exception" + e.getLocalizedMessage());
        } finally {
            mDecoder.release();
            mEncoder.release();
        }
    }

    void doExtract() throws IOException {
        final int TIMEOUT_USEC = 10000;
        ByteBuffer[] decoderInputBuffers = mDecoder.decoder.getInputBuffers();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int inputChunk = 0;
        int decodeCount = 0;
        long frameSaveTime = 0;

        boolean outputDone = false;
        boolean inputDone = false;
        while (!outputDone) {
            // Feed more data to the decoder.
            if (!inputDone) {
                int inputBufIndex = mDecoder.decoder.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufIndex >= 0) {
                    ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
                    int chunkSize = mDecoder.extractor.readSampleData(inputBuf, 0);
                    if (chunkSize < 0) {
                        mDecoder.decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        //   inputDone = true;
                        if (VERBOSE) Log.d(TAG, "sent input EOS");
                    } else {
                        if (mDecoder.extractor.getSampleTrackIndex() != mDecoder.DecodetrackIndex) {
                            Log.w(TAG, "WEIRD: got sample from track " +
                                    mDecoder.extractor.getSampleTrackIndex() + ", expected " + mDecoder.DecodetrackIndex);
                        }
                        long presentationTimeUs = mDecoder.extractor.getSampleTime();
                        mDecoder.decoder.queueInputBuffer(inputBufIndex, 0, chunkSize, presentationTimeUs, 0 /*flags*/);
                        if (VERBOSE) {
                            Log.d(TAG, "submitted frame " + inputChunk + " to dec, size=" +
                                    chunkSize);
                        }
                        inputChunk++;
                        mDecoder.extractor.advance();
                    }
                } else {
                    if (VERBOSE) Log.d(TAG, "input buffer not available");
                }
            }

            if (!outputDone) {
                int decoderStatus = mDecoder.decoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
                if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    if (VERBOSE) Log.d(TAG, "no output from decoder available");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // not important for us, since we're using Surface
                    if (VERBOSE) Log.d(TAG, "decoder output buffers changed");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                    MediaFormat newFormat = mDecoder.decoder.getOutputFormat();
//                    if (VERBOSE) Log.d(TAG, "decoder output format changed: " + newFormat);
                } else if (decoderStatus < 0) {

                } else { // decoderStatus >= 0
                    if (VERBOSE) Log.d(TAG, "surface decoder given buffer " + decoderStatus +
                            " (size=" + info.size + ")");
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        if (VERBOSE) Log.d(TAG, "output EOS");
                        outputDone = true;
                    }

                    boolean doRender = (info.size != 0);
                    mDecoder.decoder.releaseOutputBuffer(decoderStatus, doRender);
                    if (doRender) {
                        //
                        mDecoder.outputSurface.makeCurrent(1);
                        mDecoder.outputSurface.awaitNewImage();
                        mDecoder.outputSurface.drawImage(true);

                        mEncoder.drainEncoder(false);
                        mDecoder.outputSurface.setPresentationTime(computePresentationTimeNsec(decodeCount));
                        mDecoder.outputSurface.swapBuffers();
                        decodeCount++;
                        Log.e("Harrison", "decodeCont" + decodeCount);
                    }

                }
            }
        }

        mEncoder.drainEncoder(true);
    }

    //30 18 是该视频的fps,得获取该视频的帧率才行，否则最终MP4的时长是不对的
    private long computePresentationTimeNsec(int frameIndex) {

        int frameRate = 25;
        if (videoInfo != null && videoInfo.getFrameRate() > 0) {
            frameRate = videoInfo.getFrameRate();
        }
        return frameIndex * ONE_BILLION / frameRate;
    }


}

