package com.zd.imagetovideo

import android.Manifest
import android.os.Environment

import java.io.File

/**
 * Create by zhangdong 2019/9/20
 */
interface GlobalConstant {
    companion object {
        /**
         * APP文件夹路径
         */
        val FILE_FOLDER = Environment.getExternalStorageDirectory()
            .absolutePath + File.separator + "azd_magic"

        /**
         * 图片的文件夹
         */
        val FILE_PICTRUES_FLODER = FILE_FOLDER + File.separator + "pictures"

        /**
         * 配置文件
         */
        val FILE_CONFIG = FILE_FOLDER + File.separator + "config.txt"

        /**
         * 生成的3D模型的存放路径
         */
        val FILE_PLY_FLODER = FILE_FOLDER + File.separator + "ply"
        /**
         * 截屏图片的位置
         */
        val FILE_SCREEN_FLODER = FILE_FOLDER + File.separator + "screenshoot"
        /**
         * 视频存放路径
         */
        val FILE_VIDEO_FLODER = FILE_FOLDER + File.separator + "video"

        /**
         * 公司的主页
         */
        val URL_HOME = "http://www.moyansz.com"
        /**
         * 关键词
         * 带给显示的页面的路径
         */
        val EXTRA_PLY_PATH = "extra_ply_path"
        /**
         * 显示页面是否需要分享
         */
        val EXTRA_PLY_SHARE = "extra_ply_share"

        /**
         * 权限申请
         * SD卡的读取和写入
         */
        val PERMS_EXTERNAL_STORAGE =
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        val PERMS_EXTERNAL_STORAGE_CAMERA = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}
