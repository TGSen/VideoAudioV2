package com.cgfay.cameralibrary.media.surface;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.cgfay.cameralibrary.media.VideoInfo;

import java.io.File;
import java.io.IOException;

/**
 * Created by guoheng on 2016/9/1.
 */
public class SurfaceDecoder {

    private static final String TAG = "EncodeDecodeSurface";
    private static final boolean VERBOSE = false;           // lots of logging


    MediaCodec decoder = null;

    CodecOutputSurface outputSurface = null;

    MediaExtractor extractor = null;

    public int DecodetrackIndex;
    private VideoInfo mVideoInfo;

    public void setVideoInfo(VideoInfo info) {
        this.mVideoInfo = info;
    }

    void SurfaceDecoderPrePare(Surface encodersurface) {
        try {
            if (mVideoInfo == null) {
                return;
            }
            File inputFile = new File(mVideoInfo.getPath());   // must be an absolute path

            if (!inputFile.canRead()) {
                return;
            }
            extractor = new MediaExtractor();
            extractor.setDataSource(inputFile.toString());
            DecodetrackIndex = selectTrack(extractor);
            if (DecodetrackIndex < 0) {
                return;
            }
            extractor.selectTrack(DecodetrackIndex);

            MediaFormat format = extractor.getTrackFormat(DecodetrackIndex);
            outputSurface = new CodecOutputSurface(mVideoInfo.getWidth(), mVideoInfo.getHeight(), encodersurface);
            String mime = format.getString(MediaFormat.KEY_MIME);
            decoder = MediaCodec.createDecoderByType(mime);
            decoder.configure(format, outputSurface.getSurface(), null, 0);
            decoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private int selectTrack(MediaExtractor extractor) {
        // Select the first video track we find, ignore the rest.
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                if (VERBOSE) {
                    Log.d(TAG, "Extractor selected track " + i + " (" + mime + "): " + format);
                }
                return i;
            }
        }

        return -1;
    }


    void release() {
        if (decoder != null) {
            decoder.stop();
            decoder.release();
            decoder = null;
        }
        if (extractor != null) {
            extractor.release();
            extractor = null;
        }
        if (outputSurface != null) {
            outputSurface.release();
            outputSurface = null;
        }
    }
}
