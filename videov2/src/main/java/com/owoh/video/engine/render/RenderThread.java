package com.owoh.video.engine.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES30;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.owoh.video.engine.camera.CameraEngine;
import com.owoh.video.engine.camera.CameraParam;
import com.owoh.video.engine.camera.SensorControler;
import com.owoh.video.engine.recorder.HardcodeEncoder;
import com.cgfay.filterlibrary.gles.EglCore;
import com.cgfay.filterlibrary.gles.WindowSurface;
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceType;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

import java.nio.ByteBuffer;

/**
 * 渲染线程
 * Created by cain on 2017/11/4.
 */

public class RenderThread extends HandlerThread implements SurfaceTexture.OnFrameAvailableListener,
        Camera.PreviewCallback {

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
    private RenderHandler mRenderHandler;

    // 计算帧率
    private FrameRateMeter mFrameRateMeter;

    // 上下文
    private Context mContext;

    // 正在拍照
    private volatile boolean mTakingPicture;
    // 预览参数
    private CameraParam mCameraParam;

    // 渲染管理器
    private RenderManager mRenderManager;


    public RenderThread(Context context, String name) {
        super(name);
        mContext = context;
        mCameraParam = CameraParam.getInstance();
        mRenderManager = RenderManager.getInstance();
        mFrameRateMeter = new FrameRateMeter();
    }

    /**
     * 设置预览Handler回调
     *
     * @param handler
     */
    public void setRenderHandler(RenderHandler handler) {
        mRenderHandler = handler;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    private long time = 0;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        synchronized (mSynOperation) {
            if (isPreviewing || isRecording) {
                mRenderHandler.sendMessage(mRenderHandler
                        .obtainMessage(RenderHandler.MSG_PREVIEW_CALLBACK, data));
            }
        }
        if (mPreviewBuffer != null) {
            camera.addCallbackBuffer(mPreviewBuffer);
        }
        // 计算fps
        if (mRenderHandler != null && mCameraParam.showFps) {
            mRenderHandler.sendEmptyMessage(RenderHandler.MSG_CALCULATE_FPS);
        }
        if (VERBOSE) {
            Log.d("onPreviewFrame", "update time = " + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();
        }
    }

    /**
     * 预览回调
     *
     * @param data
     */
    void onPreviewCallback(byte[] data) {
        if (mCameraParam.cameraCallback != null) {
            mCameraParam.cameraCallback.onPreviewCallback(data);
        }
    }

    /**
     * Surface创建
     *
     * @param holder
     */
    void surfaceCreated(SurfaceHolder holder) {
        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
        mDisplaySurface = new WindowSurface(mEglCore, holder.getSurface(), false);
        mDisplaySurface.makeCurrent();

        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        GLES30.glDisable(GLES30.GL_CULL_FACE);

        // 渲染器初始化
        mRenderManager.init(mContext);

        mInputTexture = OpenGLUtils.createOESTexture();
        mSurfaceTexture = new SurfaceTexture(mInputTexture);
        mSurfaceTexture.setOnFrameAvailableListener(this);

        openCamera();
        //初始化
        SensorControler.getInstance().init(mContext);
    }

    /**
     * Surface改变
     *
     * @param width
     * @param height
     */
    void surfaceChanged(int width, int height) {
        mRenderManager.setDisplaySize(width, height);
//        mTextureWidth = width;
//        mTextureHeight = height;
        //  openCamera();
        // cameraSetting();
        startPreview();

    }


    /**
     * Surface销毁
     */
    void surfaceDestroyed() {
        mTakingPicture = false;
        mRenderManager.release();
        releaseCamera();
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
                        mSurfaceTexture.updateTexImage();
                        --mFrameNum;
                    }
                } else {
                    return;
                }
            }
        }

        // 切换渲染上下文
        mDisplaySurface.makeCurrent();
        mSurfaceTexture.getTransformMatrix(mMatrix);

        // 绘制渲染
        mCurrentTexture = mRenderManager.drawFrame(mInputTexture, mMatrix);


        // 显示到屏幕
        mDisplaySurface.swapBuffers();

        // 执行拍照
        if (mCameraParam.isTakePicture && !mTakingPicture) {
            synchronized (mSyncFence) {
                mTakingPicture = true;
                mRenderHandler.sendEmptyMessage(RenderHandler.MSG_TAKE_PICTURE);
            }
        }

        // 是否处于录制状态
        if (isRecording && !isRecordingPause) {
            HardcodeEncoder.getInstance().frameAvailable();
            HardcodeEncoder.getInstance().drawRecorderFrame(mCurrentTexture, mSurfaceTexture.getTimestamp());
        }
    }

    /**
     * 拍照
     */
    void takePicture() {
        synchronized (mSyncFence) {
            ByteBuffer buffer = mDisplaySurface.getCurrentFrame();
            mCameraParam.captureCallback.onCapture(buffer,
                    mDisplaySurface.getWidth(), mDisplaySurface.getHeight());
            mTakingPicture = false;
            mCameraParam.isTakePicture = false;
        }
    }

    /**
     * 计算fps
     */
    void calculateFps() {
        // 帧率回调
        if ((mCameraParam).fpsCallback != null) {
            mFrameRateMeter.drawFrameCount();
            (mCameraParam).fpsCallback.onFpsCallback(mFrameRateMeter.getFPS());
        }
    }

    /**
     * 计算imageView 的宽高
     */
    private void calculateImageSize() {
//        if (mCameraParam.orientation == 90 || mCameraParam.orientation == 270) {
//            mTextureWidth = mCameraParam.previewHeight;
//            mTextureHeight = mCameraParam.previewWidth;
//        } else {
//            mTextureWidth = mCameraParam.previewWidth;
//            mTextureHeight = mCameraParam.previewHeight;
//        }
        mRenderManager.setTextureSize(mTextureWidth, mTextureHeight);
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
            HardcodeEncoder.getInstance().setTextureSize(CameraParam.getInstance().previewWidth,CameraParam.getInstance().previewWidth);
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
        synchronized (mSyncFrameNum) {
            if (isPreviewing) {
                ++mFrameNum;
                if (mRenderHandler != null) {
                    mRenderHandler.removeMessages(RenderHandler.MSG_RENDER);
                    mRenderHandler.sendMessage(mRenderHandler
                            .obtainMessage(RenderHandler.MSG_RENDER));
                }
            }
        }
    }


    // --------------------------------- 相机操作逻辑 ----------------------------------------------

    private void cameraSetting() {
        //  CameraEngine.getInstance().cameraSetting(mContext,mTextureWidth,mTextureHeight);
    }

    /**
     * 打开相机
     */
    void openCamera() {
        releaseCamera();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        mTextureWidth = dm.widthPixels;
        mTextureHeight = dm.heightPixels;
        Camera.Size size = CameraEngine.getInstance().openCamera(mContext, mTextureWidth, mTextureHeight, CameraParam.getInstance().DEFAULT_16_9_WIDTH, CameraParam.getInstance().DEFAULT_16_9_HEIGHT);
        mTextureWidth = size.height;
        mTextureHeight = size.width;
        Log.e("Harrison", "****" + mTextureWidth + "****" + mTextureHeight);
        CameraEngine.getInstance().setPreviewSurface(mSurfaceTexture);
        calculateImageSize();
        mPreviewBuffer = new byte[mTextureWidth * mTextureHeight * 3 / 2];
        CameraEngine.getInstance().setPreviewCallbackWithBuffer(this, mPreviewBuffer);
        // 相机打开回调
        if (mCameraParam.cameraCallback != null) {
            mCameraParam.cameraCallback.onCameraOpened();
        }

    }

    /**
     * 切换相机
     */
    void switchCamera() {
        mCameraParam.backCamera = !mCameraParam.backCamera;
        if (mCameraParam.backCamera) {
            mCameraParam.cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            mCameraParam.cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        openCamera();
        startPreview();

    }



    /**
     * 开始预览
     */
    private void startPreview() {
        CameraEngine.getInstance().startPreview();
        isPreviewing = true;
    }

    /**
     * 释放相机
     */
    private void releaseCamera() {
        isPreviewing = false;
        CameraEngine.getInstance().releaseCamera();
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


}
