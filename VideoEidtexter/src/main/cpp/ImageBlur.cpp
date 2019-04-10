//
// Created by GuestUser on 2019/4/10.
//
#include <android/bitmap.h>
#include "stackblur.h"

#include "ImageBlur.h"
/**
 * 动态注册相关的
 * JNIREG_CLASS 如果该类改变的时候，就只改这里就可以
 */

#define JNIREG_CLASS "com/cgfay/cameralibrary/utils/ImageBlur"

void blurBitmap(JNIEnv *env, jclass jclazz, jobject bitmapIn, jint r) {
    AndroidBitmapInfo infoIn;
    void *pixels;

    // Get image info
    if (AndroidBitmap_getInfo(env, bitmapIn, &infoIn) != ANDROID_BITMAP_RESULT_SUCCESS) {

        return;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 &&
        infoIn.format != ANDROID_BITMAP_FORMAT_RGB_565) {

        return;
    }

    // Lock all images
    if (AndroidBitmap_lockPixels(env, bitmapIn, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS) {

        return;
    }
    // height width
    int h = infoIn.height;
    int w = infoIn.width;

    // Start
    if (infoIn.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
        pixels = blur_ARGB_8888((int *) pixels, w, h, r);
    } else if (infoIn.format == ANDROID_BITMAP_FORMAT_RGB_565) {
        pixels = blur_RGB_565((short *) pixels, w, h, r);
    }
    // End
    // Unlocks everything
    AndroidBitmap_unlockPixels(env, bitmapIn);
};


static int registerNativeMethods(JNIEnv *env, const char *className, JNINativeMethod *gMethods,
                                 int numMethods) {
    jclass clazz = nullptr;
    clazz = env->FindClass(className);
    if (clazz == nullptr) {
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
};

// 的所有方法
static JNINativeMethod mMethods[] = {
        {"blurBitmap", "(Landroid/graphics/Bitmap;I)V", (void *) blurBitmap}
};


/*
* 为所有该类注册本地方法
*/
static int registerNatives(JNIEnv *env) {
    int re = registerNativeMethods(env, JNIREG_CLASS, mMethods,
                                   sizeof(mMethods) / sizeof(mMethods[0]));
    return re;
}
/*
* System.loadLibrary("lib")时会调用
* 如果成功返回JNI版本, 失败返回-1
*/
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    jint result = -1;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    if (!registerNatives(env)) {//注册
        return -1;
    }
    //成功
    result = JNI_VERSION_1_6;
    return result;
}


/**动态注册结束*/

