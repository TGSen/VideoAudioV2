package com.owoh.video.engine

/**
 * 长宽比
 * Created by cain.huang on 2017/7/27.
 */
enum class AspectRatio { RATIO_4_3, RATIO_1_1, Ratio_16_9 }

/**
 * 相机预览和拍照计算类型
 * Created by cain on 2018/1/14.
 */

enum class CalculateType {
    Min, // 最小
    Max, // 最大
    Larger, // 大一点
    Lower
    // 小一点
}

/**
 * Created by cain.huang on 2017/9/28.
 */
enum class GalleryType {
    PICTURE, // 图片
    VIDEO
    // 视频
}

/**
 * 缩放类型
 * Created by cain on 17-7-26.
 */
enum class ScaleType { CENTER_INSIDE, CENTER_CROP, FIT_XY }

class Size(var width: Int = 0, var height: Int = 0)
