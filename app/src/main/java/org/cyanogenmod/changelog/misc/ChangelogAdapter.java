package org.cyanogenmod.changelog.misc;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.cyanogenmod.changelog.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChangelogAdapter extends ArrayAdapter<Change> {
    private static String TAG = "ChangelogAdapter";

    public ChangelogAdapter(Context context, ArrayList<Change> changes) {
        super(context, 0, changes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Change change = getItem(position);
        String commitDate = null;

        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.gridview, parent, false);
        }

        TextView subject = (TextView) convertView.findViewById(R.id.subject);
        TextView project = (TextView) convertView.findViewById(R.id.project);
        TextView last_updated = (TextView) convertView.findViewById(R.id.last_updated);

        try {
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
            Date convertedCommitDate = simpleDateFormat.parse(change.mLastUpdated);
            commitDate = simpleDateFormat.format(convertedCommitDate);
        } catch (ParseException e) {
            Log.e(TAG, "", e);
        }

        subject.setText(change.mSubject);
        last_updated.setText(commitDate);

        if (change.mProject.equals("android")) {
            project.setText(String.format("%s_manifest", change.mProject));
        } else {
            project.setText(change.mProject.replace("android_", ""));
        }

        return convertView;
    }
}

