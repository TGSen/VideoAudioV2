package com.owoh.video.widget.sticker

import android.view.MotionEvent

abstract class AbstractFlipEvent : StickerIconEvent {

    @get:StickerView.Flip
    protected abstract val flipDirection: Int

    override fun onActionDown(stickerView: StickerView, event: MotionEvent) {

    }

    override fun onActionMove(stickerView: StickerView, event: MotionEvent) {

    }

    override fun onActionUp(stickerView: StickerView, event: MotionEvent) {
        stickerView.flipCurrentSticker(flipDirection)
    }
}
