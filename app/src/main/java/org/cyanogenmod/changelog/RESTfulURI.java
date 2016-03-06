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

public class RESTfulURI {
    public static final String STATUS_MERGED = "status:merged";
    public static final String STATUS_OPEN = "status:open";
    public static final String STATUS_ABANDONED = "status:abandoned";
    private final String mURL = "http://review.cyanogenmod.org/changes/";
    /**
     * Number of changes to query
     */
    public int n;
    /**
     * Number of changes to skip
     */
    public int start;
    private String mStatus;
    private String mBranch;

    public RESTfulURI(String mStatus, String mBranch, int changesToGet, int changesToSkip) {
        this.mStatus = mStatus;
        this.mBranch = mBranch;
        this.n = changesToGet;
        this.start = changesToSkip;
    }

    @Override
    public String toString() {
        return mURL + "?q=" + mStatus + "+" + mBranch + "&n=" + n + "&start=" + start;
    }
}
