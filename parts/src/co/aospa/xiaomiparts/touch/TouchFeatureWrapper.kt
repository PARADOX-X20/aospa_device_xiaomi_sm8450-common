/*
 * SPDX-FileCopyrightText: 2023-2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.xiaomiparts.touch

import android.os.IBinder
import android.os.ServiceManager
import android.util.Log
import co.aospa.xiaomiparts.utils.dlog
import vendor.xiaomi.hw.touchfeature.ITouchFeature

/** Convenient wrapper around xiaomi touchfeature AIDL interface. */
object TouchFeatureWrapper {
    private const val TAG = "TouchFeatureWrapper"
    private val SERVICE_NAME = "${ITouchFeature.DESCRIPTOR}/default"

    @Volatile private var touchFeature: ITouchFeature? = null

    private val deathRecipient =
        IBinder.DeathRecipient {
            dlog(TAG, "serviceDied")
            touchFeature = null
        }

    @Synchronized
    private fun getTouchFeature(): ITouchFeature? {
        if (touchFeature == null) {
            try {
                val binder = ServiceManager.getService(SERVICE_NAME)
                if (binder != null) {
                    touchFeature = ITouchFeature.Stub.asInterface(binder)
                    binder.linkToDeath(deathRecipient, 0)
                }
            } catch (e: Exception) {
                Log.e(TAG, "getTouchFeature failed!", e)
            }
        }
        return touchFeature
    }

    fun setTouchMode(mode: Int, value: Int) {
        val service = getTouchFeature()
        if (service == null) {
            Log.e(TAG, "setTouchMode: touchFeature is null!")
            return
        }
        
        dlog(TAG, "set mode=$mode value=$value")
        try {
            service.setTouchMode(0, mode, value)
        } catch (e: Exception) {
            Log.e(TAG, "setTouchMode failed!", e)
        }
    }
}