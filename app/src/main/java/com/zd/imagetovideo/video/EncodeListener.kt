package com.zd.imagetovideo.video

/**
 * Create by zhangdong 2019/9/20
 */
interface EncodeListener {
    /**
     * Called to notify progress.
     *
     * @param progress Progress in [0.0, 1.0] range, or negative value if progress is unknown.
     */
    fun onProgress(progress: Float)

    /**
     * Called when transcode completed.
     */
    fun onCompleted()

    fun onFailed(exception: Exception)

}