package com.cgfay.cameralibrary.media.surface;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.cgfay.cameralibrary.media.VideoInfo;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by guoheng on 2016/8/31.
 */
public class EncodeDecodeSurface {

    private static final String TAG = "EncodeDecodeSurface";
    private static final boolean VERBOSE = false;           // lots of logging

    private int MAX_FRAMES = 400;       // stop extracting after this many

    SurfaceDecoder mDecoder = new SurfaceDecoder();
    SurfaceEncoder mEncoder = new SurfaceEncoder();

    private String videoPath;
    private String outVideoPath;

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
                mTest.Prepare();
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

    private void Prepare() throws IOException {
        try {
            //获取原视频的帧率，宽高，等信息并传递给Mediaceodec
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            File file = new File(videoPath);
            if (!file.canRead()) {
                return;
            }
            mmr.setDataSource(videoPath);
            VideoInfo videoInfo = new VideoInfo();
            String bitrate = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
            String width = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String height = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String frameCont = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT);
            videoInfo.setWidth(Integer.parseInt(width))
                    .setHeight(Integer.parseInt(height))
                    .setBitRate(Integer.parseInt(bitrate))
                    .setPath(videoPath)
                    .setOutPath(outVideoPath);
            mEncoder.setVideoInfo(videoInfo);
            mEncoder.VideoEncodePrepare();
            //将解码的surface
            mDecoder.setVideoInfo(videoInfo);
            mDecoder.SurfaceDecoderPrePare(mEncoder.getEncoderSurface());
            doExtract();
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
            if (VERBOSE) Log.d(TAG, "loop");

            // Feed more data to the decoder.
            if (!inputDone) {
                int inputBufIndex = mDecoder.decoder.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufIndex >= 0) {
                    ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
                    int chunkSize = mDecoder.extractor.readSampleData(inputBuf, 0);
                    if (chunkSize < 0) {
                        mDecoder.decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        inputDone = true;
                        if (VERBOSE) Log.d(TAG, "sent input EOS");
                    } else {
                        if (mDecoder.extractor.getSampleTrackIndex() != mDecoder.DecodetrackIndex) {
                            Log.w(TAG, "WEIRD: got sample from track " +
                                    mDecoder.extractor.getSampleTrackIndex() + ", expected " + mDecoder.DecodetrackIndex);
                        }
                        long presentationTimeUs = mDecoder.extractor.getSampleTime();
                        mDecoder.decoder.queueInputBuffer(inputBufIndex, 0, chunkSize,
                                presentationTimeUs, 0 /*flags*/);
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
                    MediaFormat newFormat = mDecoder.decoder.getOutputFormat();
                    if (VERBOSE) Log.d(TAG, "decoder output format changed: " + newFormat);
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
                        if (VERBOSE) Log.d(TAG, "awaiting decode of frame " + decodeCount);

                        if (decodeCount < MAX_FRAMES) {
                            mDecoder.outputSurface.makeCurrent(1);
                            mDecoder.outputSurface.awaitNewImage();
                            mDecoder.outputSurface.drawImage(true);

                            mEncoder.drainEncoder(false);
                            mDecoder.outputSurface.setPresentationTime(computePresentationTimeNsec(decodeCount));
                            mDecoder.outputSurface.swapBuffers();

                        }
                        decodeCount++;
                    }

                }
            }
        }

        mEncoder.drainEncoder(true);
        int numSaved = (MAX_FRAMES < decodeCount) ? MAX_FRAMES : decodeCount;
        Log.d(TAG, "Saving " + numSaved + " frames took " +
                (frameSaveTime / numSaved / 1000) + " us per frame");
    }


    private static long computePresentationTimeNsec(int frameIndex) {
        final long ONE_BILLION = 1000000000;
        return frameIndex * ONE_BILLION / 30;
    }


}

