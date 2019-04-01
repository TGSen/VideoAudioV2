package com.cgfay.cameralibrary.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;

import com.cgfay.cameralibrary.engine.camera.CameraParam;
import com.cgfay.cameralibrary.engine.render.PreviewRenderer;
import com.cgfay.cameralibrary.media.MediaPlayerWrapper;
import com.cgfay.cameralibrary.media.VideoInfo;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;


import java.io.IOException;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * desc: 播放视频的view 单个视频循环播放
 */

public class VideoPreviewView extends SurfaceView implements MediaPlayerWrapper.IMediaCallback {
    private MediaPlayerWrapper mMediaPlayer;
    // private VideoDrawer mDrawer;

    /**
     * 视频播放状态的回调
     */
    private MediaPlayerWrapper.IMediaCallback callback;

    public VideoPreviewView(Context context) {
        super(context, null);
        init(context);
    }

    public VideoPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        //初始化Drawer和VideoPlayer
        mMediaPlayer = new MediaPlayerWrapper();
        mMediaPlayer.setOnCompletionListener(this);
    }

    /**
     * 设置视频的播放地址
     */
    public void setVideoPath(List<String> paths) {
        mMediaPlayer.setDataSource(paths);
    }

//    @Override
//    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//        int mInputTexture = OpenGLUtils.createOESTexture();
//        SurfaceTexture surfaceTexture = new SurfaceTexture(mInputTexture);
//        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
//            @Override
//            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//                requestRender();
//            }
//        });
//        Surface surface = new Surface(surfaceTexture);
//        mMediaPlayer.setSurface(surface);
//        try {
//            mMediaPlayer.prepare();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        mMediaPlayer.start();
//    }



    public void onDestroy() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.release();
    }



    @Override
    public void onVideoPrepare() {
        if (callback != null) {
            callback.onVideoPrepare();
        }
    }

    @Override
    public void onVideoStart() {
        if (callback != null) {
            callback.onVideoStart();
        }
    }

    @Override
    public void onVideoPause() {
        if (callback != null) {
            callback.onVideoPause();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (callback != null) {
            callback.onCompletion(mp);
        }
    }

    @Override
    public void onVideoChanged(final VideoInfo info) {

        if (callback != null) {
            callback.onVideoChanged(info);
        }
    }

    /**
     * isPlaying now
     */
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    /**
     * pause play
     */
    public void pause() {
        mMediaPlayer.pause();
    }

    /**
     * start play video
     */
    public void start() {
        mMediaPlayer.start();
    }

    /**
     * 跳转到指定的时间点，只能跳到关键帧
     */
    public void seekTo(int time) {
        mMediaPlayer.seekTo(time);
    }

    /**
     * 获取当前视频的长度
     */
    public int getVideoDuration() {
        return mMediaPlayer.getCurVideoDuration();
    }

    /**
     * 获取当前播放的视频的列表
     */
    public List<VideoInfo> getVideoInfo() {
        return mMediaPlayer.getVideoInfo();
    }


    public void setIMediaCallback(MediaPlayerWrapper.IMediaCallback callback) {
        this.callback = callback;
    }
}
