package com.zd.imagetovideo.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.*;

/**
 * Create by zhangdong 2019/9/20
 */
public class FileUtil {
    private static final String LOG_TAG = FileUtil.class.getSimpleName();

    /**
     * 从assets目录读取文件并保存到sdcard
     *
     * @param context 上下文
     * @param savePath 保存文件路径
     * @return 保存后的文件
     */
    public static void getAssetsFile(Context context, String savePath) {
        if (context == null || TextUtils.isEmpty(savePath)) {
            return;
        }
        File destDir = new File(savePath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        } else if (destDir.isFile()) {
            destDir.delete();
            destDir.mkdirs();
        }
        AssetManager assetManager = context.getAssets();
        String[] assetsList = null;
        try {
            assetsList = assetManager.list("video_images");
        }  catch (IOException e) {
            Log.e(LOG_TAG, e.toString());
        }
        if (assetsList == null || assetsList.length <= 0) {
            return;
        }
        for (String name : assetsList) {
            Log.i(LOG_TAG, "image name : " + name);
            File targetFile = new File(savePath + File.separator + name);
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open("video_images/" + name);
                out = new FileOutputStream(targetFile);
                byte[] buffer = new byte[2048];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                Log.i(LOG_TAG, "copyFileFromAssets, success");
            } catch (IOException e) {
                targetFile.delete(); //拷贝过程中出现异常则删除
                Log.e(LOG_TAG, "copyFileFromAssets IOException :{}", e);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.toString());
                }
            }
        }


    }
}
