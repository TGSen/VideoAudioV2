package com.cgfay.cameralibrary.media.bgmusic;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

/**
 * @author Harrison 唐广森
 * @description:
 * @date :2019/4/23 11:55
 */

public class MusicService extends Service {

    private static final String TAG = "MediaService";
    private MusicBinder mBinder = new MusicBinder();

    //初始化MediaPlayer
    public MediaPlayer mMediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.e("Harrison","onCompletion");
            }
        });

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);
                Log.e("Harrison","onPrepared");
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MusicBinder extends Binder {
        /**
         * 播放音乐
         */
        public void play() {
            if (!mMediaPlayer.isPlaying()) {
                //如果还没开始播放，就开始
                mMediaPlayer.start();
            }
        }

        /**
         * 暂停播放
         */
        public void stop() {
            if (mMediaPlayer.isPlaying()) {
                //如果还没开始播放，就开始
                mMediaPlayer.pause();
            }
        }

        /**
         * reset
         */
        public void reset() {
            if (!mMediaPlayer.isPlaying()) {
                //如果还没开始播放，就开始
                mMediaPlayer.reset();

            }
        }

        /**
         * 释放播放器
         */
        public void release() {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            }
        }


        /**
         * 获取长度
         **/
        public int getDuration() {

            return mMediaPlayer.getDuration();
        }

        /**
         * 获取播放位置
         */
        public int getCurretentPosition() {

            return mMediaPlayer.getCurrentPosition();
        }

        /**
         * seek
         */
        public void seekTo(int msec) {
            mMediaPlayer.seekTo(msec);
        }

        /**
         * @param url
         */
        public void changeUrl(String url) {
            //获取文件路径
            try {
                //此处的两个方法需要捕获IO异常
                //设置音频文件到MediaPlayer对象中
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(url);
                //让MediaPlayer对象准备
                mMediaPlayer.prepare();
            } catch (IOException e) {
                Log.e("Harrison", "");
                e.printStackTrace();
            }
        }
    }
}
