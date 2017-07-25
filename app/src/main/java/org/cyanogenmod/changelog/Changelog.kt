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

import android.support.annotation.WorkerThread
import android.util.Log
import java.io.BufferedInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.ProtocolException

internal class Changelog(val branch: String) {

    /**
     * List of Changes of this changelog
     */
    var changes = arrayListOf<Change>()

    /**
     * Update this Changelog by retrieving and processing data from API.

     * @param numberOfChanges the minimum number of changes to fetch.
     * *
     * @return true if the Changelog was successfully updated, false if not.
     */
    @WorkerThread
    fun update(numberOfChanges: Int): Boolean {
        val newChanges = arrayListOf<Change>()
        val parser = ChangelogParser()
        val n = 500
        var start = 0 // number of changes to fetch and to skip
        val url = createRestUrl()
        url.setN(n)
        while (newChanges.size < numberOfChanges) {
            url.setStart(start)
            try {
                Log.d(TAG, "Sending GET request to \"" + url.toString() + "\"")
                val connection = url.createUrl().openConnection() as HttpURLConnection
                try {
                    connection.setRequestProperty("Accept", "application/json")
                    connection.requestMethod = "GET"
                    try {
                        BufferedInputStream(connection.inputStream).use { `in` ->
                            newChanges.addAll(parser.readJsonStream(`in`))
                        }
                    } catch (e: IOException) {
                        Log.e(TAG, "Error while parsing received input stream!\n" + e.message)
                        return false
                    }

                } catch (e: ProtocolException) {
                    Log.e(TAG, e.message)
                    return false
                } finally {
                    connection.disconnect()
                }
            } catch (e: MalformedURLException) {
                Log.e(TAG, "Malformed URL!")
                return false
            } catch (e: IOException) {
                Log.e(TAG, "Error while connecting to " + url.toString())
                return false
            }

            start += n // skip n changes in next iteration
        }
        if (changes.isNotEmpty() && changes[0] != newChanges[0] || changes.size != newChanges.size) {
            changes = newChanges
            return true
        } else {
            return false
        }
    }

    private fun createRestUrl(): RestfulUrl {
        val rest = RestfulUrl("https://review.lineageos.org", "/changes/")
        rest.appendQuery("status:merged")
        if (branch.isNotBlank())
            rest.appendQuery("(branch:" + branch + " OR "
                    + "branch:" + branch + "-caf" + " OR "
                    + "branch:" + branch + "-caf-" + Device.BOARD + ")")
        return rest
    }

    companion object {
        /**
         * Logcat tag
         */
        private val TAG = "Changelog"
    }
}
