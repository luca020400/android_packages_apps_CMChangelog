package com.luca020400.cmchangelog.Tasks;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.luca020400.cmchangelog.R;
import com.luca020400.cmchangelog.activities.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class ChangelogTask extends AsyncTask<String, String, String> {
    ArrayList<String> mProject = new ArrayList<>();
    ArrayList<String> mLastUpdates = new ArrayList<>();
    ArrayList<String> mId = new ArrayList<>();
    ArrayList<String> mSubject = new ArrayList<>();

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        MainActivity.getInstance().swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    protected String doInBackground(String... urls) {
        try {
            String out = new Scanner(new URL(urls[0]).openStream(), "UTF-8").useDelimiter("\\A").next();
            JSONArray newJArray = new JSONArray(out);

            for (int i = 0; i < newJArray.length(); ++i) {
                JSONObject jsonObject = newJArray.getJSONObject(i);

                String msg_project = (String) jsonObject.get("project");
                String msg_last_updated = (String) jsonObject.get("last_updated");
                Integer msg_id = (Integer) jsonObject.get("id");
                String msg_subject = (String) jsonObject.get("subject");

                mProject.add(msg_project);
                mLastUpdates.add(msg_last_updated);
                mId.add(msg_id.toString());
                mSubject.add(msg_subject);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String urls) {
        super.onPostExecute(urls);

        final MainActivity mainActivity = MainActivity.getInstance();
        GridView gridview = (GridView) mainActivity.findViewById(R.id.gridview);
        ArrayAdapter adapter = new ArrayAdapter(mainActivity,
                R.layout.gridview, R.id.commit, mSubject) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView commit = (TextView) view.findViewById(R.id.commit);
                TextView repo = (TextView) view.findViewById(R.id.repo);
                TextView date = (TextView) view.findViewById(R.id.date);

                String CommitDate = null;

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                    Date convertedCommitDate = sdf.parse(mLastUpdates.get(position));
                    CommitDate = sdf.format(convertedCommitDate );
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }

                commit.setText(mSubject.get(position));
                if (mProject.get(position).equals("android")) {
                    repo.setText(mProject.get(position) + "_manifest");
                } else {
                    repo.setText(mProject.get(position).replace("android_", ""));
                }
                date.setText(CommitDate);
                return view;
            }
        };
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                String review_url = String.format
                        ("http://review.cyanogenmod.org/#/c/%s", mId.get(position));
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(review_url));
                mainActivity.startActivity(browserIntent);
            }
        });

        mainActivity.swipeRefreshLayout.setRefreshing(false);
    }
}