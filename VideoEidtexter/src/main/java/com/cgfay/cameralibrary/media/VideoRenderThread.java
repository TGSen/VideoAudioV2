package com.cgfay.cameralibrary.media;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.GLES30;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.cgfay.cameralibrary.engine.recorder.HardcodeEncoder;
import com.cgfay.cameralibrary.engine.render.RenderManager;
import com.cgfay.cameralibrary.media.bean.VideoEffectType;
import com.cgfay.filterlibrary.gles.EglCore;
import com.cgfay.filterlibrary.gles.WindowSurface;
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceType;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 渲染线程
 * Created by cain on 2017/11/4.
 */

public class VideoRenderThread extends HandlerThread implements SurfaceTexture.OnFrameAvailableListener {

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
    private String mVideoPath;

    private VideoPlayerStatusChangeLisenter mVideoPlayerStatusChangeLisenter;

    public void setVideoPlayerStatusChangeLisenter(VideoPlayerStatusChangeLisenter lisenter) {
        mVideoPlayerStatusChangeLisenter = lisenter;
    }

    // 矩阵
    private final float[] mMatrix = new float[16];

    // 预览回调
    private byte[] mPreviewBuffer;
    // 输入图像大小
    private int mTextureWidth, mTextureHeight;

    // 可用帧
    private int mFrameNum = 0;

    // 渲染Handler回调
    private VideoRenderHandler mRenderHandler;


    // 上下文
    private Context mContext;


    // 渲染管理器
    private VideoRenderManager mRenderManager;
    private MediaPlayer mMediaPlayer;

    public VideoRenderThread(Context context, String name, VideoEffectType effectType) {
        super(name);
        mContext = context;

        mRenderManager = new VideoRenderManager(effectType);

    }

    /**
     * 设置预览Handler回调
     *
     * @param handler
     */
    public void setRenderHandler(VideoRenderHandler handler) {
        mRenderHandler = handler;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
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
        Surface surface = new Surface(mSurfaceTexture);
        //开始播放视频
        playVideo(surface);
    }

    private void playVideo(Surface surface) {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setSurface(surface);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            if (!TextUtils.isEmpty(mVideoPath) && new File(mVideoPath).canRead()) {
                Log.e("Harrison", "playVideo:" + mVideoPath);
                mMediaPlayer.setDataSource(mVideoPath);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.prepareAsync();
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mMediaPlayer.start();
                if (mVideoPlayerStatusChangeLisenter != null) {
                    mVideoPlayerStatusChangeLisenter.videoStart(mMediaPlayer.getDuration());
                }
            }
        });

        //设置无限循环
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer player) {
                player.start();
                player.setLooping(true);
            }
        });
    }

    /**
     * @param
     */
    /**
     * 设置视频的播放地址
     */
    public void setVideoPath(String paths) {
        this.mVideoPath = paths;
    }

    /**
     * Surface改变
     *
     * @param width
     * @param height
     */
    void surfaceChanged(int width, int height) {
        Log.e("Harrison", "surfaceChanged" + width + height);
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
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
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


        // 是否处于录制状态
        if (isRecording && !isRecordingPause) {
            HardcodeEncoder.getInstance().frameAvailable();
            HardcodeEncoder.getInstance()
                    .drawRecorderFrame(mCurrentTexture, mSurfaceTexture.getTimestamp());
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
                    mRenderHandler.removeMessages(VideoRenderHandler.MSG_RENDER);
                    mRenderHandler.sendMessage(mRenderHandler
                            .obtainMessage(VideoRenderHandler.MSG_RENDER));
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

    /**
     * 设置视频暂停
     */
    public void setVideoStop() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            if (mVideoPlayerStatusChangeLisenter != null) {
                mVideoPlayerStatusChangeLisenter.videoStop();
            }
        }
    }

    /**
     * 设置视频继续播放
     */
    public void setVideoStart() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            if (mVideoPlayerStatusChangeLisenter != null) {
                mVideoPlayerStatusChangeLisenter.videoRestart();
            }

        }
    }

    public int getVideoProgress() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();

        }
        return 0;
    }

    public void changeVideoProgress(int progress) {
        if(mMediaPlayer!=null)
            mMediaPlayer.seekTo(progress);
    }

    public boolean isVideoPlay() {
        if (mMediaPlayer != null) return mMediaPlayer.isPlaying();
        return false;
    }

    /**
     * 视频状态的改变,该方法在子线程执行的
     */
    public interface VideoPlayerStatusChangeLisenter {
        void videoStart(int totalTime);

        void videoStop();

        void videoRestart();

    }
}
