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

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.support.annotation.UiThread
import android.support.annotation.WorkerThread
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.main.*
import java.io.*
import java.util.*

class ChangelogActivity : Activity(), SwipeRefreshLayout.OnRefreshListener {
    private val TAG = "ChangelogActivity"

    /**
     * Adapter for the RecyclerView.
     */
    private val mChangelogAdapter by lazy {
        ChangelogAdapter()
    }

    /**
     * Dialog showing info about the device.
     */
    private var mInfoDialog: Dialog? = null

    /**
     * Changelog to show
     */
    private var changelog = Changelog(Device.LINEAGE_BRANCH)

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        /* Setup and create Views */
        init()
        /* Populate RecyclerView with cached data */
        bindCache()
        /* Fetch data */
        updateChangelog()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.actions, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_device_info -> mInfoDialog!!.show()
            R.id.menu_refresh -> if (!swipe_refresh.isRefreshing) updateChangelog()
        }
        return true
    }

    override fun onRefresh() {
        updateChangelog()
    }

    /**
     * Utility method.
     */
    private fun init() {
        // Setup refresh listener which triggers new data loading
        swipe_refresh.setOnRefreshListener(this)
        // Color scheme of the refresh spinner
        swipe_refresh.setColorSchemeResources(
                R.color.color_primary_dark, R.color.color_accent)
        // Setup RecyclerView
        recycler_view.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        recycler_view.layoutManager = linearLayoutManager
        // Setup divider for RecyclerView items
        recycler_view.addItemDecoration(DividerItemDecoration(
                recycler_view.context, linearLayoutManager.orientation))
        // Setup item animator
        recycler_view.itemAnimator = null    // Disable to prevent view blinking when refreshing
        // Setup and initialize RecyclerView adapter
        recycler_view.adapter = mChangelogAdapter
        // Setup and initialize info dialog
        val message = String.format(Locale.getDefault(), "%s %s\n\n%s %s\n\n%s %s\n\n%s %s",
                getString(R.string.dialog_device_name), Device.DEVICE,
                getString(R.string.dialog_version), Device.LINEAGE_VERSION,
                getString(R.string.dialog_build_date), Device.BUILD_DATE,
                getString(R.string.dialog_update_channel), Device.LINEAGE_RELEASE_CHANNEL)
        val infoDialog = layoutInflater.inflate(R.layout.info_dialog, recycler_view, false)
        val builder = AlertDialog.Builder(this, R.style.Theme_InfoDialog)
                .setView(infoDialog)
                .setPositiveButton(R.string.dialog_ok, null)
        val dialogMessage = infoDialog.findViewById<TextView>(R.id.info_dialog_message)
        dialogMessage.text = message
        mInfoDialog = builder.create()
    }

    /**
     * Update Changelog
     */
    private fun updateChangelog() {
        Log.i(TAG, "Updating Changelog")
        if (!Device.isConnected(this)) {
            Log.w(TAG, "Missing network connection")
            Toast.makeText(this, R.string.data_connection_required, Toast.LENGTH_SHORT).show()
            swipe_refresh.isRefreshing = false
            return
        }
        ChangelogTask().execute()
    }

    /**
     * Read cached data and bind it to the RecyclerView.
     */
    private fun bindCache() {
        try {
            val fileInputStream = FileInputStream(File(cacheDir, "cache"))
            val objectInputStream = ObjectInputStream(fileInputStream)
            val cachedData = arrayListOf<Change>()
            while (true) {
                val temp = objectInputStream.readObject() as Change? ?: break
                cachedData.add(temp)
            }
            objectInputStream.close()
            changelog.changes = cachedData
            mChangelogAdapter.clear()
            mChangelogAdapter.addAll(changelog.changes)
            Log.d(TAG, "Restored cache")
        } catch (e: FileNotFoundException) {
            Log.w(TAG, "Cache not found.")
        } catch (e: EOFException) {
            Log.e(TAG, "Error while reading cache! (EOF) ")
        } catch (e: StreamCorruptedException) {
            Log.e(TAG, "Corrupted cache!")
        } catch (e: IOException) {
            Log.e(TAG, "Error while reading cache!")
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }

    }

    private inner class ChangelogTask : AsyncTask<Void, Void, Boolean>() {

        @UiThread
        override fun onPreExecute() {
            /* Start refreshing circle animation.
             * Wrap in runnable to workaround SwipeRefreshLayout bug.
             * View: https://code.google.com/p/android/issues/detail?id=77712
             */
            swipe_refresh.post { swipe_refresh.isRefreshing = true }
        }

        @WorkerThread
        override fun doInBackground(vararg voids: Void): Boolean {
            return changelog.update(100)
        }

        @UiThread
        override fun onPostExecute(isUpdated: Boolean) {
            if (isUpdated) {
                mChangelogAdapter.clear()
                mChangelogAdapter.addAll(changelog.changes)
                // Update cache
                CacheChangelogTask(cacheDir).execute(changelog.changes)
            } else {
                Log.d(TAG, "Nothing changed")
            }
            // Stop refreshing circle animation.
            swipe_refresh.post { swipe_refresh.isRefreshing = false }
        }
    }
}
