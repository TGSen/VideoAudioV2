package com.cgfay.cameralibrary.media.bgmusic;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
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

    private MediaPlayerLinstener mediaPlayerLinstener;

    //初始化MediaPlayer
    public MediaPlayer mMediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mediaPlayerLinstener != null) {
                    mediaPlayerLinstener.onCompletion();
                }
            }
        });

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (mediaPlayerLinstener != null) {
                    mediaPlayerLinstener.onPrepareFinish();
                }
                mp.start();
                mp.setLooping(true);
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
        public boolean changeUrl(String url) {
            //获取文件路径
            try {
                //如果切换音频的时候需要重置一下
                if (TextUtils.isEmpty(url)) {
                    if (mediaPlayerLinstener != null) {
                        mediaPlayerLinstener.onFail(url);
                        return false;
                    }
                }

                if (mediaPlayerLinstener != null) {
                    mediaPlayerLinstener.onStart(url);
                }
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(url);
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        public void setMediaPlayerLinstener(MediaPlayerLinstener linstener) {
            mediaPlayerLinstener = linstener;
        }


    }

    /**
     * 通过接口回调，通知Activity
     */
    public interface MediaPlayerLinstener {
        void onStart(String url);

        void onFail(String url);

        void onPrepareFinish();

        void onCompletion();
    }
}
