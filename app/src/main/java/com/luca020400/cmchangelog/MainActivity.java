package com.luca020400.cmchangelog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends Activity {
    private ProgressDialog mProgressDialog;
    public String mDevice;
    public String mCMVersion;
    public String mCyanogenMod;
    public String mCMReleaseType;
    ArrayList<String> mRepo = new ArrayList<>();
    ArrayList<String> mLastUpdates = new ArrayList<>();
    ArrayList<String> mId = new ArrayList<>();
    ArrayList<String> mSubject = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] version = cmd.exec("getprop ro.cm.version").split("-");
        mCMVersion = cmd.exec("getprop ro.cm.version");
        mCyanogenMod = version[0];
        mCMReleaseType = version[2];
        mDevice = version[3];

        new UpdateTask().execute(String.format
                ("http://api.cmxlog.com/changes/%s/%s", mCyanogenMod, mDevice));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, R.string.device_info)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        menu.add(0, 0, 0, R.string.update_changelog)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                DeviceInfo();
                return true;
            case 1:
                UpdateChangelog();
                return true;
        }
        return false;
    }

    private void DeviceInfo() {
        String message = getString(R.string.devive_info_device) + " " + mDevice + "\n\n"
                + getString(R.string.devive_info_running) + " " + mCMVersion + "\n\n"
                + getString(R.string.devive_info_update_channel) + " " + mCMReleaseType;

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.device_info)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_ok, null);

        AlertDialog dialog = builder.create();
        dialog.show();

        TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
        messageView.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Small);
    }

    private void UpdateChangelog() {
        if (mProgressDialog != null) {
            return;
        }

        if (isOnline()) {
            Toast.makeText(this, R.string.data_connection_required, Toast.LENGTH_SHORT).show();
            return;
        }

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(R.string.checking_for_updates);
        mProgressDialog.setMessage(getString(R.string.checking_for_updates));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);

        new UpdateTask().execute(String.format
                ("http://api.cmxlog.com/changes/%s/%s", mCyanogenMod, mDevice));
        mProgressDialog.show();
    }

    public boolean isOnline() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class UpdateTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            if (isOnline()) {
                Toast.makeText(MainActivity.this, R.string.data_connection_required, Toast.LENGTH_SHORT).show();
            }
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
                Object obj = parser.parse(new FileReader(temp.getAbsolutePath()));
                JSONObject jsonObject = (JSONObject) obj;
                // Repo Name
                JSONArray msg_repo = (JSONArray) jsonObject.get("repo");
                if (!msg_repo.isEmpty()) {
                    Iterator<String> iterator_repo = msg_repo.iterator();
                    while (iterator_repo.hasNext()) {
                        mRepo.add(iterator_repo.next());
                    }
                }
                // Last Updated
                JSONArray msg_last_updated = (JSONArray) jsonObject.get("last_updated");
                if (!msg_last_updated.isEmpty()) {
                    Iterator<String> iterator_last_updated = msg_last_updated.iterator();
                    while (iterator_last_updated.hasNext()) {
                        mLastUpdates.add(iterator_last_updated.next());
                    }
                }
                // Commit ID
                JSONArray msg_id = (JSONArray) jsonObject.get("id");
                if (!msg_id.isEmpty()) {
                    Iterator<String> iterator_id = msg_repo.iterator();
                    while (iterator_id.hasNext()) {
                        mId.add(iterator_id.next());
                    }
                }
                // Commit message
                JSONArray msg_subject = (JSONArray) jsonObject.get("subject");
                if (!msg_subject.isEmpty()) {
                    Iterator<String> iterator_subject = msg_subject.iterator();
                    while (iterator_subject.hasNext()) {
                        mSubject.add(iterator_subject.next());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute() {
            if (mProgressDialog != null) {
                mProgressDialog.hide();
            }
        }
    }
}