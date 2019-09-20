package com.zd.imagetovideo

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import com.zd.imagetovideo.util.BitmapUtil
import com.zd.imagetovideo.util.FileUtil
import com.zd.imagetovideo.util.PermissionMediator
import com.zd.imagetovideo.video.EncodeListener
import com.zd.imagetovideo.video.ImageToVideoConverter
import com.zd.imagetovideo.video.VideoSize
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private var imageToVideo: ImageToVideoConverter? = null
    private var oneProgressBar: ProgressBar? = null
    private var twoProgressBar: ProgressBar? = null
    private var onePlayView: Button? = null
    private var twoPlayView: Button? = null
    private var oneVideoPath: String? = null
    private var videoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()
        initData()
    }

    private fun initData() {
        oneVideoPath = GlobalConstant.FILE_FOLDER + File.separator + "plan_one_image_video.mp4"
        videoPath = GlobalConstant.FILE_FOLDER + "/" + "plan_two_image_video.mp4"
        oneProgressBar = findViewById(R.id.main_progress_bar)
        twoProgressBar = findViewById(R.id.progress_bar)
        onePlayView = findViewById(R.id.main_btn_play)
        twoPlayView = findViewById(R.id.play)
        main_btn_start?.setOnClickListener {
            Handler().post {
                performCodec()
            }
        }
        start?.setOnClickListener {
            startImageToVideo()
        }
        onePlayView?.setOnClickListener {
            oneVideoPath?.let { path ->
                val uri = Uri.parse(path)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setDataAndType(uri, "video/mp4")
                startActivity(intent)
            }
        }
        twoPlayView?.setOnClickListener {
            videoPath?.let { path ->
                val uri = Uri.parse(path)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setDataAndType(uri, "video/mp4")
                startActivity(intent)
            }
        }
    }

    private fun startImageToVideo() {
        val rootFile = File(GlobalConstant.FILE_FOLDER)
        if (!rootFile.exists()) {
            rootFile.mkdir()
        }
        FileUtil.getAssetsFile(this, GlobalConstant.FILE_PICTRUES_FLODER)
        twoPlayView?.isEnabled = false
        videoPath?.let { outputPath ->
            imageToVideo = ImageToVideoConverter(
                outputPath = outputPath,
                inputImagePath = GlobalConstant.FILE_PICTRUES_FLODER + File.separator + "image_1.jpg",
                size = VideoSize(720, 720),
                duration = TimeUnit.SECONDS.toMicros(4),
                listener = object : EncodeListener {
                    override fun onProgress(progress: Float) {
                        Log.d("progress", "progress = $progress")
                        runOnUiThread {
                            twoProgressBar?.progress = (progress * 100).toInt()
                        }
                    }

                    override fun onCompleted() {
                        runOnUiThread {
                            twoPlayView?.isEnabled = true
                            twoProgressBar?.progress = 100
                            findViewById<Button>(R.id.play).isEnabled = true
                        }
                        exportMp4ToGallery(applicationContext, GlobalConstant.FILE_FOLDER)
                    }

                    override fun onFailed(exception: Exception) {

                    }
                }
            )
        }
        imageToVideo?.start()
    }

    private fun exportMp4ToGallery(context: Context, filePath: String) {
        val values = ContentValues(2)
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        values.put(MediaStore.Video.Media.DATA, filePath)
        // MediaStore
        context.contentResolver.insert(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values
        )
        context.sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://$filePath")
            )
        )
    }

    private fun performCodec() {
        onePlayView?.isEnabled = false
        try {
            Log.e("performJcodec: ", "执行开始")
            var se: SequenceEncoderMp4? = null
            val rootFile = File(GlobalConstant.FILE_FOLDER)
            if (!rootFile.exists()) {
                rootFile.mkdir()
            }
            FileUtil.getAssetsFile(this, GlobalConstant.FILE_PICTRUES_FLODER)
            val file = File(GlobalConstant.FILE_PICTRUES_FLODER)

            val out = File(oneVideoPath ?: "")
            se = SequenceEncoderMp4(out)

            val files = file.listFiles() ?: return
            val size = files.size
            var index = 1
            for (i in files.indices) {
                if (!files[i].exists()) {
                    break
                }
                val frame = BitmapUtil.decodeSampledBitmapFromFile(files[i].absolutePath, 480, 320)

                se.encodeImage(frame)
                Log.e("performJcodec: ", "执行到的图片是 $i")
                runOnUiThread {
                    oneProgressBar?.progress = ((index / size).toFloat() * 100).toInt()
                }
                index++
            }
            se.finish()
            Log.e("performJcodec: ", "执行完成")
            onePlayView?.isEnabled = true
            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(out)))
        } catch (e: IOException) {
            Log.e("performJcodec: ", "执行异常 " + e.toString())
        }

    }

    private fun requestPermission() {
        //文件存取
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        PermissionMediator.checkPermission(this, permissions, object : PermissionMediator.DefaultPermissionRequest() {
            override fun onPermissionRequest(granted: Boolean, permission: String) {
                if (!granted) {
                    finish()
                }
            }

            override fun onPermissionRequest(
                isAllGranted: Boolean,
                permissions: Array<String>?,
                grantResults: IntArray?
            ) {
                if (!isAllGranted) {
                    finish()
                }
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionMediator.dispatchPermissionResult(this, requestCode, permissions, grantResults)
    }
}
