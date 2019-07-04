package com.owoh.video

import com.owoh.video.engine.GalleryType


/**
 * 图库点击监听器
 */
interface OnGallerySelectedListener {

    // 图库点击监听器
    fun onGalleryClickListener(type: GalleryType)
}

/**
 * 页面监听器
 */
interface OnPageOperationListener {
    // 打开视频编辑页面
    fun onOpenVideoEditPage(path: String)

}

/**
 * 媒体拍摄回调
 */
interface OnPreviewCaptureListener {

    // 媒体选择
    fun onMediaSelectedListener(path: String, type: GalleryType)
}
