package com.cgfay.cameralibrary.media.surface;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.EGLExt;
import android.opengl.GLES30;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.cgfay.cameralibrary.engine.recorder.HardcodeEncoder;
import com.cgfay.filterlibrary.gles.EglCore;
import com.cgfay.filterlibrary.gles.WindowSurface;
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceType;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

import java.io.File;
import java.io.IOException;

/**
 * 渲染线程
 * Created by cain on 2017/11/4.
 */

class OffSVideoRenderThread extends HandlerThread implements SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = "OffSVideoRenderThread";
    private static final boolean VERBOSE = false;

    // 操作锁
    private final Object mSynOperation = new Object();
    // 更新帧的锁
    private final Object mSyncFrameNum = new Object();
    private final Object mSyncFence = new Object();

    private boolean isPreviewing = false;       // 是否预览状态
    private boolean isRecording = false;        // 是否录制状态
    private boolean isRecordingPause = false;   // 是否处于暂停录制状态

    // EGL共享上下文
    private EglCore mEglCore;
    // 预览用的EGLSurface
    private WindowSurface mDisplaySurface;

    private int mInputTexture;
    private int mCurrentTexture;
    private SurfaceTexture mSurfaceTexture;


    // 矩阵
    private final float[] mMatrix = new float[16];

    // 预览回调
    private byte[] mPreviewBuffer;
    // 输入图像大小
    private int mTextureWidth, mTextureHeight;

    // 可用帧
    private int mFrameNum = 0;

    // 渲染Handler回调
    private OffSVideoRenderHandler mRenderHandler;


    // 上下文
    private Context mContext;


    // 渲染管理器
    private OffSVideoRenderManager mRenderManager;
    public Surface EncodeSurface;
    public Surface mSurface;


    public OffSVideoRenderThread(Context context, String name) {
        super(name);
        mContext = context;
        mRenderManager = OffSVideoRenderManager.getInstance();
    }

    /**
     * 设置预览Handler回调
     *
     * @param handler
     */
    public void setRenderHandler(OffSVideoRenderHandler handler) {
        mRenderHandler = handler;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.e("Harrison", "onFrameAvailable***1*");
        requestRender();
//        mSurfaceTexture.updateTexImage();
//        synchronized (mFrameSyncObject) {
//            if (mFrameAvailable) {
//                // throw new RuntimeException("mFrameAvailable already set, frame could be dropped");
//                return;
//            }
//            Log.e("Harrison", "onFrameAvailable****");
//            mFrameAvailable = true;
//            mFrameSyncObject.notifyAll();
//
//        }
    }


    /**
     * Surface创建
     *
     * @param
     */
    void surfaceCreated(Surface surface) {

        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
        mDisplaySurface = new WindowSurface(mEglCore, surface, false);
        mDisplaySurface.makeCurrent();
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        GLES30.glDisable(GLES30.GL_CULL_FACE);
        // 渲染器初始化
        mRenderManager.init(mContext);
        mInputTexture = OpenGLUtils.createOESTexture();
        mSurfaceTexture = new SurfaceTexture(mInputTexture);
        mSurfaceTexture.setOnFrameAvailableListener(this);

        mSurface = new Surface(mSurfaceTexture);
        EncodeSurface = surface;

    }


    /**
     * Surface改变
     *
     * @param width
     * @param height
     */
    void surfaceChanged(int width, int height) {
        //这代码在调试中
        mRenderManager.setTextureSize(width, height);
        mRenderManager.setDisplaySize(width, height);
    }


    /**
     * Surface销毁
     */
    void surfaceDestroyed() {
        mRenderManager.release();
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        if (mDisplaySurface != null) {
            mDisplaySurface.release();
            mDisplaySurface = null;
        }
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }
        if (mSurface != null) {
            mSurface.release();
        }
    }

    /**
     * 绘制帧
     */
    void drawFrame() {
        // 如果存在新的帧，则更新帧
        synchronized (mSyncFrameNum) {
            synchronized (mSyncFence) {
                if (mSurfaceTexture != null) {
                    while (mFrameNum != 0) {
                        Log.e("Harrison","updateTexImage:"+mFrameNum);
                        mSurfaceTexture.updateTexImage();
                        --mFrameNum;
                    }
                } else {
                    return;
                }
            }
        }
        Log.e("Harrison", "makeCurrent");
        // 切换渲染上下文
//        mDisplaySurface.makeCurrent();
        mSurfaceTexture.getTransformMatrix(mMatrix);

        // 绘制渲染
        mCurrentTexture = mRenderManager.drawFrame(mInputTexture, mMatrix);


        // 是否处于录制状态
        if (isRecording && !isRecordingPause) {
            HardcodeEncoder.getInstance().frameAvailable();
            HardcodeEncoder.getInstance()
                    .drawRecorderFrame(mCurrentTexture, mSurfaceTexture.getTimestamp());
        }
    }

    private Object mFrameSyncObject = new Object();     // guards mFrameAvailable
    private boolean mFrameAvailable;

    public void awaitNewImage() {
        final int TIMEOUT_MS =2500;
        synchronized (mFrameSyncObject) {
            while (!mFrameAvailable) {
                try {
                    // Wait for onFrameAvailable() to signal us.  Use a timeout to avoid
                    // stalling the test if it doesn't arrive.
                    mFrameSyncObject.wait(TIMEOUT_MS);
                    if (!mFrameAvailable) {
                        // TODO: if "spurious wakeup", continue while loop
                        break;
                    }
                } catch (InterruptedException ie) {
                    // shouldn't happen

                }
            }
            mFrameAvailable = false;
        }
        Log.e("Harrison","updateTexImage");
        mSurfaceTexture.updateTexImage();

    }


    /**
     * 切换动态滤镜
     *
     * @param color
     */
    void changeDynamicFilter(DynamicColor color) {
        synchronized (mSynOperation) {
            mRenderManager.changeDynamicFilter(color);
        }
    }

    public void changeColorDynamicFilter(DynamicColor color) {
        synchronized (mSynOperation) {
            mRenderManager.changeColorDynamicFilter(color);
        }
    }

    /**
     * 切换动态Camera滤镜
     *
     * @param color
     */
    void changeCameraDynamicFilter(DynamicColor color) {
        synchronized (mSynOperation) {
            mRenderManager.changeCameraDynamicFilter(color);
        }
    }


    /**
     * 切换动态资源
     *
     * @param color
     */
    void changeDynamicResource(DynamicColor color) {
        synchronized (mSynOperation) {
            mRenderManager.changeDynamicResource(color);
        }
    }

    /**
     * 切换动态资源
     *
     * @param sticker
     */
    void changeDynamicResource(DynamicSticker sticker) {
        synchronized (mSynOperation) {
            mRenderManager.changeDynamicResource(sticker);
        }
    }

    /**
     * 开始录制
     */
    void startRecording() {
        if (mEglCore != null) {
            // 设置渲染Texture 的宽高
            HardcodeEncoder.getInstance().setTextureSize(mTextureWidth, mTextureHeight);
            // 这里将EGLContext传递到录制线程共享。
            // 由于EGLContext是当前线程手动创建，也就是OpenGLES的main thread
            // 这里需要传自己手动创建的EglContext
            HardcodeEncoder.getInstance().startRecording(mContext, mEglCore.getEGLContext());
        }
        isRecording = true;
    }

    /**
     * 停止录制
     */
    void stopRecording() {
        HardcodeEncoder.getInstance().stopRecording();
        isRecording = false;
    }

    /**
     * 请求刷新
     */
    public void requestRender() {
        isPreviewing = true;
        synchronized (mSyncFrameNum) {
            if (isPreviewing) {
                ++mFrameNum;
                if (mRenderHandler != null) {
                    Log.e("Harrison", "mRenderHandler*****");
                    mRenderHandler.removeMessages(OffSVideoRenderHandler.MSG_RENDER);
                    mRenderHandler.sendMessage(mRenderHandler.obtainMessage(OffSVideoRenderHandler.MSG_RENDER));
                }
            }
        }
    }


    public void removeDynamicResource(DynamicColor obj) {
        synchronized (mSynOperation) {
            if (obj.getColorType() == ResourceType.CAMERA_FILTER.getIndex()) {
                mRenderManager.removeDynamicCameraFilter();
            } else {
                mRenderManager.removeDynamicColorFilter();
            }
        }

    }


    public Surface getSurface() {
        return mSurface;
    }

    public void setPresentationTime(long nsecs) {
        mDisplaySurface.setPresentationTime(nsecs);
    }

    public void swapBuffers() {
        mDisplaySurface.swapBuffers();
    }

    public void updateTexImage() {
        mSurfaceTexture.updateTexImage();
    }

    public void makeCurrent() {
        mDisplaySurface.makeCurrent();
    }


}
