package com.luca020400.cmchangelog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class MainActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {
    GridView gridview;
    SwipeRefreshLayout swipeRefreshLayout;

    String mCMVersion;
    String mCyanogenMod;
    String mCMReleaseType;
    String mDevice;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mCMVersion = Cmd.exec("getprop ro.cm.version");
        String[] version = mCMVersion.split("-");
        mCyanogenMod = version[0];
        mCMReleaseType = version[2];
        mDevice = version[3];

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_primary);

        UpdateChangelog();
    }

    public void onRefresh() {
        UpdateChangelog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, R.string.update_changelog)
                .setIcon(R.drawable.ic_menu_refresh)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS
                        | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(0, 1, 0, R.string.device_info)
                .setIcon(R.drawable.ic_info)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS
                        | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                UpdateChangelog();
                return true;
            case 1:
                DeviceInfo();
                return true;
        }
        return false;
    }

    public void DeviceInfo() {
        String message = getString(R.string.devive_info_device) + " " + mDevice + "\n"
            + getString(R.string.devive_info_running) + " " + mCMVersion + "\n"
            + getString(R.string.devive_info_update_channel) + " " + mCMReleaseType;

        new AlertDialog.Builder(this)
            .setTitle(R.string.device_info)
            .setMessage(message)
            .setPositiveButton(R.string.dialog_ok, null)
            .show();

    }

    public void UpdateChangelog() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService
                (Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isConnected()) {
            Toast.makeText(this, R.string.data_connection_required, Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        new ChangelogTask().execute(String.format
                ("http://api.cmxlog.com/changes/%s/%s", mCyanogenMod, mDevice));
    }

    public class ChangelogTask extends AsyncTask<String, String, String> {
        ArrayList<String> mProject = new ArrayList<>();
        ArrayList<String> mLastUpdates = new ArrayList<>();
        ArrayList<String> mId = new ArrayList<>();
        ArrayList<String> mSubject = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
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

            gridview = (GridView) findViewById(R.id.gridview);
            ArrayAdapter adapter = new ArrayAdapter(MainActivity.this,
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
                    startActivity(browserIntent);
                }
            });
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}