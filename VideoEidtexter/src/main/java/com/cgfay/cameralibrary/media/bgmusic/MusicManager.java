package com.cgfay.cameralibrary.media.bgmusic;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * @author Harrison 唐广森
 * @description: 播放器的管理
 * @date :2019/4/23 16:49
 */
public class MusicManager {

    private MusicService.MusicBinder mMusicBinder;
    private String currentUrl;
    private ServiceConnection mServiceConnection;
    private volatile static MusicManager instance;

    private MusicManager() {

    }

    public static MusicManager getInstance() {
        if (instance == null) {
            synchronized (MusicManager.class) {
                if (instance == null) {
                    instance = new MusicManager();
                }
            }
        }
        return instance;
    }


    public void startService(Context context) {
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mMusicBinder = (MusicService.MusicBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mMusicBinder = null;
            }

        };
        Intent service = new Intent(context, MusicService.class);
        context.bindService(service, mServiceConnection, BIND_AUTO_CREATE);

    }

    public void changeAudioPlay(String url) {
        if (TextUtils.isEmpty(url) || url.equals(currentUrl) || mMusicBinder == null) return;
        this.currentUrl = url;
        mMusicBinder.changeUrl(url);
    }

    /**
     * 释放资源
     *
     * @param context
     */
    public void release(Context context) {
        if (mMusicBinder != null) {
            mMusicBinder.release();
            mMusicBinder = null;
        }
        if (mServiceConnection != null) {
            context.unbindService(mServiceConnection);
        }
    }


}
