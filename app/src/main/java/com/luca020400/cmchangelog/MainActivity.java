package com.luca020400.cmchangelog;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

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
    public String mDevice;
    public String mCMVersion;
    public String mCyanogenMod;
    public String mCMReleaseType;
    ArrayList<String> mRepo = new ArrayList<>();
    ArrayList<String> mLastUpdates = new ArrayList<>();
    ArrayList<String> mId = new ArrayList<>();
    ArrayList<String> mSubject = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                DeviceInfo();
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
        messageView.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Small);
    }

    private class UpdateTask extends AsyncTask<String, String, String> {
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
                Iterator<String> iterator_repo = msg_repo.iterator();
                while (iterator_repo.hasNext()) {
                    mRepo.add(iterator_repo.next());
                }
                // Last Updated
                JSONArray msg_last_updated = (JSONArray) jsonObject.get("last_updated");
                Iterator<String> iterator_last_updated = msg_last_updated.iterator();
                while (iterator_repo.hasNext()) {
                    mLastUpdates.add(iterator_repo.next());
                }
                // Commit ID
                JSONArray msg_id = (JSONArray) jsonObject.get("id");
                Iterator<String> iterator_id = msg_repo.iterator();
                while (iterator_repo.hasNext()) {
                    mId.add(iterator_repo.next());
                }
                // Commit message
                JSONArray msg_subject = (JSONArray) jsonObject.get("subject");
                Iterator<String> iterator_subject = msg_subject.iterator();
                while (iterator_repo.hasNext()) {
                    mSubject.add(iterator_repo.next());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Log.d("Repo", String.format(mRepo.get(1)));
            return null;
        }
    }
}