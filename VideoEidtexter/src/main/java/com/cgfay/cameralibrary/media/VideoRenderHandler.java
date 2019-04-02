package com.cgfay.cameralibrary.media;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 预览渲染Handler
 * Created by cain.huang on 2017/11/3.
 */

class VideoRenderHandler extends Handler {

    // Surface创建
    public static final int MSG_SURFACE_CREATED = 0x001;
    // Surface改变
    public static final int MSG_SURFACE_CHANGED = 0x002;
    // Surface销毁
    public static final int MSG_SURFACE_DESTROYED = 0x003;
    // 渲染
    public static final int MSG_RENDER = 0x004;
    // 开始录制
    public static final int MSG_START_RECORDING = 0x006;
    // 停止录制
    public static final int MSG_STOP_RECORDING = 0x008;

    // 设置video paths
    public static final int MSG_SURFACE_SET_VIDEO_PATH = 0x009;
    // 预览帧回调
    public static final int MSG_PREVIEW_CALLBACK = 0x011;


    // 切换动态滤镜
    public static final int MSG_CHANGE_DYNAMIC_COLOR = 0x14;


    // 切换动态动态资源
    public static final int MSG_CHANGE_DYNAMIC_RESOURCE = 0x15;
    //切换分镜的滤镜
    public static final int MSG_CHANGE_DYNAMIC_CAMERA_FILTER = 0x16;
    //移除滤镜
    public static final int MSG_CHANGE_DYNAMIC_REMOVE = 0x17;
    // 切换ColorFilter资源
    public static final int MSG_CHANGE_DYNAMIC_COLOR_FILTER = 0x18;


    private WeakReference<VideoRenderThread> mWeakRenderThread;

    public VideoRenderHandler(VideoRenderThread thread) {
        super(thread.getLooper());
        mWeakRenderThread = new WeakReference<VideoRenderThread>(thread);
    }

    @Override
    public void handleMessage(Message msg) {
        if (mWeakRenderThread == null || mWeakRenderThread.get() == null) {
            return;
        }
        VideoRenderThread thread = mWeakRenderThread.get();
        switch (msg.what) {

            // surfaceCreated
            case MSG_SURFACE_CREATED:
                thread.surfaceCreated((SurfaceHolder) msg.obj);
                break;

            // surfaceChanged
            case MSG_SURFACE_CHANGED:
                thread.surfaceChanged(msg.arg1, msg.arg2);
                break;

            // surfaceDestroyed;
            case MSG_SURFACE_DESTROYED:
                thread.surfaceDestroyed();
                break;

            // 帧可用（考虑同步的问题）
            case MSG_RENDER:
                Log.e("Harrison","Draw --");
                thread.drawFrame();
                break;

            // 开始录制
            case MSG_START_RECORDING:
                thread.startRecording();
                break;

            // 停止录制
            case MSG_STOP_RECORDING:
                thread.stopRecording();
                break;


            // 预览帧回调
            case MSG_PREVIEW_CALLBACK:
                thread.onPreviewCallback((byte[]) msg.obj);
                break;  // 预览帧回调

            case MSG_SURFACE_SET_VIDEO_PATH:
                thread.setVideoPath((List<String>) msg.obj);
                break;


            // 切换动态滤镜
            case MSG_CHANGE_DYNAMIC_COLOR: {
                thread.changeDynamicFilter((DynamicColor) msg.obj);
                break;
            }// 切分镜滤镜
            case MSG_CHANGE_DYNAMIC_CAMERA_FILTER: {
                thread.changeCameraDynamicFilter((DynamicColor) msg.obj);
                break;
            }
            case MSG_CHANGE_DYNAMIC_COLOR_FILTER: {
                thread.changeColorDynamicFilter((DynamicColor) msg.obj);
                break;
            }

            // 切换动态贴纸
            case MSG_CHANGE_DYNAMIC_RESOURCE:
                if (msg.obj == null) {
                    thread.changeDynamicResource((DynamicSticker) null);
                } else if (msg.obj instanceof DynamicColor) {
                    thread.changeDynamicResource((DynamicColor) msg.obj);
                } else if (msg.obj instanceof DynamicSticker) {
                    thread.changeDynamicResource((DynamicSticker) msg.obj);
                }
                break;
            case MSG_CHANGE_DYNAMIC_REMOVE:
                if (msg.obj != null)
                    thread.removeDynamicResource((DynamicColor) msg.obj);
                break;
            default:
                throw new IllegalStateException("Can not handle message what is: " + msg.what);
        }
    }
}
