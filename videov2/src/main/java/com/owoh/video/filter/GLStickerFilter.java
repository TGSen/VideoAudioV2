package com.owoh.video.filter;

import android.graphics.Paint;
import android.util.Log;

import com.cgfay.filterlibrary.mp4compose.filter.GlOverlayFilter;

public abstract class GLStickerFilter extends GlOverlayFilter {
    //当前时间
    private double currentTime;

    public double getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(double currentTime) {
        this.currentTime = currentTime;
    }



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
