package com.cgfay.cameralibrary.media.combine;

import java.io.IOException;

/**
 * @author Harrison 唐广森
 * @description:
 * @date :2019/4/22 17:02
 */
public interface IAudioVideo {
    //初始化
    void init() throws IOException;

    //开始合并
    void startCombine();

    void endCombine();
}
