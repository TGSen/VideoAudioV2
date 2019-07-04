package com.owoh.video.widget.sticker

/**
 * @author wupanjie
 */

class FlipHorizontallyEvent : AbstractFlipEvent() {

    override val flipDirection: Int
        @StickerView.Flip get() = StickerView.FLIP_HORIZONTALLY
}
