package com.owoh.video.widget.sticker;

import android.graphics.drawable.Drawable;

public class GifSticker extends DrawableSticker {
    //在保存渲染时，如果渲染一次了就不用从新 draw


    public GifSticker(Drawable drawable) {
        super(drawable);
    }


}
