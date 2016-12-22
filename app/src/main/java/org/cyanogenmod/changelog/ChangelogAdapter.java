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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class ChangelogAdapter extends RecyclerView.Adapter<ChangelogAdapter.ViewHolder> {

    /**
     * Logcat tag.
     */
    private static final String TAG = "ChangelogAdapter";

    /**
     * Data set to be represented by this Adapter.
     */
    private final List<Change> dataSet;

    /**
     * Format in which the dates will be displayed. , based on the device default locale.
     * For example the commit last update date.
     */
    private final DateFormat formatter;

    /**
     * Construct a new ChangelogAdapter representing the specified data set.
     *
     * @param dataSet the set of data we want this Adapter to represent.
     */
    public ChangelogAdapter(List<Change> dataSet) {
        this.dataSet = dataSet;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    }

    /**
     * Create new views (invoked by the layout manager)
     */
    @Override
    public ChangelogAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_view, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Change change = dataSet.get(holder.getAdapterPosition());
        holder.project.setText(String.format("%s", change.getProject().replace("CyanogenMod/", "").replace("android_", "")));
        holder.subject.setText(String.format("%s", change.getSubject()));
        if (change.getInsertions() != 0)
            holder.insertions.setText(String.format("+%s\t", change.getInsertions()));
        if (change.getDeletions() != 0)
            holder.deletions.setText(String.format("-%s\t", change.getDeletions()));
        // format the value of the date
        holder.date.setText(formatter.format(change.getSubmitted()));
        // set open in browser intent
        holder.container.setOnClickListener(new openBrowserOnClick(change.getChangeId()));
    }

    /**
     * Returns the size of the data set. Usually invoked by LayoutManager.
     *
     * @return the size of the data set.
     */
    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public List<Change> getDataset() {
        return dataSet;
    }

    /**
     * Clear all the elements of the RecyclerView
     */
    public void clear() {
        int c = getItemCount();
        dataSet.clear();
        notifyItemRangeRemoved(0, c);
    }

    /**
     * Append a set of elements to the RecyclerView
     *
     * @param changeCollection the List we want to append.
     */
    public void addAll(Collection<Change> changeCollection) {
        dataSet.addAll(changeCollection);
        notifyItemRangeChanged(0, getItemCount());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView subject;
        private final TextView project;
        private final TextView date;
        private final LinearLayout container;
        private final TextView insertions;
        private final TextView deletions;

        public ViewHolder(View itemView) {
            super(itemView);
            subject = (TextView) itemView.findViewById(R.id.subject);
            project = (TextView) itemView.findViewById(R.id.project);
            date = (TextView) itemView.findViewById(R.id.date);
            insertions = (TextView) itemView.findViewById(R.id.insertions);
            deletions = (TextView) itemView.findViewById(R.id.deletions);
            container = (LinearLayout) itemView.findViewById(R.id.list_item_container);
        }
    }

    private static class openBrowserOnClick implements View.OnClickListener {

        private final String reviewUrl;

        public openBrowserOnClick(String changeId) {
            this.reviewUrl = "https://review.cyanogenmod.org/#/c/" + changeId;
        }

        @Override
        public void onClick(View view) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(reviewUrl));
            try {
                view.getContext().startActivity(browserIntent);
                Log.i(TAG, String.format("Opening %s", reviewUrl));
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "Browser activity not found.");
            }
        }

    }
}
