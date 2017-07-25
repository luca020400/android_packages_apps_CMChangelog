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

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.list_view.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Construct a new ChangelogAdapter representing the specified data set.
 */
internal class ChangelogAdapter : RecyclerView.Adapter<ChangelogAdapter.ViewHolder>() {
    val dataSet = arrayListOf<Change>()

    /**
     * Format in which the dates will be displayed. , based on the device default locale.
     * For example the commit last update date.
     */
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    /**
     * Create new views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChangelogAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_view, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val change = dataSet[holder.adapterPosition]
        holder.project.text = String.format("%s", change.project.replace("LineageOS/", "").replace("android_", ""))
        holder.subject.text = String.format("%s", change.subject)
        if (change.insertions != 0)
            holder.insertions.text = String.format("+%s\t", change.insertions)
        if (change.deletions != 0)
            holder.deletions.text = String.format("-%s\t", change.deletions)
        // format the value of the date
        holder.date.text = formatter.format(change.submitted)
        // set open in browser intent
        holder.container.setOnClickListener(openBrowserOnClick(change.id))
    }

    /**
     * Returns the size of the data set. Usually invoked by LayoutManager.

     * @return the size of the data set.
     */
    override fun getItemCount() = dataSet.size

    /**
     * Clear all the elements of the RecyclerView
     */
    fun clear() {
        dataSet.clear()
        notifyDataSetChanged()
    }

    /**
     * Append a set of elements to the RecyclerView

     * @param changeCollection the List we want to append.
     */
    fun addAll(changeCollection: Collection<Change>) {
        dataSet.addAll(changeCollection)
        notifyDataSetChanged()
    }

    internal class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subject: TextView = itemView.subject
        val project: TextView = itemView.project
        val date: TextView = itemView.date
        val container: LinearLayout = itemView.list_item_container
        val insertions: TextView = itemView.insertions
        val deletions: TextView = itemView.deletions
    }

    private class openBrowserOnClick internal constructor(changeId: String) : View.OnClickListener {

        private val reviewUrl: String = "https://review.lineageos.org/#/c/" + changeId

        override fun onClick(view: View) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(reviewUrl))
            try {
                view.context.startActivity(browserIntent)
                Log.i(TAG, String.format("Opening %s", reviewUrl))
            } catch (e: ActivityNotFoundException) {
                Log.e(TAG, "Browser activity not found.")
            }

        }
    }

    companion object {
        /**
         * Logcat tag.
         */
        private val TAG = "ChangelogAdapter"
    }
}
