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

import android.os.AsyncTask;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * An AsyncTask that caches a Changelog.
 */
class CacheChangelogTask extends AsyncTask<List, Void, Void> {

    /**
     * Logcat tag.
     */
    private static final String TAG = "CacheChangelogTask";

    /**
     * File representing the directory in which the data will be stored.
     */
    private final File cacheDir;

    /**
     * @param cacheDir the directory in which the data will be stored.
     */
    CacheChangelogTask(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    /**
     * Caches the specified set of Changes. Will execute in a separate Thread.
     *
     * @param list the list of Changes to be cached.
     * @return null
     */
    @WorkerThread
    @Override
    protected Void doInBackground(List... list) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(cacheDir, "cache"));
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            for (Object obj : list[0]) {
                objectOutputStream.writeObject(obj);
            }
            objectOutputStream.writeObject(null);
            objectOutputStream.close();
            Log.d(TAG, "Successfully cached data");
        } catch (IOException e) {
            Log.e(TAG, "Error while writing cache");
        }
        return null;
    }

}