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
 *
 */

package org.cyanogenmod.changelog;

import android.os.Build;
import android.util.Log;

public class Device {
    /**
     * The manufacturer of the product/hardware.
     */
    public final static String manufacturer = Build.MANUFACTURER.toLowerCase();

    /**
     * The name of the hardware (from the kernel command line or /proc).
     */
    public final static String hardware = Build.HARDWARE.toLowerCase();

    /**
     * The name of the underlying board.
     */
    public final static String board = Build.BOARD.toLowerCase();

    /**
     * The device code-name (e.g. hammerhead).
     */
    public final static String device = Build.DEVICE.toLowerCase();

    /**
     * The full CyanogenMod build version string. The value is determined by the output of getprop ro.cm.version.
     */
    public final static String CMVersion;

    /**
     * The CyanogenMod version of the device (e.g 13).
     */
    public final static String CMNumber;

    /**
     * The CyanogenMod release channel (e.g nightly).
     */
    public final static String CMReleaseChannel;

    /**
     * The date when this build was compiled. The value is determined by the output of getprop ro.build.date.
     */
    public final static String buildDate;

    /**
     * Common repositories.
     */
    public static final String[] COMMON_REPOS = {
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
    public static final String[] COMMON_REPOS_QCOM = {
            "android_device_qcom_common",
            "android_device_qcom_sepolicy",
    };

    /**
     * Logcat Tag
     */
    private final static String TAG = "Device";

    static {
        CMVersion = Cmd.exec("getprop ro.cm.version").replace("\n", "");
        String[] version = CMVersion.split("-");
        CMNumber = version[0];
        CMReleaseChannel = version[2];
        buildDate = Cmd.exec("getprop ro.build.date").replace("\n", "");
        Log.v(TAG, "Device" +
                "{ manufacturer=" + manufacturer +
                ", hardware=" + hardware +
                ", board=" + board +
                ", device=" + device +
                ", CMVersion=" + CMVersion +
                ", CMNumber=" + CMNumber +
                ", CMReleaseChannel=" + CMReleaseChannel +
                ", buildDate=" + buildDate +
                "}");
    }

}
