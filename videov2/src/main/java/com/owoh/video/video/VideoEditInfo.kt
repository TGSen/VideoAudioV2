package com.owoh.video.video

import java.io.Serializable


class VideoEditInfo(
    var path: String? = null, //图片的sd卡路径
    var time: Long = 0//图片所在视频的时间  毫秒
) : Serializable {
    override fun toString(): String {
        return "VideoEditInfo{path='$path\', time='$time\'}"
    }
}
