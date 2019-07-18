package com.owoh.video.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 下载服务
 * Created by QZD on 2017/9/20.
 */

public class DownLoadService {
    private final String TAG = "LOGCAT";
    private int fileLength, downloadLength;//文件大小
    private Handler handler = new Handler(Looper.myLooper());
    //目前只做一个一个下载
    private boolean isStart;
    private  Executor EXECUTOR = Executors.newCachedThreadPool();


    private volatile static DownLoadService singleton;

    private DownLoadService() {
    }

    public static DownLoadService getInstance() {
        if (singleton == null) {
            synchronized (DownLoadService.class) {
                if (singleton == null) {
                    singleton = new DownLoadService();
                }
            }
        }
        return singleton;
    }


    public void downloadFile(final String url, String savePath) {
        try {
            if(isStart)return;
            isStart = true;
            File dirs = new File(savePath);//文件保存地址
            if (!dirs.exists()) {// 检查文件夹是否存在，不存在则创建
                dirs.mkdir();
            }
            final File file = new File(dirs, url.substring(url.lastIndexOf('/') + 1));//输出文件名
            // 开始下载
            EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    downloadFile(url, file);
                    isStart = false;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件下载
     *
     * @param downloadUrl
     * @param file
     */
    private void downloadFile(String downloadUrl, final File file) {
        FileOutputStream _outputStream;//文件输出流
        try {
            _outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        InputStream _inputStream = null;//文件输入流
        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection _downLoadCon = (HttpURLConnection) url.openConnection();
            _downLoadCon.setRequestMethod("GET");
            fileLength = Integer.valueOf(_downLoadCon.getHeaderField("Content-Length"));//文件大小
            if (file.exists() && file.length() == fileLength) {
                Log.e("Harrison", "文件存在");
                if (downloadListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            downloadListener.downloadFinish(file.getAbsolutePath());
                        }
                    });
                }

                return;
            }
            _inputStream = _downLoadCon.getInputStream();
            int respondCode = _downLoadCon.getResponseCode();//服务器返回的响应码
            if (respondCode == 200) {
                byte[] buffer = new byte[1024 * 8];// 数据块，等下把读取到的数据储存在这个数组，这个东西的大小看需要定，不要太小。
                int len;
                while ((len = _inputStream.read(buffer)) != -1) {
                    _outputStream.write(buffer, 0, len);
                    downloadLength = downloadLength + len;
//                    Log.d(TAG, downloadLength + "/" + fileLength );
                }
                if (downloadListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            downloadListener.downloadFinish(file.getAbsolutePath());
                        }
                    });
                }
            } else {
                Log.d(TAG, "respondCode:" + respondCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {//别忘了关闭流
                if (_outputStream != null) {
                    _outputStream.close();
                }
                if (_inputStream != null) {
                    _inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //    private Runnable run = new Runnable() {
//        public void run() {
//            int _pec=(int) (downloadLength*100 / fileLength);
//
//            handler.postDelayed(run, 1000);
//        }
//    };
    private DownloadListener downloadListener;

    public void setDownloadListener(DownloadListener listener) {
        downloadListener = listener;

    }

    public interface DownloadListener {
        void downloadFinish(String path);

    }


}
