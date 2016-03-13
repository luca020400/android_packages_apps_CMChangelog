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

public class RestfulUri {

    private static final String URL = "http://review.cyanogenmod.org/changes/";

    /**
     * Number of changes to query
     */
    public int n;

    /**
     * Number of changes to skip
     */
    public int start;

    /**
     * The status of the requested changes.
     */
    private String status;

    /**
     * The branch of the requested changes
     */
    private String branch;

    private String options;

    /**
     * Construct a new formatted API URI with the specified options.
     *
     * @param status        request changes in the specified status
     * @param branch        request changes of the specified branch
     * @param changesToGet  the number of changes to get
     * @param changesToSkip the number of changes to skip
     */
    public RestfulUri(String status, String branch, int changesToGet, int changesToSkip) {
        this.status = "status:" + status;
        this.branch = branch;
        this.n = changesToGet;
        this.start = changesToSkip;
        this.options = "MESSAGES";
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(128);
        builder.append(URL).append("?");
        builder.append("q=").append(status).append("+").append(branch);
        if (options != null && !options.isEmpty())
            builder.append("&o=").append(options);
        if (n > 0)
            builder.append("&n=").append(n);
        if (start > 0)
            builder.append("&start=").append(start);
        return builder.toString();
    }
}
