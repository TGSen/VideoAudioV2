package com.owoh.video.media.surface;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;

import com.owoh.video.media.bean.VideoInfo;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by guoheng on 2016/9/1.
 */
public class SurfaceEncoder {

    private static final String TAG = "EncodeDecodeSurface";
    private static final boolean VERBOSE = false;           // lots of logging
    private static final String MIME_TYPE = "com/owoh/video/avc";    // H.264 Advanced Video Coding

    private static final int IFRAME_INTERVAL = 1;          // 10 seconds between I-frames

    MediaCodec encoder = null;
    Surface encodesurface;
    private MediaCodec.BufferInfo mBufferInfo;
    public MediaMuxer mMuxer;

    public int mTrackIndex;
    public boolean mMuxerStarted;
    private VideoInfo mVideoInfo;

    public void setVideoInfo(VideoInfo videoInfo) {
        this.mVideoInfo = videoInfo;
    }


    public void videoEncodePrepare() {
        if (mVideoInfo == null) {
            return;
        }
        mBufferInfo = new MediaCodec.BufferInfo();

        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mVideoInfo.getWidth(), mVideoInfo.getHeight());

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, mVideoInfo.getFrameRate());
        Log.e("Harrison", "帧率：" + mVideoInfo.getFrameRate());
        if (mVideoInfo.getBitRate() > 0) {
            format.setInteger(MediaFormat.KEY_BIT_RATE, mVideoInfo.getBitRate());
        } else {
            format.setInteger(MediaFormat.KEY_BIT_RATE, calcBitRate(mVideoInfo.getBitRate(), mVideoInfo.getWidth(), mVideoInfo.getHeight()));
        }
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);


        encoder = null;
        try {
            encoder = MediaCodec.createEncoderByType(MIME_TYPE);
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            encodesurface = encoder.createInputSurface();
            encoder.start();

            mMuxer = new MediaMuxer(mVideoInfo.getOutPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

        } catch (IOException ioe) {
            Log.e("Harrison", "IOException:" + ioe.getLocalizedMessage());
        }

        mTrackIndex = -1;
        mMuxerStarted = false;

    }

    private int calcBitRate(int frameRate, int width, int height) {
        return (int) (0.25f * frameRate * width * height) * 2;
    }


    public void drainEncoder(boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;
        if (VERBOSE) Log.d(TAG, "drainEncoder(" + endOfStream + ")");

        if (endOfStream) {
            if (VERBOSE) Log.d(TAG, "sending EOS to encoder");
            encoder.signalEndOfInputStream();
        }

        ByteBuffer[] encoderOutputBuffers = encoder.getOutputBuffers();
        while (true) {
            int encoderStatus = encoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    break;      // out of while
                } else {
                    if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = encoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = encoder.getOutputFormat();
                if (newFormat == null) {
                    Log.e("Harrison", "newFormat ==null");
                } else {
                    // now that we have the Magic Goodies, start the muxer
                    mTrackIndex = mMuxer.addTrack(newFormat);
                }
                mMuxer.start();
                mMuxerStarted = true;
            } else if (encoderStatus < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
                // let's ignore it
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    if (VERBOSE) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");

                    MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mVideoInfo.getWidth(), mVideoInfo.getHeight());
                    format.setByteBuffer("csd-0", encodedData);

                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }

                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);

                    mMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
                    if (VERBOSE) Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer");
                }

                encoder.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.w(TAG, "reached end of stream unexpectedly");
                    } else {
                        if (VERBOSE) Log.d(TAG, "end of stream reached");
                    }
                    break;      // out of while
                }
            }
        }
    }


    void release() {
        if (encoder != null) {
            encoder.stop();
            encoder.release();
        }
        if (mMuxer != null) {
            mMuxer.stop();
            mMuxer.release();
            mMuxer = null;
        }
    }


    Surface getEncoderSurface() {
        return encodesurface;
    }


}
