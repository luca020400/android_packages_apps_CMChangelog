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

import android.os.Build;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Information about the device and the current build.
 */
public class Device {

    /**
     * The manufacturer of the product/hardware. (e.g lge)
     */
    public final static String MANUFACTURER = Build.MANUFACTURER.toLowerCase(Locale.getDefault());

    /**
     * The name of the hardware (from the kernel command line or /proc).
     */
    public final static String HARDWARE = Build.HARDWARE.toLowerCase(Locale.getDefault());

    /**
     * The name of the underlying board.
     */
    public final static String BOARD = Build.BOARD.toLowerCase(Locale.getDefault());

    /**
     * The DEVICE code-name (e.g. hammerhead).
     */
    public final static String DEVICE = Build.DEVICE;

    /**
     * The full CyanogenMod build version string. The value is determined by the output of getprop ro.cm.version.
     */
    public final static String CM_VERSION;

    /**
     * The CyanogenMod version of the device (e.g 13.0).
     */
    public final static String CM_NUMBER;

    /**
     * The CyanogenMod release channel (e.g NIGHTLY).
     */
    public final static String CM_RELEASE_CHANNEL;

    /**
     * Git BRANCH of this build
     */
    public final static String BRANCH;

    /**
     * The date when this build was compiled. The value is determined by the output of getprop ro.build.date.
     */
    public final static String BUILD_DATE;

    /**
     * String value for the nightly release channel
     */
    public final static String RC_NIGHTLY = "NIGHTLY";

    /**
     * String value for the unofficial release channel
     */
    public final static String RC_UNOFFICIAL = "UNOFFICIAL";

    /**
     * String value for the stable release channel
     */
    public final static String RC_SNAPSHOT = "SNAPSHOT";

    /**
     * Collection of device specific projects.
     * The value is determined by the content of the build-manifest.xml, a file that defines all the projects used to
     * build the running build. This file is generated in official builds, unofficial builds may not include it.
     * If build-manifest.xml is not present, the Collection is empty.
     */
    public final static Collection<String> PROJECTS;

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
     * Logcat tag
     */
    private final static String TAG = "Device";

    static {
        CM_VERSION = Cmd.exec("getprop ro.cm.version").replace("\n", "");
        String[] version = CM_VERSION.split("-");
        CM_NUMBER = version[0];
        CM_RELEASE_CHANNEL = version[2];
        if (CM_RELEASE_CHANNEL.equals("SNAPSHOT")) {
            BRANCH = "stable/" + version[3].substring(0, 4);
        } else {
            BRANCH = CM_NUMBER;
        }
        BUILD_DATE = Cmd.exec("getprop ro.build.date").replace("\n", "");
        Log.v(TAG, "Device" +
                " {MANUFACTURER:" + MANUFACTURER +
                ", HARDWARE:" + HARDWARE +
                ", BOARD:" + BOARD +
                ", BRANCH:" + BRANCH +
                ", DEVICE:" + DEVICE +
                ", CM_VERSION:" + CM_VERSION +
                ", CM_NUMBER:" + CM_NUMBER +
                ", CM_RELEASE_CHANNEL:" + CM_RELEASE_CHANNEL +
                ", BUILD_DATE:" + BUILD_DATE + "}");
         /* Parse DEVICE projects from build-manifest.xml */
        String buildManifest = Cmd.exec("cat /etc/build-manifest.xml");
        if (buildManifest.length() == 0) {
            // Cat produced no output
            Log.d(TAG, "Couldn't find a build-manifest.xml.");
            PROJECTS = new LinkedList<>();  //  Empty list
        } else {
            // Cat was successful, parse the output as XML
            Log.d(TAG, "Found build-manifest.xml.");
            PROJECTS = parseBuildManifest(buildManifest);
            Log.v(TAG, "Number of projects: " + PROJECTS.size());
        }
    }

    private static Collection<String> parseBuildManifest(String inputXML) {
        Collection<String> deviceProjects = new ArrayList<>(320);
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(inputXML));
            int eventType = xpp.getEventType();
            do {
                if (eventType == XmlPullParser.END_TAG && xpp.getName().equals("project")) {
                    String value = xpp.getAttributeValue(null, "name"); // may return null value
                    if (value != null && value.contains("CyanogenMod/")) {
                        deviceProjects.add(value);
                    }
                }
            } while ((eventType = xpp.next()) != XmlPullParser.END_DOCUMENT);
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Error while creating XML parser");
            deviceProjects.clear();
        } catch (IOException e) {
            Log.e(TAG, "IOException while parsing XML");
            deviceProjects.clear();
        }
        return deviceProjects;
    }

    private static Date parseDate(String timestamp) {
        Date date = new Date(0);
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            /* Parse UTC date */
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = formatter.parse(timestamp);
            /* Convert from UTC to local time zone *//*
            formatter.setTimeZone(Calendar.getInstance().getTimeZone());
            date = formatter.parse(formatter.format(utcDate));*/
        } catch (ParseException e) {
            Log.e(TAG, "Couldn't parse timestamp.");
        }
        return date;
    }

}
