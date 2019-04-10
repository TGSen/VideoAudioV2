package com.cgfay.cameralibrary.utils;

import android.graphics.Bitmap;

/**
 * @author Harrison 唐广森
 * @description:
 * @date :2019/4/10 16:37
 */
public class ImageBlur {
    static {
        System.loadLibrary("imageBlur");
    }

    public native static void blurBitmap(Bitmap bitmap, int r);
}
