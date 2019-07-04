package com.owoh.video.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * @author LLhon
 * @Project diaoyur_android
 * @Package com.kangoo.util.video
 * @Date 2018/4/11 12:25
 * @description 视频工具类
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class VideoUtil {

    private static final String TAG = VideoUtil.class.getSimpleName();
    public static final int VIDEO_MAX_DURATION = 10;// 15秒
    public static final int MIN_TIME_FRAME = 3;
    private static final long one_frame_time = 1000000;
    public static final String POSTFIX = ".jpeg";
    private static final String TRIM_PATH = "small_video";
    private static final String THUMB_PATH = "thumb";


    /**
     * 复制文件
     */
    public static void copyFile(final String from, final String destination) throws IOException {
        FileInputStream in = new FileInputStream(from);
        FileOutputStream out = new FileOutputStream(destination);
        copy(in, out);
        in.close();
        out.close();
    }

    public static void copy(FileInputStream in, FileOutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
    }


    public static String getVideoFilePath(String url) {

        if (TextUtils.isEmpty(url) || url.length() < 5)
            return "";

        if (url.substring(0, 4).equalsIgnoreCase("http")) {
        } else
            url = "file://" + url;

        return url;
    }

    public static String convertSecondsToTime(long seconds) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (seconds <= 0)
            return "00:00";
        else {
            minute = (int) seconds / 60;
            if (minute < 60) {
                second = (int) seconds % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = (int) (seconds - hour * 3600 - minute * 60);
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    public static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }

    /**
     * 裁剪视频本地路径
     *
     * @param context
     * @param dirName
     * @param fileNamePrefix
     * @return
     */
    public static String getTrimmedVideoPath(Context context, String dirName, String fileNamePrefix) {
        String finalPath = "";
        String dirPath = "";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            dirPath = context.getExternalCacheDir() + File.separator + dirName; // /mnt/sdcard/Android/data/<package name>/files/...
        } else {
            dirPath = context.getCacheDir() + File.separator + dirName; // /data/data/<package name>/files/...
        }
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        finalPath = file.getAbsolutePath();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String outputName = fileNamePrefix + timeStamp + ".mp4";
        finalPath = finalPath + "/" + outputName;
        return finalPath;
    }

    /**
     * 裁剪视频本地目录路径
     */
    public static String getTrimmedVideoDir(Context context, String dirName) {
        String dirPath = "";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            dirPath = context.getExternalCacheDir() + File.separator
                    + dirName; // /mnt/sdcard/Android/data/<package name>/files/...
        } else {
            dirPath = context.getCacheDir() + File.separator
                    + dirName; // /data/data/<package name>/files/...
        }
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return dirPath;
    }

    public static String saveImageToSD(Bitmap bmp, String dirPath) {
        if (bmp == null) {
            return "";
        }
        File appDir = new File(dirPath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }


    public static String saveImageToSDForEdit(Bitmap bmp, String dirPath, String fileName) {
        if (bmp == null) {
            return "";
        }
        File appDir = new File(dirPath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    public static void deleteFile(File f) {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; ++i) {
                    deleteFile(files[i]);
                }
            }
        }
        f.delete();
    }

    public static String getSaveEditThumbnailDir(Context context) {
        String state = Environment.getExternalStorageState();
        File rootDir =
                state.equals(Environment.MEDIA_MOUNTED) ? context.getExternalCacheDir()
                        : context.getCacheDir();
        File folderDir = new File(rootDir.getAbsolutePath() + File.separator + TRIM_PATH + File.separator + THUMB_PATH);
        if (folderDir == null) {
            folderDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator + "videoeditor" + File.separator + "picture");
        }
        if (!folderDir.exists() && folderDir.mkdirs()) {

        }
        return folderDir.getAbsolutePath();
    }
}
