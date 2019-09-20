package com.zd.imagetovideo.util

import android.content.Context
import android.text.TextUtils
import android.widget.Toast

/**
 * Create by zhangdong 2019/9/20
 */
object DialogUtil {

    fun showShortPromptToast(context: Context, resid: Int) {
        try {
            showShortPromptToast(context, context.getString(resid))
        } catch (e: Exception) {
        }

    }

    fun showShortPromptToast(context: Context, res: String) {
        var resTemp = res
        if (TextUtils.isEmpty(resTemp)) {
            resTemp = ""
        }
        Toast.makeText(context, resTemp, Toast.LENGTH_SHORT).show()
    }

    fun showLongPromptToast(context: Context, resid: Int) {
        try {
            showLongPromptToast(context, context.getString(resid))
        } catch (e: Exception) {
        }

    }

    fun showLongPromptToast(context: Context, res: String) {
        var resTemp = res
        if (TextUtils.isEmpty(resTemp)) {
            resTemp = ""
        }
        Toast.makeText(context, resTemp, Toast.LENGTH_LONG).show()
    }
}