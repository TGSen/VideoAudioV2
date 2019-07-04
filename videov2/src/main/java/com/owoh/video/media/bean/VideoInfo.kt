package com.owoh.video.media.bean

import java.io.Serializable

/**
 * Created by Administrator on 2017/6/29 0029.
 * 视频的信息bean
 */

class VideoInfo : Serializable {
    var path: String = ""//路径
    var outPath: String = ""
    var rotation: Int = 0//旋转角度
    var width: Int = 0//宽
    var height: Int = 0//高
    var bitRate: Int = 0//比特率
    var frameRate: Int = 0//帧率
    var frameInterval: Int = 0//关键帧间隔
    var duration: Int = 0//时长

    var expWidth: Int = 0//期望宽度
    var expHeight: Int = 0//期望高度
    var cutPoint: Int = 0//剪切的开始点
    var cutDuration: Int = 0//剪切的时长
    var frameCount: Int = 0// 帧的数量





}
