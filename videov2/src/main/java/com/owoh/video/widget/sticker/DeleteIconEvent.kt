package com.owoh.video.widget.sticker

import android.view.MotionEvent

/**
 * @author wupanjie
 */

class DeleteIconEvent : StickerIconEvent {
    override fun onActionDown(stickerView: StickerView, event: MotionEvent) {

    }

    override fun onActionMove(stickerView: StickerView, event: MotionEvent) {

    }

    override fun onActionUp(stickerView: StickerView, event: MotionEvent) {
        stickerView.removeCurrentSticker()
    }
}
