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

package org.cyanogenmod.changelog;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import java.util.Locale;

/**
 * Information about the device and the current build.
 */
class Device {

    /**
     * The manufacturer of the product/hardware. (e.g lge)
     */
    final static String MANUFACTURER = Build.MANUFACTURER.toLowerCase(Locale.getDefault());
    /**
     * The name of the hardware (from the kernel command line or /proc).
     */
    final static String HARDWARE = Build.HARDWARE.toLowerCase(Locale.getDefault());
    /**
     * The name of the underlying board.
     */
    final static String BOARD = Build.BOARD.toLowerCase(Locale.getDefault());
    /**
     * The DEVICE code-name (e.g. hammerhead).
     */
    final static String DEVICE = Build.PRODUCT.toLowerCase(Locale.getDefault());
    /**
     * The full CyanogenMod build version string. The value is determined by the output of getprop ro.cm.version.
     */
    final static String LINEAGE_VERSION;
    /**
     * The CyanogenMod release channel (e.g NIGHTLY).
     */
    final static String LINEAGE_RELEASE_CHANNEL;
    /**
     * Git CM_BRANCH of this build
     */
    final static String LINEAGE_BRANCH;
    /**
     * The date when this build was compiled. The value is determined by the output of getprop ro.build.date.
     */
    final static String BUILD_DATE;
    /**
     * Common repositories.
     */
    static final String[] COMMON_REPOS = {
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
            "android_hardware_sony_timekeep"
    };
    /**
     * Common repositories (Qualcomm boards only).
     */
    static final String[] COMMON_REPOS_QCOM = {
            "android_device_qcom_common",
            "android_device_qcom_sepolicy",
    };

    static {
        LINEAGE_VERSION = Cmd.exec("getprop ro.cm.version").replace("\n", "");
        /* Validate LINEAGE_VERSION */
        if (LINEAGE_VERSION.isEmpty()) {
            LINEAGE_RELEASE_CHANNEL = LINEAGE_BRANCH = LINEAGE_VERSION;
        } else {
            String[] version = LINEAGE_VERSION.split("-");
            LINEAGE_RELEASE_CHANNEL = version[2];
            LINEAGE_BRANCH = "cm-" + version[0];
        }
        BUILD_DATE = Cmd.exec("getprop ro.build.date").replace("\n", "");
    }

    /**
     * Check if the device is connected to internet, return true if the device has data connection.
     * A valid application Context must be specified.
     *
     * @param c the Context holding the global information about the application environment.
     * @return true if device is connected to internet, otherwise returns false.
     */
    static boolean isConnected(Context c) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return !(networkInfo == null || !networkInfo.isConnected());
    }

}
