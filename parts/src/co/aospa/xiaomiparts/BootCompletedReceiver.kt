/*
 * SPDX-FileCopyrightText: 2018 The LineageOS Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.xiaomiparts

import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import co.aospa.xiaomiparts.camera.NfcCameraService
import co.aospa.xiaomiparts.display.ColorService
import co.aospa.xiaomiparts.display.DcDimmingService
import co.aospa.xiaomiparts.doze.PocketService
import co.aospa.xiaomiparts.gestures.GestureUtils
import co.aospa.xiaomiparts.thermal.ThermalUtils
import co.aospa.xiaomiparts.touch.HighTouchPollingService
import co.aospa.xiaomiparts.touch.TouchFeatureWrapper // Import the wrapper
import co.aospa.xiaomiparts.touch.TouchOrientationService
import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import vendor.xiaomi.hw.touchfeature.ITouchFeature

/** Everything begins at boot. */
class BootCompletedReceiver : BroadcastReceiver() {

    private var xiaomiTouchFeature: ITouchFeature? = null
    private lateinit var contentObserver: ContentObserver

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent: ${intent.action}")
        if (intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED) return

        Log.i(TAG, "Boot completed, starting services")

        // Start original services
        ColorService.startService(context)
        DcDimmingService.startService(context)
        PocketService.startService(context)
        NfcCameraService.startService(context)
        TouchOrientationService.startService(context)
        HighTouchPollingService.startService(context)
        ThermalUtils.getInstance(context).startService()
        GestureUtils.onBootCompleted(context)
<<<<<<< HEAD

        // Set up and monitor Double Tap to Wake
        setupTapToWake(context)
    }

    private fun setupTapToWake(context: Context) {
        val resolver: ContentResolver = context.contentResolver
        val observer: ContentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                updateTapToWakeStatus(context)
            }
        }
        
        resolver.registerContentObserver(
            Settings.Secure.getUriFor(Settings.Secure.DOUBLE_TAP_TO_WAKE), true, observer
        )
        
        // Update Tap to Wake status on boot
        updateTapToWakeStatus(context)
    }

    private fun updateTapToWakeStatus(context: Context) {
        val enabled = Settings.Secure.getInt(
            context.contentResolver, Settings.Secure.DOUBLE_TAP_TO_WAKE, 0
        ) == 1
        
        Log.i(TAG, "Tap to Wake set to " + if (enabled) "enabled" else "disabled")
        TouchFeatureWrapper.setTouchMode(DOUBLE_TAP_TO_WAKE_MODE, if (enabled) 1 else 0)
=======
        setupDt2w(context)
>>>>>>> e8abad1 (Fixed Trees)
    }
    
    private fun setupDt2w(context: Context) {
    contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            updateDt2wStatus(context)
        }
    }

    context.contentResolver.registerContentObserver(
        Settings.Secure.getUriFor(Settings.Secure.DOUBLE_TAP_TO_WAKE),
        true,
        contentObserver
    )

    // Update status once at boot
    updateDt2wStatus(context)
    }

    private fun updateDt2wStatus(context: Context) {
    if (xiaomiTouchFeature == null) {
        try {
            // This is the correct way to get an AIDL service
            val binder = android.os.ServiceManager.getService(
                "vendor.xiaomi.hw.touchfeature.ITouchFeature/default"
            )
            xiaomiTouchFeature = ITouchFeature.Stub.asInterface(binder)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to get ITouchFeature service", e)
            return
        }
    }

    val enabled = Settings.Secure.getInt(
        context.contentResolver,
        Settings.Secure.DOUBLE_TAP_TO_WAKE,
        0
    ) == 1

    try {
        if (xiaomiTouchFeature != null) {
            // Call the setTouchMode function from our AIDL
            // Mode 14 is for DT2W
            xiaomiTouchFeature?.setTouchMode(0, 14, if (enabled) 1 else 0)
            Log.i(TAG, "Set DT2W to $enabled")
        } else {
            Log.e(TAG, "Cannot set DT2W status, service instance is null")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to set DT2W status", e)
    }
}

    companion object {
        private const val TAG = "XiaomiParts-BCR"
        private const val DOUBLE_TAP_TO_WAKE_MODE = 14
    }
}