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

import java.io.Serializable
import java.util.*

/**
 * Model class to use as the data source for ChangelogAdapter
 * @param subject   the subject of the Change
 * *
 * @param project   the project package affected by the Change
 * *
 * @param submitted last update date of the Change
 * *
 * @param id        id of the Change
 * @see ChangelogAdapter
 */
internal data class Change(
        /**
         * The subject of the change (header line of the commit message).
         */
        val subject: String,
        /**
         * The name of the project.
         */
        val project: String,
        /**
         * The timestamp of when the change was submitted.
         */
        val submitted: Date,
        /**
         * The legacy numeric ID of the change.
         */
        val id: String,
        /**
         * Number of inserted lines.
         */
        val insertions: Int,
        /**
         * Number of deleted lines.
         */
        val deletions: Int) : Serializable {

    /**
     * Check if this Change is a device specific Change.

     * @return true if this Change is a device specific change, otherwise false
     */
    val isDeviceSpecific: Boolean
        get() {
            Device.COMMON_REPOS
                    .filter { project.contains(it) }
                    .forEach { return true }

            if (Device.HARDWARE == "qcom") {
                Device.COMMON_REPOS_QCOM
                        .filter { project.contains(it) }
                        .forEach { return true }
            }

            if (project.contains("device")) {
                return project.contains(Device.DEVICE) ||
                        project.contains(Device.MANUFACTURER + "-common") ||
                        project.contains(Device.MANUFACTURER) && project.contains(Device.BOARD + "-common") ||
                        project.contains(Device.MANUFACTURER) && project.contains(Device.HARDWARE + "-common")
            } else if (project.contains("kernel")) {
                return project.contains(Device.DEVICE) ||
                        project.contains(Device.MANUFACTURER) && project.contains(Device.DEVICE) ||
                        project.contains(Device.MANUFACTURER) && project.contains(Device.BOARD) ||
                        project.contains(Device.MANUFACTURER) && project.contains(Device.HARDWARE)
            } else if (project.contains("hardware")) {
                return project.contains(Device.HARDWARE)
            }
            return true
        }
}
