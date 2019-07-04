package com.owoh.video.widget.sticker

import android.view.MotionEvent

/**
 * @author wupanjie
 */

class ZoomIconEvent : StickerIconEvent {
    override fun onActionDown(stickerView: StickerView, event: MotionEvent) {

    }

    override fun onActionMove(stickerView: StickerView, event: MotionEvent) {
        stickerView.zoomAndRotateCurrentSticker(event)
    }

    override fun onActionUp(stickerView: StickerView, event: MotionEvent) {
        stickerView.currentSticker?.apply {
            stickerView.onStickerOperationListener?.onStickerZoomFinished(this)
        }
    }
}
