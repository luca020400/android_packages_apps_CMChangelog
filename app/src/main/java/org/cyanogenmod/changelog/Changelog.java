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

import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class Changelog {

    /**
     * Logcat tag
     */
    private static final String TAG = "Changelog";

    /**
     * List of Changes of this changelog
     */
    private List<Change> changes;

    /**
     * The branch of this Changelog
     */
    private String branch;

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public List<Change> getChanges() {
        return changes;
    }

    public void setChanges(List<Change> changes) {
        this.changes = changes;
    }

    /**
     * Update this Changelog by retrieving and processing data from API.
     *
     * @param numberOfChanges the minimum number of changes to fetch.
     * @return true if the Changelog was successfully updated, false if not.
     */
    @WorkerThread
    public boolean update(int numberOfChanges) {
        List<Change> newChanges = new LinkedList<>();
        ChangelogParser parser = new ChangelogParser();
        int n = 500, start = 0; // number of changes to fetch and to skip
        RestfulUri uri = new RestfulUri("merged", branch, n, start);
        long time = System.currentTimeMillis();
        while (newChanges.size() < numberOfChanges) {
            uri.start = start;
            try {
                URL url = new URL(uri.toString());
                Log.d(TAG, "Sending GET request to \"" + url.toString() + "\"");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    connection.setRequestMethod("GET");
                    try (InputStream in = new BufferedInputStream(connection.getInputStream())) {
                        newChanges.addAll(parser.readJsonStream(in));
                    } catch (IOException e) {
                        Log.e(TAG, "Error while parsing received input stream!\n" + e.getMessage());
                        return false;
                    }
                } catch (ProtocolException e) {
                    Log.e(TAG, e.getMessage());
                    return false;
                } finally {
                    connection.disconnect();
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, "Malformed URL!");
                return false;
            } catch (IOException e) {
                Log.e(TAG, "Error while connecting to " + uri.toString());
                return false;
            }
            start += n; // skip n changes in next iteration
        }
        Log.v(TAG, "Successfully parsed " + newChanges.size() + " changes in " + (System.currentTimeMillis() - time) + "ms");
        if (changes == null || !changes.get(0).equals(newChanges.get(0)) || changes.size() != newChanges.size()) {
            changes = newChanges;
            return true;
        } else {
            return false;
        }
    }
}
