package org.cyanogenmod.changelog

import android.annotation.SuppressLint

internal object Utils {
    @SuppressLint("PrivateApi")
    fun getSystemProperty(key: String): String {
        return Class.forName("android.os.SystemProperties")
                .getMethod("get", String::class.java).invoke(null, key) as String
    }
}
