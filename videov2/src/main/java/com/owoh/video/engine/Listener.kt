package com.owoh.video.engine

import java.nio.ByteBuffer

/**
 * 相机回调
 */
interface OnCameraCallback {

    // 相机已打开
    fun onCameraOpened()

    // 预览回调
    fun onPreviewCallback(data: ByteArray)
}

/**
 * 截帧监听器
 * Created by cain.huang on 2017/12/27.
 */
interface OnCaptureListener {
    // 截帧回调
    fun onCapture(buffer: ByteBuffer, width: Int, height: Int)
}
/**
 * fps监听器
 */
interface OnFpsListener {
    // fps回调
    fun onFpsCallback(fps: Float)
}

/**
 * 录制监听器
 */
interface OnRecordListener {
    // 录制已经开始
    fun onRecordStarted()

    // 录制时间改变
    fun onRecordProgressChanged(duration: Long)

    // 录制已经结束
    fun onRecordFinish()
}
