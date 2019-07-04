package com.owoh.video.media.combine

import java.io.IOException

/**
 * @author Harrison 唐广森
 * @description:
 * @date :2019/4/22 17:02
 */
interface IAudioVideo {
    //初始化
    @Throws(IOException::class)
    fun init1()

    /**
     * 合并前做些准备
     */
    fun startCombinePrepare()

    //开始合并
    fun startCombine()

    fun endCombine()
}
