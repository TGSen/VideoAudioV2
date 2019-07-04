package com.owoh.video.widget.sticker

import android.view.MotionEvent

/**
 * @author wupanjie
 */

interface StickerIconEvent {
    fun onActionDown(stickerView: StickerView, event: MotionEvent)

    fun onActionMove(stickerView: StickerView, event: MotionEvent)

    fun onActionUp(stickerView: StickerView, event: MotionEvent)
}
