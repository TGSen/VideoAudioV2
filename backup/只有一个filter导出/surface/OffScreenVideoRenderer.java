package com.cgfay.cameralibrary.media.surface;

import android.content.Context;
import android.opengl.EGLExt;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;

import java.lang.ref.WeakReference;


/**
 * 离屏渲染器
 */

public final class OffScreenVideoRenderer {

    private OffScreenVideoRenderer() {
    }


    private static class RenderHolder {
        private static OffScreenVideoRenderer instance = new OffScreenVideoRenderer();
    }

    public static OffScreenVideoRenderer getInstance() {
        return RenderHolder.instance;
    }


    // 渲染Handler
    private OffSVideoRenderHandler mRenderHandler;
    // 渲染线程
    private OffSVideoRenderThread mPreviewRenderThread;
    // 操作锁
    private final Object mSynOperation = new Object();

    private WeakReference<SurfaceView> mWeakSurfaceView;


    /**
     * 初始化渲染器
     */
    public void initRenderer(Context context) {
        Log.e("Harrison", "initRenderer");
//        synchronized (mSynOperation) {
        mPreviewRenderThread = new OffSVideoRenderThread(context, "OffSVideoRenderThread");
        mPreviewRenderThread.start();
        mRenderHandler = new OffSVideoRenderHandler(mPreviewRenderThread);
        // 绑定Handler
        mPreviewRenderThread.setRenderHandler(mRenderHandler);
        OffSVideoRenderManager.getInstance().init(context);
//        }
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
     * Surface回调
     */
    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (mRenderHandler != null) {
                mRenderHandler.sendMessage(mRenderHandler
                        .obtainMessage(OffSVideoRenderHandler.MSG_SURFACE_CREATED, holder));
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
                        .obtainMessage(OffSVideoRenderHandler.MSG_SURFACE_DESTROYED));
            }
        }
    };


    /**
     * Surface大小发生变化
     *
     * @param width
     * @param height
     */
    public void surfaceSizeChanged(int width, int height) {
        if (mRenderHandler != null) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(OffSVideoRenderHandler.MSG_SURFACE_CHANGED, width, height));
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
                    .obtainMessage(OffSVideoRenderHandler.MSG_CHANGE_DYNAMIC_REMOVE, color));
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
                    .obtainMessage(OffSVideoRenderHandler.MSG_CHANGE_DYNAMIC_COLOR_FILTER, color));
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
                    .obtainMessage(OffSVideoRenderHandler.MSG_CHANGE_DYNAMIC_CAMERA_FILTER, color));
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
                    .obtainMessage(OffSVideoRenderHandler.MSG_CHANGE_DYNAMIC_RESOURCE, color));
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
                    .obtainMessage(OffSVideoRenderHandler.MSG_CHANGE_DYNAMIC_RESOURCE, sticker));
        }
    }

    public Surface getSurface() {
        if (mPreviewRenderThread != null) {
            return mPreviewRenderThread.getSurface();
        }
        return null;
    }

    public OffSVideoRenderThread getOutputSurface() {
        return mPreviewRenderThread;
    }


    public void onSurfaceCreate(Surface surface) {
        if (mPreviewRenderThread != null) {
            mPreviewRenderThread.surfaceCreated(surface);
        }
    }

    public void onSurfaceChanged(int width, int height) {
        if (mPreviewRenderThread != null) {
            mPreviewRenderThread.surfaceChanged(width, height);
        }
    }


    public void setPresentationTime(long l) {
        if (mPreviewRenderThread != null) {
            mPreviewRenderThread.setPresentationTime(l);
        }
    }

    public void updateTexImage() {
        if (mPreviewRenderThread != null) {
            mPreviewRenderThread.updateTexImage();
        }
    }

    public void awaitNewImage() {
        if (mPreviewRenderThread != null) {
            mPreviewRenderThread.awaitNewImage();
        }
    }

    public void makeCurrent() {
        if (mPreviewRenderThread != null) {
            mPreviewRenderThread.makeCurrent();
        }
    }

    /**
     * 显示在屏幕
     */

    public void swapBuffers() {
        if (mPreviewRenderThread != null) {
            mPreviewRenderThread.swapBuffers();
        }
    }
}
