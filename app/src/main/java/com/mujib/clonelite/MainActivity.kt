package com.mujib.clonelite

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.UserManager
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var content: LinearLayout
    private val spacing = 16

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render()
    }

    override fun onResume() {
        super.onResume()
        if (::content.isInitialized) render()
    }

    private fun render() {
        val userManager = getSystemService(UserManager::class.java)
        val isWorkProfile = userManager?.isManagedProfile == true
        val hasWorkProfile = !isWorkProfile && (userManager?.userProfiles?.size ?: 1) > 1

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 28, 24, 20)
            setBackgroundColor(0xFFF6F7FB.toInt())
        }
        root.addView(TextView(this).apply {
            text = "CloneLite"
            textSize = 28f
            setTextColor(0xFF182033.toInt())
            typeface = Typeface.DEFAULT_BOLD
        })
        root.addView(TextView(this).apply {
            text = when {
                isWorkProfile -> "Work profile · Separate app space"
                hasWorkProfile -> "Work profile already exists"
                else -> "Simple dual-app setup using Android"
            }
            textSize = 14f
            setTextColor(0xFF596273.toInt())
            setPadding(0, 6, 0, 18)
        })

        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(content) }
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        when {
            isWorkProfile -> {
                addText(content, "Apps in this profile", 20f, true)
                addText(content, "Install apps into this work profile to keep their data and logins separate from your personal apps.", 14f, false)
                addButton(content, "Open Play Store in this profile") { openWorkStore() }
                addButton(content, "Refresh app list") { render() }
                listLaunchableApps(content)
            }
            hasWorkProfile -> {
                addText(content, "Your Work Profile is already set up", 20f, true)
                addText(content, "Android reports that a separate profile already exists. Do not set it up again: Android may offer to delete the existing profile and its data.", 15f, false)
                addText(content, "Next steps: return to your home screen, open the app drawer, find the Work tab or apps with a briefcase badge, then open Play Store there and install Clash of Clans. Launch CloneLite with the briefcase badge to manage apps inside the Work Profile.", 15f, false)
                addButton(content, "Open home screen") { openHomeScreen() }
                addButton(content, "Check profile status again") { render() }
            }
            else -> {
                addText(content, "Create a separate app space", 20f, true)
                addText(content, "CloneLite uses Android's built-in work profile. Apps installed there keep separate app data and logins from your personal profile.", 15f, false)
                addButton(content, "Set up work profile") { startWorkProfileSetup() }
                addText(content, "Requirements: device support, screen lock/encryption when requested, and no existing work profile. Android normally allows only one managed profile.", 13f, false)
            }
        }
        setContentView(root)
    }

    private fun startWorkProfileSetup() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS)) {
            toast("Perangkat ini tidak menyediakan fitur Android Work Profile.")
            return
        }
        val manager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if (!manager.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)) {
            toast("Setup tidak tersedia. Work Profile mungkin sudah ada atau perangkat membatasi provisioning.")
            render()
            return
        }
        val admin = ComponentName(this, CloneAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE).apply {
            putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, admin)
        }
        if (intent.resolveActivity(packageManager) == null) {
            toast("Wizard Work Profile Android tidak ditemukan.")
            return
        }
        try {
            startActivityForResult(intent, REQUEST_PROVISION_PROFILE)
        } catch (error: Exception) {
            toast("Tidak bisa memulai setup: " + (error.localizedMessage ?: "ditolak Android"))
        }
    }

    @Deprecated("Legacy managed-profile provisioning callback for Android 11")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PROVISION_PROFILE) {
            toast(if (resultCode == Activity.RESULT_OK) "Wizard selesai. Tunggu Android menyelesaikan Work Profile." else "Setup dibatalkan atau tidak memenuhi syarat.")
            render()
        }
    }

    private fun listLaunchableApps(target: LinearLayout) {
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = packageManager.queryIntentActivities(query, 0)
            .filter { it.activityInfo.packageName != packageName }
            .distinctBy { it.activityInfo.packageName }
            .sortedBy { it.loadLabel(packageManager).toString().lowercase() }
        if (apps.isEmpty()) {
            addText(target, "Belum ada aplikasi lain di profil ini. Buka Play Store dengan badge tas kerja untuk memasang CoC.", 14f, false)
            return
        }
        for (app in apps) {
            val label = app.loadLabel(packageManager).toString()
            addButton(target, "Open · $label") {
                val launch = packageManager.getLaunchIntentForPackage(app.activityInfo.packageName)
                if (launch != null) {
                    try { startActivity(launch) } catch (_: Exception) { toast("Aplikasi tidak bisa dibuka dari profil ini.") }
                } else toast("Launcher aplikasi tidak tersedia.")
            }
        }
    }

    private fun openWorkStore() {
        val playStore = packageManager.getLaunchIntentForPackage("com.android.vending")
        try {
            if (playStore != null) startActivity(playStore)
            else startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps")))
        } catch (_: Exception) { toast("Play Store tidak tersedia di profil ini.") }
    }

    private fun openHomeScreen() {
        try {
            startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME))
        } catch (_: Exception) {
            toast("Tidak bisa membuka layar utama.")
        }
    }

    private fun addText(target: LinearLayout, value: String, size: Float, bold: Boolean) {
        target.addView(TextView(this).apply {
            text = value
            textSize = size
            setTextColor(if (bold) 0xFF182033.toInt() else 0xFF596273.toInt())
            if (bold) typeface = Typeface.DEFAULT_BOLD
            setPadding(0, spacing, 0, spacing)
        })
    }

    private fun addButton(target: LinearLayout, label: String, action: () -> Unit) {
        target.addView(Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 15f
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = 8
        })
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val REQUEST_PROVISION_PROFILE = 4102
    }
}
