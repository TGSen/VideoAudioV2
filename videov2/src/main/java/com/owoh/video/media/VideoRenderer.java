package com.owoh.video.media;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;
import com.owoh.video.media.bean.VideoEffectType;

import java.lang.ref.WeakReference;


/**
 * 预览渲染器,由于多个页面都需要渲染滤镜等啥的，就不做单例模式了
 */

public final class VideoRenderer {

//    private VideoRenderer() {
//    }
//
//
//    private static class RenderHolder {
//        private static VideoRenderer instance = new VideoRenderer();
//    }
//
//    public static VideoRenderer getInstance() {
//        return RenderHolder.instance;
//    }


    // 操作锁
    private final Object mSynOperation = new Object();
    // 渲染Handler
    private VideoRenderHandler mRenderHandler;
    // 渲染线程
    private VideoRenderThread mPreviewRenderThread;
    private WeakReference<SurfaceView> mWeakSurfaceView;
    /**
     * Surface回调
     */
    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (mRenderHandler != null) {
                mRenderHandler.sendMessage(mRenderHandler
                        .obtainMessage(VideoRenderHandler.MSG_SURFACE_CREATED, holder));
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            surfaceSizeChanged(width, height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mRenderHandler != null) {
                mRenderHandler.sendMessage(mRenderHandler
                        .obtainMessage(VideoRenderHandler.MSG_SURFACE_DESTROYED));
            }
        }
    };

    /**
     * 初始化渲染器
     */
    public void initRenderer(Context context, VideoEffectType effectType) {
        synchronized (mSynOperation) {
            mPreviewRenderThread = new VideoRenderThread(context, "OffSVideoRenderThread", effectType);
            mPreviewRenderThread.start();
            mRenderHandler = new VideoRenderHandler(mPreviewRenderThread);
            // 绑定Handler
            mPreviewRenderThread.setRenderHandler(mRenderHandler);
        }
    }

    /**
     * 销毁渲染器
     */
    public void destroyRenderer() {
        synchronized (mSynOperation) {
            if (mWeakSurfaceView != null) {
                mWeakSurfaceView.clear();
                mWeakSurfaceView = null;
            }
            if (mRenderHandler != null) {
                mRenderHandler.removeCallbacksAndMessages(null);
                mRenderHandler = null;
            }
            if (mPreviewRenderThread != null) {
                mPreviewRenderThread.quitSafely();
                try {
                    mPreviewRenderThread.join();
                } catch (InterruptedException e) {

                }
                mPreviewRenderThread = null;
            }
        }
    }

    /**
     * 绑定需要渲染的SurfaceView
     *
     * @param surfaceView
     */
    public void setSurfaceView(SurfaceView surfaceView) {
        mWeakSurfaceView = new WeakReference<>(surfaceView);
        surfaceView.getHolder().addCallback(mSurfaceCallback);
    }

    /**
     * 录制视频的列表
     *
     * @param path
     */
    public void setVideoPaths(String path) {
        if (mRenderHandler != null) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(VideoRenderHandler.MSG_SURFACE_SET_VIDEO_PATH, path));
        }
    }

    /**
     * 暂停播放
     */
    public void stopPlayVideo() {
        if (mRenderHandler != null) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(VideoRenderHandler.MSG_VIDEO_STATUS_STOP));
        }
    }

    public void startPlayVideo() {
        if (mRenderHandler != null) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(VideoRenderHandler.MSG_VIDEO_STATUS_PLAY));
        }
    }

    /**
     * Surface大小发生变化
     *
     * @param width
     * @param height
     */
    public void surfaceSizeChanged(int width, int height) {
        if (mRenderHandler != null) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(VideoRenderHandler.MSG_SURFACE_CHANGED, width, height));
        }
    }

    /**
     * 请求渲染
     */
    public void requestRender() {
        if (mPreviewRenderThread != null) {
            mPreviewRenderThread.requestRender();
        }
    }

    /**
     * 当分镜或者是滤镜效果设置为none 的时候就移除
     *
     * @param color
     */
    public void removeDynamic(Object color) {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(VideoRenderHandler.MSG_CHANGE_DYNAMIC_REMOVE, color));
        }
    }


    /**
     * 切换Color滤镜
     *
     * @param color
     */
    public void changeDynamicColorFilter(DynamicColor color) {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(VideoRenderHandler.MSG_CHANGE_DYNAMIC_COLOR_FILTER, color));
        }
    }

    /**
     * 切换分镜滤镜
     *
     * @param color
     */
    public void changeDynamicCameraFilter(DynamicColor color) {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(VideoRenderHandler.MSG_CHANGE_DYNAMIC_CAMERA_FILTER, color));
        }
    }


    /**
     * 切换动态资源
     *
     * @param color
     */
    public void changeDynamicResource(DynamicColor color) {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(VideoRenderHandler.MSG_CHANGE_DYNAMIC_RESOURCE, color));
        }
    }

    /**
     * 切换动态资源
     *
     * @param sticker
     */
    public void changeDynamicResource(DynamicSticker sticker) {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(VideoRenderHandler.MSG_CHANGE_DYNAMIC_RESOURCE, sticker));
        }
    }

    /**
     * 开始录制
     */
    public void startRecording() {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(VideoRenderHandler.MSG_START_RECORDING));
        }
    }

    /**
     * 停止录制
     */
    public void stopRecording() {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendEmptyMessage(VideoRenderHandler.MSG_STOP_RECORDING);
        }
    }

    /**
     * 获取视频的进度
     *
     * @return
     */
    public int getVideoProgress() {
        if (mPreviewRenderThread != null) {
            return mPreviewRenderThread.getVideoProgress();
        }
        return 0;
    }

    /**
     * 设置video 状态监听
     *
     * @param lisenter
     */
    public void setVideoPlayerStatusChangeLisenter(VideoRenderThread.VideoPlayerStatusChangeLisenter lisenter) {
        if (mPreviewRenderThread != null) {
            mPreviewRenderThread.setVideoPlayerStatusChangeLisenter(lisenter);
        }

    }

    /**
     * seekto progress
     *
     * @param progress
     */
    public void changeVideoProgress(int progress) {
        if (mPreviewRenderThread != null) {
            mPreviewRenderThread.changeVideoProgress(progress);
        }
    }

    /**
     * 设置video的播放
     */
    public void changeVideoVoice(float voice) {
        if (mPreviewRenderThread != null) {
            mPreviewRenderThread.changeVideoVoice(voice);
        }
    }

    public boolean isVideoPlay() {
        if (mPreviewRenderThread != null) {
            return mPreviewRenderThread.isVideoPlay();
        }
        return false;
    }
}
