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

/** Everything begins at boot. */
class BootCompletedReceiver : BroadcastReceiver() {

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
    }

    companion object {
        private const val TAG = "XiaomiParts-BCR"
        private const val DOUBLE_TAP_TO_WAKE_MODE = 14
    }
}