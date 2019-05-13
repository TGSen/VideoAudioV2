package com.cgfay.cameralibrary.media.surface;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.cgfay.cameralibrary.media.bean.VideoInfo;

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
    private MediaFormat mediaFormat;


    /**
     * 首先将本地的Mp4 经过MediaExtractor 处理后，会有比如frameRate（貌似只有这个api 可以获取到） ,bitRate 这些
     */
    public void initDecodeVideoInfo(VideoInfo info) {
        try {
            if (info == null) {
                Log.e("Harrison", "mVideoInfo ==null");
                return;
            }
            this.mVideoInfo = info;
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

            mediaFormat = extractor.getTrackFormat(DecodetrackIndex);
            //在这里设置，因为只有这里才可以获取
            mVideoInfo.setFrameRate(mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE))
                    .setWidth(mediaFormat.getInteger(MediaFormat.KEY_WIDTH))
                    .setHeight(mediaFormat.getInteger(MediaFormat.KEY_HEIGHT));

            Log.e("Harrison", "mediaformat" + mediaFormat.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param encodersurface
     */
    void surfaceDecoderPrePare(Surface encodersurface) {
        try {
            outputSurface = new CodecOutputSurface(mVideoInfo.getWidth(), mVideoInfo.getHeight(), encodersurface);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            decoder = MediaCodec.createDecoderByType(mime);
            decoder.configure(mediaFormat, outputSurface.getSurface(), null, 0);
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
