package com.zd.imagetovideo.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import com.zd.imagetovideo.R
import java.util.ArrayList

/**
 * Create by zhangdong 2019/9/20
 */
object PermissionMediator {
    private val TAG = PermissionMediator::class.java.simpleName

    // FragmentActivity要求request code为8位
    private var sIncRequestCode : Int = 0

    private val mListenerMap = SparseArray<OnPermissionRequestListener>()

    /**
     * 权限申请回调listener
     */
    interface OnPermissionRequestListener {
        /**
         * 单条权限申请回调
         * 多条权限申请时也需要回调此方法，因为只申请非授权的权限
         */
        fun onPermissionRequest(granted: Boolean, permission: String)

        /**
         * 多条权限申请回调
         */
        fun onPermissionRequest(isAllGranted: Boolean, permissions: Array<String>?, grantResults: IntArray?)
    }

    abstract class DefaultPermissionRequest : PermissionMediator.OnPermissionRequestListener {
        override fun onPermissionRequest(granted: Boolean, permission: String) {

        }

        override fun onPermissionRequest(isAllGranted: Boolean, permissions: Array<String>?, grantResults: IntArray?) {

        }
    }

    fun dispatchPermissionResult(activity: Activity, requestCode: Int, permissions: Array<String>?, grantResults: IntArray) {
        if (permissions == null || permissions.isEmpty() || grantResults.isEmpty()) {
            return
        }
        for (index in permissions.indices) {
            Log.i(TAG, "request $permissions[index], ${grantResults[index] == PackageManager.PERMISSION_GRANTED}")
            if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[index])) {
                    //用户之前拒绝，并勾选不再提示时，引导用户去设置页面开启权限
                    showGrantToast(activity.applicationContext, permissions[index])
                }
            }
        }
        val listener = mListenerMap[requestCode]
        if (listener != null) {
            if (permissions.size == 1) {
                listener.onPermissionRequest(grantResults[0] == PackageManager.PERMISSION_GRANTED, permissions[0])
            } else {
                var isAllGranted = true
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        isAllGranted = false
                        listener.onPermissionRequest(false, permissions, grantResults)
                        break
                    }
                }
                if (isAllGranted) {
                    listener.onPermissionRequest(true, permissions, grantResults)
                }
            }
            mListenerMap.remove(requestCode)
        }
    }

    fun checkPermission(activity: Activity, permission: String, listener: OnPermissionRequestListener) {
        checkPermission(activity, arrayOf(permission), listener)
    }

    fun checkPermission(activity: Activity?, permissions: Array<String>?, listener: OnPermissionRequestListener?) {
        if (activity == null || permissions == null || permissions.size <= 0) {
            return
        }
        val unauthorizedPermissions = ArrayList<String>()
        for (permission in permissions) {
            if (TextUtils.isEmpty(permission)) {
                continue
            }
            try {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "request {}$permission")
                    unauthorizedPermissions.add(permission)
                } else {
                    Log.i(TAG, "already has {} permission$permission")
                }
            } catch (e: Exception) {
                Log.e(TAG, "check self permission failed. {}", e)
                unauthorizedPermissions.add(permission)
            }

        }
        if (!unauthorizedPermissions.isEmpty()) {
            sIncRequestCode++
            if (sIncRequestCode > 255) {
                sIncRequestCode = 0
            }
            mListenerMap.put(sIncRequestCode, listener)
            ActivityCompat.requestPermissions(activity, unauthorizedPermissions.toTypedArray(), sIncRequestCode)
        } else {
            if (listener != null) {
                if (permissions.size == 1) {
                    listener.onPermissionRequest(true, permissions[0])
                } else {
                    listener.onPermissionRequest(true, permissions, null)
                }
            }
        }
    }

    fun checkPermission(activity: Context, permission: String, listener: OnPermissionRequestListener) {
        checkPermission(activity, arrayOf(permission), listener)
    }

    private fun checkPermission(activity: Context?, permissions: Array<String>?, listener: OnPermissionRequestListener?) {
        if (activity == null || permissions == null || permissions.isEmpty()) {
            return
        }
        val unauthorizedPermissions = ArrayList<String>()
        for (permission in permissions) {
            if (TextUtils.isEmpty(permission)) {
                continue
            }
            try {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "request {}$permission")
                    unauthorizedPermissions.add(permission)
                } else {
                    Log.i(TAG, "already has {} permission$permission")
                }
            } catch (e: Exception) {
                Log.e(TAG, "check self permission failed. {}", e)
                unauthorizedPermissions.add(permission)
            }

        }
        if (!unauthorizedPermissions.isEmpty()) {
            sIncRequestCode++
            if (sIncRequestCode > 255) {
                sIncRequestCode = 0
            }
            mListenerMap.put(sIncRequestCode, listener)
            if (activity !is Activity) {
                return
            }
            ActivityCompat.requestPermissions(activity, unauthorizedPermissions.toTypedArray(), sIncRequestCode)
        } else {
            if (listener != null) {
                if (permissions.size == 1) {
                    listener.onPermissionRequest(true, permissions[0])
                } else {
                    listener.onPermissionRequest(true, permissions, null)
                }
            }
        }
    }

    /**
     * 判断是否拒绝权限并勾选不再提示
     */
    fun permissionIsRefuseAndHide(activity: Activity, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true
            }
        }
        return false
    }

    private fun showGrantToast(context: Context, unauthorizedPermission: String) {
        if (TextUtils.isEmpty(unauthorizedPermission)) {
            return
        }
        when (unauthorizedPermission) {
            Manifest.permission.CALL_PHONE -> DialogUtil.showShortPromptToast(context, R.string.grant_permission_phone_call)
            Manifest.permission.CAMERA -> DialogUtil.showShortPromptToast(context, R.string.grant_permission_camera)
            Manifest.permission.RECORD_AUDIO -> DialogUtil.showShortPromptToast(context, R.string.grant_permission_audio)
            Manifest.permission.READ_CONTACTS -> DialogUtil.showShortPromptToast(context, R.string.grant_permission_read_contact)
        }
    }

    fun showGrantLocationToast(context: Context) {
        DialogUtil.showShortPromptToast(context, R.string.grant_permission_location)
    }
}