package com.cgfay.cameralibrary.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.opengl.GLES20;
import android.util.Log;

import com.cgfay.cameralibrary.mp4compose.filter.GlFilter;
import com.cgfay.cameralibrary.mp4compose.filter.GlOverlayFilter;

public abstract class GLStickerFilter extends GlOverlayFilter {

//    private Bitmap bitmap;
//
//    public GLStickerFilter(Bitmap bitmap) {
//        this.bitmap = bitmap;
//    }
    public GLStickerFilter() {
    }


//    @Override
//    protected void drawCanvas(Canvas canvas) {
//        if (bitmap != null && !bitmap.isRecycled()) {
//            Log.e("Harrison", "drawCanvas"+canvas.getWidth()+"***"+canvas.getHeight());
//            canvas.drawBitmap(bitmap, 0, 0, null);
//        } else {
//            Log.e("Harrison", "drawa null");
//        }
//    }

    /**
     * 被调用了两次，造成bitmap 不能用，奇怪
     */
    @Override
    public void release() {
        Log.e("Harrison", "release**********");
//        if (bitmap != null && !bitmap.isRecycled()) {
//            bitmap.recycle();
//        }
    }

}
