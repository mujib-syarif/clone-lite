package com.mujib.clonelite

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

class CloneAdminReceiver : DeviceAdminReceiver() {
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        try {
            val manager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            manager.setProfileEnabled(ComponentName(context, CloneAdminReceiver::class.java))
        } catch (error: Exception) {
            Log.e("CloneLite", "Could not enable work profile", error)
        }
    }
}
