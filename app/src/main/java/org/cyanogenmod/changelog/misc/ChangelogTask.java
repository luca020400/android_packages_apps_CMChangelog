package org.cyanogenmod.changelog.misc;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.cyanogenmod.changelog.ChangelogActivity;
import org.cyanogenmod.changelog.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class ChangelogTask extends AsyncTask<String, String, String> {
    private static String TAG = "ChangelogTask";
    private ChangelogActivity mChangelogActivity;
    private ChangelogAdapter mAdapter;
    private ArrayList<String> mId = new ArrayList<>();

    public ChangelogTask(ChangelogActivity changelogActivity) {
        mChangelogActivity = changelogActivity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (mAdapter != null) {
            mAdapter.clear();
        }

        mChangelogActivity.swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    protected String doInBackground(String... urls) {
        ArrayList<Change> logs = new ArrayList<>();

        mAdapter = new ChangelogAdapter(mChangelogActivity, logs);

        try {
            String scanner =
                    new Scanner(new URL(urls[0]).openStream(), "UTF-8").useDelimiter("\\A").next();
            JSONArray jsonArray = new JSONArray(scanner);

            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String subject = (String) jsonObject.get("subject");
                String project = (String) jsonObject.get("project");
                String lastUpdated = (String) jsonObject.get("last_updated");
                String id = jsonObject.get("id").toString();

                Change newChange = new Change(subject, project, lastUpdated, id);
                mAdapter.add(newChange);

                mId.add(id);
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "", e);
        }

        Log.i(TAG, "Successfully parsed CMXLog API");

        return null;
    }

    @Override
    protected void onPostExecute(String urls) {
        super.onPostExecute(urls);

        GridView gridview = (GridView) mChangelogActivity.findViewById(R.id.gridview);
        gridview.setAdapter(mAdapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                String review_url =
                        String.format("http://review.cyanogenmod.org/#/c/%s", mId.get(position));
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(review_url));
                Log.i(TAG, String.format("Opening %s", review_url));
                mChangelogActivity.startActivity(browserIntent);
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mChangelogActivity.swipeRefreshLayout.setRefreshing(false);
            }
        }, 500);
    }
}
