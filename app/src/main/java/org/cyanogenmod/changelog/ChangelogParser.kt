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

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

internal class ChangelogParser {

    @Throws(IOException::class)
    fun readJsonStream(`in`: InputStream): List<Change> {
        var changes = listOf<Change>()

        InputStreamReader(`in`, "UTF-8").use { reader ->
            val gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
            changes = gson.fromJson<List<Change>>(reader, object : TypeToken<List<Change>>() {}.type)
        }

        return changes.filter { it.isDeviceSpecific }
    }
}
