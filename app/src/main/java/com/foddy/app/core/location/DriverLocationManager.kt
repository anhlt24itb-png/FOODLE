package com.foddy.app.core.location

import android.location.Location
import javax.inject.Inject
import javax.inject.Singleton
//quản lý và tối ưu hóa tần suất cập nhật vị trí GPS của Tài xế
@Singleton
class DriverLocationManager @Inject constructor() {
    private var lastLocation: Location? = null
    private var lastUpdateTime: Long = 0L

    private val MIN_DISTANCE_METERS = 20f
    private val MIN_TIME_INTERVAL_MS = 5000L

    fun shouldUpdateLocation(newLocation: Location): Boolean {
        val currentTime = System.currentTimeMillis()
        
        // Always update if it's the first location
        if (lastLocation == null) {
            lastLocation = newLocation
            lastUpdateTime = currentTime
            return true
        }

        val distance = newLocation.distanceTo(lastLocation!!)
        val timeElapsed = currentTime - lastUpdateTime

        return if (distance >= MIN_DISTANCE_METERS || timeElapsed >= MIN_TIME_INTERVAL_MS) {
            lastLocation = newLocation
            lastUpdateTime = currentTime
            true
        } else {
            false
        }
    }

    fun reset() {
        lastLocation = null
        lastUpdateTime = 0L
    }
}
