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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChangelogActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {

    /**
     * Logcat tag.
     */
    private static final String TAG = "ChangelogActivity";

    /**
     * Content view.
     */
    private SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * Adapter for the RecyclerView.
     */
    private ChangelogAdapter mAdapter;

    /**
     * Dialog showing info about the device.
     */
    private Dialog mInfoDialog;

    /**
     * Changelog to show
     */
    private Changelog changelog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        changelog = new Changelog();
        changelog.setBranch(Device.LINEAGE_BRANCH);
        /* Setup and create Views */
        init();
        /* Populate RecyclerView with cached data */
        bindCache();
        /* Fetch data */
        updateChangelog();
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

    @Override
    public void onRefresh() {
        updateChangelog();
    }

    /**
     * Utility method.
     */
    private void init() {
        // Setup SwipeRefreshLayout
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh);
        // Setup refresh listener which triggers new data loading
        mSwipeRefreshLayout.setOnRefreshListener(this);
        // Color scheme of the refresh spinner
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.color_primary_dark, R.color.color_accent);
        // Setup RecyclerView
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        // Setup divider for RecyclerView items
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                mRecyclerView.getContext(), linearLayoutManager.getOrientation()));
        // Setup item animator
        mRecyclerView.setItemAnimator(null);    // Disable to prevent view blinking when refreshing
        // Setup and initialize RecyclerView adapter
        mAdapter = new ChangelogAdapter(new CopyOnWriteArrayList<>());
        mRecyclerView.setAdapter(mAdapter);
        // Setup and initialize info dialog
        String message = String.format(Locale.getDefault(), "%s %s\n\n%s %s\n\n%s %s\n\n%s %s",
                getString(R.string.dialog_device_name), Device.DEVICE,
                getString(R.string.dialog_version), Device.LINEAGE_VERSION,
                getString(R.string.dialog_build_date), Device.BUILD_DATE,
                getString(R.string.dialog_update_channel), Device.LINEAGE_RELEASE_CHANNEL);
        View infoDialog = getLayoutInflater().inflate(R.layout.info_dialog, mRecyclerView, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_InfoDialog)
                .setView(infoDialog)
                .setPositiveButton(R.string.dialog_ok, null);
        TextView dialogMessage = infoDialog.findViewById(R.id.info_dialog_message);
        dialogMessage.setText(message);
        mInfoDialog = builder.create();
    }

    /**
     * Update Changelog
     */
    private void updateChangelog() {
        Log.i(TAG, "Updating Changelog");
        if (!Device.isConnected(this)) {
            Log.w(TAG, "Missing network connection");
            Toast.makeText(this, R.string.data_connection_required, Toast.LENGTH_SHORT).show();
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }
        new ChangelogTask().execute();
    }

    /**
     * Read cached data and bind it to the RecyclerView.
     */
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
            changelog.setChanges(cachedData);
            mAdapter.clear();
            mAdapter.addAll(changelog.getChanges());
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

    private class ChangelogTask extends AsyncTask<Void, Void, Boolean> {

        @UiThread
        @Override
        protected void onPreExecute() {
            /* Start refreshing circle animation.
             * Wrap in runnable to workaround SwipeRefreshLayout bug.
             * View: https://code.google.com/p/android/issues/detail?id=77712
             */
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));
        }

        @WorkerThread
        @Override
        protected Boolean doInBackground(Void... voids) {
            return changelog.update(100);
        }

        @UiThread
        @Override
        protected void onPostExecute(Boolean isUpdated) {
            if (isUpdated) {
                mAdapter.clear();
                mAdapter.addAll(changelog.getChanges());
                // Update cache
                new CacheChangelogTask(getCacheDir()).execute((List) changelog.getChanges());
            } else {
                Log.d(TAG, "Nothing changed");
            }
            // Stop refreshing circle animation.
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
        }
    }

}
