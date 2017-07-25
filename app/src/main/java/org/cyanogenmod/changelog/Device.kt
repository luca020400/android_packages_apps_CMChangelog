/*
 * Copyright (c) 2016 The CyanogenMod Project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cyanogenmod.changelog

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import java.util.*

/**
 * Information about the device and the current build.
 */
internal object Device {

    /**
     * The manufacturer of the product/hardware. (e.g lge)
     */
    val MANUFACTURER = Build.MANUFACTURER.toLowerCase(Locale.getDefault())
    /**
     * The name of the hardware (from the kernel command line or /proc).
     */
    val HARDWARE = Build.HARDWARE.toLowerCase(Locale.getDefault())
    /**
     * The name of the underlying board.
     */
    val BOARD = Build.BOARD.toLowerCase(Locale.getDefault())
    /**
     * The DEVICE code-name (e.g. hammerhead).
     */
    val DEVICE = Build.PRODUCT.toLowerCase(Locale.getDefault())
    /**
     * The full CyanogenMod build version string. The value is determined by the output of getprop ro.cm.version.
     */
    val LINEAGE_VERSION: String = Utils.getSystemProperty("ro.cm.version")
    /**
     * The CyanogenMod release channel (e.g NIGHTLY).
     */
    val LINEAGE_RELEASE_CHANNEL: String
    /**
     * Git CM_BRANCH of this build
     */
    val LINEAGE_BRANCH: String
    /**
     * The date when this build was compiled. The value is determined by the output of getprop ro.build.date.
     */
    val BUILD_DATE = Utils.getSystemProperty("ro.build.date")
    /**
     * Common repositories.
     */
    val COMMON_REPOS = arrayOf(
            "android_hardware_akm",
            "android_hardware_broadcom_libbt",
            "android_hardware_broadcom_wlan",
            "android_hardware_cm",
            "android_hardware_cyanogen",
            "android_hardware_invensense",
            "android_hardware_libhardware",
            "android_hardware_libhardware_legacy",
            "android_hardware_ril",
            "android_hardware_sony_thermanager",
            "android_hardware_sony_timekeep")
    /**
     * Common repositories (Qualcomm boards only).
     */
    val COMMON_REPOS_QCOM = arrayOf(
            "android_device_qcom_common",
            "android_device_qcom_sepolicy")

    init {
        /* Validate LINEAGE_VERSION */
        if (LINEAGE_VERSION.isEmpty()) {
            LINEAGE_BRANCH = LINEAGE_VERSION
            LINEAGE_RELEASE_CHANNEL = LINEAGE_BRANCH
        } else {
            val version = LINEAGE_VERSION.split("-".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            LINEAGE_RELEASE_CHANNEL = version[2]
            LINEAGE_BRANCH = "cm-" + version[0]
        }
    }

    /**
     * Check if the device is connected to internet, return true if the device has data connection.
     * A valid application Context must be specified.

     * @param c the Context holding the global information about the application environment.
     * *
     * @return true if device is connected to internet, otherwise returns false.
     */
    fun isConnected(c: Context): Boolean {
        val connectivityManager = c.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}
