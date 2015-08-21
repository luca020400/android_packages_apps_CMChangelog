package com.luca020400.cmchangelog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends Activity {
    private ProgressDialog mProgressDialog;
    GridView gridview;

    public String mCMVersion;
    public String mCyanogenMod;
    public String mCMReleaseType;
    public String mDevice;
    public String[] simpleproject;
    public String[] simplelastupdated;
    public String[] simpleid;
    public String[] simplesubject;
    ArrayList<String> mProject = new ArrayList<>();
    ArrayList<String> mLastUpdates = new ArrayList<>();
    ArrayList<String> mId = new ArrayList<>();
    ArrayList<String> mSubject = new ArrayList<>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        String version_full = cmd.exec("getprop ro.cm.version");
        String[] version = version_full.split("-");
        mCMVersion = version_full;
        mCyanogenMod = version[0];
        mCMReleaseType = version[2];
        mDevice = version[3];

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
         String message = getString(R.string.devive_info_device) + " " + mDevice + "\n\n"
                + getString(R.string.devive_info_running) + " " + mCMVersion + "\n\n"
                + getString(R.string.devive_info_update_channel) + " " + mCMReleaseType;

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.device_info)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_ok, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void UpdateChangelog() {
        if (!isOnline()) {
            Toast.makeText(this, R.string.data_connection_required, Toast.LENGTH_SHORT).show();
            return;
        }
        new ChangelogTask().execute(String.format
                ("http://api.cmxlog.com/changes/%s/%s", mCyanogenMod, mDevice));
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService
                (Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public class ChangelogTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setTitle(R.string.checking_for_updates);
            mProgressDialog.setMessage(getString(R.string.checking_for_updates));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            JSONParser parser = new JSONParser();
            try {
                URL url = new URL(urls[0]);
                URLConnection con = url.openConnection();
                File temp = File.createTempFile("cmxlog_json", ".tmp");
                OutputStream out = new FileOutputStream(temp);
                InputStream inputStream = con.getInputStream();
                byte buf[] = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0)
                    out.write(buf, 0, len);
                out.close();
                inputStream.close();

                JSONArray jsonarray = (JSONArray)parser.parse
                        (new FileReader(temp.getAbsolutePath()));

                for (int i = 0; i < jsonarray.size(); ++i) {

                    JSONObject jsonObject = (JSONObject) jsonarray.get(i);

                    String msg_project = (String) jsonObject.get("project");
                    String msg_last_updated = (String) jsonObject.get("last_updated");
                    Long msg_id = (Long) jsonObject.get("id");
                    String msg_subject = (String) jsonObject.get("subject");

                    mProject.add(msg_project);
                    mLastUpdates.add(msg_last_updated);
                    mId.add(String.format("%d", msg_id.intValue()));
                    mSubject.add(msg_subject);
                }
                temp.delete();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String urls) {
            super.onPostExecute(urls);
            simpleproject = new String[ mProject.size() ];
            simplelastupdated = new String[ mLastUpdates.size() ];
            simpleid = new String[ mId.size() ];
            simplesubject = new String[ mSubject.size() ];
            mProject.toArray(simpleproject);
            mLastUpdates.toArray(simplelastupdated);
            mId.toArray(simpleid);
            mSubject.toArray(simplesubject);

            gridview = (GridView) findViewById(R.id.gridview);
            ArrayAdapter adapter = new ArrayAdapter(MainActivity.this,
                    R.layout.gridview, R.id.commit, simplesubject) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView commit = (TextView) view.findViewById(R.id.commit);
                    TextView repo = (TextView) view.findViewById(R.id.repo);
                    TextView date = (TextView) view.findViewById(R.id.date);

                    String CommitDate = null;

                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                        Date convertedCommitDate = sdf.parse(simplelastupdated[position]);
                        CommitDate = sdf.format(convertedCommitDate );
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }

                    commit.setText(simplesubject[position]);
                    commit.setTextColor(Color.parseColor("#009688"));
                    repo.setText(simpleproject[position]);
                    repo.setTextColor(Color.parseColor("#FFFFFF"));
                    date.setText(CommitDate);
                    date.setTextColor(Color.parseColor("#ff5252"));
                    return view;
                }
            };
            gridview.setAdapter(adapter);

            gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                    String review_url = String.format
                            ("http://review.cyanogenmod.org/#/c/%s", simpleid[position]);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(review_url));
                    startActivity(browserIntent);
                }
            });

            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        }
    }
}