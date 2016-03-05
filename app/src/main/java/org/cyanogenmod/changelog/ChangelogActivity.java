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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.stream.JsonReader;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChangelogActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "ChangelogActivity";
    /**
     * View Container
     */
    private SwipeRefreshLayout mSwipeRefreshLayout;
    /**
     * RecyclerView used to list all the changes
     */
    private RecyclerView mRecyclerView;
    /**
     * Adapter for the RecyclerView
     */
    private ChangelogAdapter mAdapter;
    /**
     *
     */
    private Dialog mInfoDialog;
    /**
     * String representing the full CyanogenMod build version
     */
    private String mCMVersion;
    /**
     * String representing the CyanogenMod version of the device (e.g 13)
     */
    private String mCyanogenMod;
    /**
     * String representing the update channel aka release type (e.g nightly)
     */
    private String mCMReleaseType;
    /**
     * String representing device code-name (e.g. hammerhead)
     */
    private String mDevice;

    /*
     * Device Info
     */
    private String mManufacturer;
    private String mHardware;
    private String mBoard;

    /*
     * Special Repos
     */
    String[] mCommonReposCommon = {
            "android_hardware_cyanogen",
            "android_hardware_ril",
            "android_hardware_sony_thermanager",
            "android_hardware_sony_timekeep"
    };
    String[] mCommonReposQcom = {
            "android_device_qcom_common",
            "android_device_qcom_sepolicy"
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        /* Get device info */
        retrieveDeviceInfo();
        /* Setup and create Views */
        init();
        /* Populate RecyclerView with cached data */
        bindCache();
        /* Fetch data */
        updateChangelog();
    }

    /**
     * Utility method.
     */
    private void init() {
        // Setup SwipeRefreshLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        // Setup refresh listener which triggers new data loading
        mSwipeRefreshLayout.setOnRefreshListener(this);
        // Color scheme of the refresh spinner
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.color_primary_dark, R.color.color_accent);
        // Setup RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Setup divider for RecyclerView items
        mRecyclerView.addItemDecoration(new Divider(this));
        // Setup item animator
        mRecyclerView.setItemAnimator(null);    // Disable to prevent view blinking when refreshing
        // Setup and initialize RecyclerView adapter
        mAdapter = new ChangelogAdapter(new CopyOnWriteArrayList<Change>());
        mRecyclerView.setAdapter(mAdapter);
        // Setup and initialize info dialog
        String message = String.format("%s %s\n%s %s\n%s %s",
                getString(R.string.device_info_device), mDevice,
                getString(R.string.device_info_version), mCMVersion,
                getString(R.string.device_info_update_channel), mCMReleaseType);
        View infoDialog = getLayoutInflater().inflate(R.layout.info_dialog, mRecyclerView, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_InfoDialog)
                .setView(infoDialog)
                .setPositiveButton(R.string.dialog_ok, null);
        TextView dialogMessage = (TextView) infoDialog.findViewById(R.id.info_dialog_message);
        dialogMessage.setText(message);
        mInfoDialog = builder.create();
    }

    private void bindCache() {
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(getCacheDir(), "cache"));
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            List<Change> cachedData = new LinkedList<>();
            Change temp;
            while ((temp = (Change) objectInputStream.readObject()) != null) {
                cachedData.add(temp);
            }
            objectInputStream.close();
            mAdapter.clear();
            mAdapter.addAll(cachedData);
            Log.d(TAG, "Restored cache");
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Cache not found.");
        } catch (EOFException e) {
            Log.e(TAG, "Error while reading cache! (EOF) ");
        } catch (StreamCorruptedException e) {
            Log.e(TAG, "Corrupted cache!");
        } catch (IOException e) {
            Log.e(TAG, "Error while reading cache!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_device_info:
                mInfoDialog.show();
                break;
            case R.id.menu_refresh:
                if (!mSwipeRefreshLayout.isRefreshing()) updateChangelog();
                break;
        }
        return true;
    }

    /**
     * Retrieve info about the device.
     */
    private void retrieveDeviceInfo() {
        mCMVersion = Cmd.exec("getprop ro.cm.version");
        String[] version = mCMVersion.split("-");
        mCyanogenMod = version[0];
        mCMReleaseType = version[2];
        mDevice = version[3];
        mHardware = Build.HARDWARE.toLowerCase();
        mManufacturer = Build.MANUFACTURER.toLowerCase();
        mBoard = Build.BOARD.toLowerCase();
    }

    @Override
    public void onRefresh() {
        updateChangelog();
    }

    /**
     * Fetch data asynchronously
     */
    private void updateChangelog() {
        Log.i(TAG, "Updating Changelog");

        if (!deviceIsConnected()) {
            Log.e(TAG, "Missing network connection");
            Toast.makeText(this, R.string.data_connection_required, Toast.LENGTH_SHORT).show();
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        new ChangelogTask().execute(80);
    }

    /**
     * Check if the device is connected to internet, return true if the device has data connection.
     *
     * @return true if device is connected to internet, otherwise returns false.
     */
    private boolean deviceIsConnected() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return !(networkInfo == null || !networkInfo.isConnected());
    }

    @Override
    protected void onStop() {
        super.onStop();
        new CacheTask().execute(mAdapter.getDataset());
    }

    private class ChangelogTask extends AsyncTask<Integer, Change, List<Change>> {
        // Runs on UI thread
        @Override
        protected void onPreExecute() {
            /* Start refreshing circle animation.
             * Wrap in runnable to workaround SwipeRefreshLayout bug.
             * View: https://code.google.com/p/android/issues/detail?id=77712 */
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
        }

        // Runs on the separate thread
        @Override
        protected List<Change> doInBackground(Integer... q) {
            List<Change> changes = new LinkedList<>();
            int parsed = 0; // number of changes parsed and selected so far
            int n = 120, start = 0; // number of changes to fetch and to skip
            while (parsed < q[0]) {
                long time = System.currentTimeMillis();
                // Form API URL
                String apiUrl = String.format("http://review.cyanogenmod.org/changes/?q=status:merged+%s&%s&%s",
                        "branch:cm-" + mCyanogenMod,
                        "n=" + n,
                        "start=" + start);
                try {
                    HttpURLConnection con = (HttpURLConnection) new URL(apiUrl).openConnection();
                    // Optional default is GET
                    con.setRequestMethod("GET");
                    // Log
                    Log.d(TAG, "Sending 'GET' request to URL : " + apiUrl);
                    Log.v(TAG, String.format("Response code: %s\tResponse message: %s", con.getResponseCode(), con.getResponseMessage()));
                    /* Parse JSON */
                    JsonReader reader = new JsonReader(new InputStreamReader(con.getInputStream()));
                    reader.setLenient(true); // strip XSSI protection
                    reader.beginArray();
                    while (reader.hasNext()) {
                        reader.beginObject();
                        Change newChange = new Change();
                        while (reader.hasNext()) {
                            switch (reader.nextName()) {
                                case "_number":
                                    newChange.setChangeId(reader.nextString());
                                    break;
                                case "project":
                                    newChange.setProject(reader.nextString());
                                    break;
                                case "subject":
                                    newChange.setSubject(reader.nextString());
                                    break;
                                case "updated":
                                    newChange.setLastUpdate(reader.nextString());
                                    break;
                                default:
                                    reader.skipValue();
                            }
                        }
                        reader.endObject();
                        // check if its a legit change
                        if (isDeviceSpecific(newChange)) {
                            changes.add(newChange);
                            parsed++;
                        }
                    }
                    reader.endArray();
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Parse error!", e);
                }
                Log.i(TAG, "Successfully parsed REST API in " +
                        (System.currentTimeMillis() - time) + "ms");
                start += n; // skip n changes in next iteration
            }
            return changes;
        }

        private boolean isDeviceSpecific(Change change) {
            for (String repo : mCommonReposCommon) {
                if (change.getProject().contains(repo)) {
                    return true;
                }
            }

            for (String repo : mCommonReposQcom) {
                if (mHardware.equals("qcom") && change.getProject().contains(repo)) {
                    return true;
                }
            }

            if (change.getProject().contains("device") ||
                    change.getProject().contains("kernel")) {
                return change.getProject().contains(mDevice) ||
                        change.getProject().contains(mManufacturer + "-common") ||
                        change.getProject().contains(mManufacturer) &&
                                change.getProject().contains(mBoard + "-common") ||
                        change.getProject().contains(mManufacturer) &&
                                change.getProject().contains(mHardware + "-common");
            } else if (change.getProject().contains("hardware")) {
                return change.getProject().contains(mHardware);
            }
            return true;
        }

        // Runs on the UI thread
        @Override
        protected void onPostExecute(List<Change> fetchedChanges) {
            // update the list
            mAdapter.clear();
            mAdapter.addAll(fetchedChanges);
            // delay refreshing animation just for the show
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }, 400);
        }
    }

    private class CacheTask extends AsyncTask<List, Void, Void> {
        @Override
        protected Void doInBackground(List... list) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(new File(getCacheDir(), "cache"));
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

}

