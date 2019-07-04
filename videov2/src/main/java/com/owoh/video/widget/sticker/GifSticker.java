package com.owoh.video.widget.sticker;

import android.graphics.drawable.Drawable;

public class GifSticker extends DrawableSticker {
    //在保存渲染时，如果渲染一次了就不用从新 draw
    private boolean isDrawning;
    public GifSticker(Drawable drawable) {
        super(drawable);
    }

    public boolean isDrawning() {
        return isDrawning;
    }

    public void setDrawning(boolean drawning) {
        isDrawning = drawning;
    }
}
