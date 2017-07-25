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

import java.io.Serializable;
import java.util.Date;

/**
 * Model class to use as the data source for ChangelogAdapter
 *
 * @see ChangelogAdapter
 */
class Change implements Serializable {

    /**
     * The subject of the change (header line of the commit message).
     */
    private final String subject;

    /**
     * The name of the project.
     */
    private final String project;

    /**
     * The timestamp of when the change was submitted.
     */
    private final Date submitted;

    /**
     * The legacy numeric ID of the change.
     */
    private final String id;

    /**
     * Number of inserted lines.
     */
    private final int insertions;

    /**
     * Number of deleted lines.
     */
    private final int deletions;

    /**
     * Constructs a new Change with the specified properties.
     *
     * @param subject   the subject of the Change
     * @param project   the project package affected by the Change
     * @param submitted last update date of the Change
     * @param id        id of the Change
     */
    public Change(String subject, String project, Date submitted, String id, int insertions, int deletions) {
        this.subject = subject;
        this.project = project;
        this.submitted = submitted;
        this.id = id;
        this.insertions = insertions;
        this.deletions = deletions;
    }

    String getSubject() {
        return subject;
    }

    String getProject() {
        return project;
    }

    Date getSubmitted() {
        return submitted;
    }

    String getId() {
        return id;
    }

    int getInsertions() {
        return insertions;
    }

    int getDeletions() {
        return deletions;
    }

    /**
     * Check if this Change is device specific Change.
     *
     * @return true if the Device is affected by this Change, else returns false
     */
    boolean isDeviceSpecific() {
        // Fallback to 'old' method
        if (Device.PROJECTS.isEmpty()) {
            return isDeviceSpecificFallback();
        } else for (String deviceProject : Device.PROJECTS) {
            if (this.project.equals(deviceProject)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if this Change is a device specific Change.
     *
     * @return true if this Change is a device specific change, otherwise false
     */
    private boolean isDeviceSpecificFallback() {
        for (String repo : Device.COMMON_REPOS) {
            if (project.contains(repo))
                return true;
        }

        for (String repo : Device.COMMON_REPOS_QCOM) {
            if (Device.HARDWARE.equals("qcom") && project.contains(repo))
                return true;
        }

        if (project.contains("device")) {
            return project.contains(Device.DEVICE) ||
                    project.contains(Device.MANUFACTURER + "-common") ||
                    project.contains(Device.MANUFACTURER) && project.contains(Device.BOARD + "-common") ||
                    project.contains(Device.MANUFACTURER) && project.contains(Device.HARDWARE + "-common");
        } else if (project.contains("kernel")) {
            return project.contains(Device.DEVICE) ||
                    project.contains(Device.MANUFACTURER) && project.contains(Device.DEVICE) ||
                    project.contains(Device.MANUFACTURER) && project.contains(Device.BOARD) ||
                    project.contains(Device.MANUFACTURER) && project.contains(Device.HARDWARE);
        } else if (project.contains("hardware")) {
            return project.contains(Device.HARDWARE);
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Change change = (Change) o;

        return !(subject != null ? !subject.equals(change.subject) : change.subject != null) ||
                !(project != null ? !project.equals(change.project) : change.project != null) ||
                !(submitted != null ? !submitted.equals(change.submitted) : change.submitted != null) ||
                !(id != null ? !id.equals(change.id) : change.id != null);
    }
}