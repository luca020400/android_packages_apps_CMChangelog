package com.luca020400.cmchangelog;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity {
    public String mDevice;
    public String mCyanogenMod;
    public String mLastUpdates;
    public String mRepo;
    public String mId;
    public String mSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] version = cmd.exec("getprop ro.cm.version").split("-");
        mDevice = version[3];
        mCyanogenMod = version[0];

        Log.i("Device", "'" + String.valueOf(mDevice) + "'");
        Log.i("CyanogenMod", "'" + String.valueOf(mCyanogenMod) + "'");

        new UpdateTask().execute(String.format
                ("http://api.cmxlog.com/changes/%s/%s", mCyanogenMod, mDevice));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the device_info; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will

        return super.onOptionsItemSelected(item);
    }

    public class GridAdapter extends BaseAdapter {

        // Assume it's known
        final int ROW_ITEMS = 2;

        final ArrayList<String> mItems;
        final int mCount;

        /**
         * Default constructor
         *
         * @param items to fill data to
         */
        private GridAdapter(final ArrayList<String> items) {

            mCount = items.size() * ROW_ITEMS;
            mItems = new ArrayList<>(mCount);

            // for small size of items it's ok to do it here, sync way
            for (String item : items) {
                // get separate string parts, divided by ,
                final String[] parts = item.split(",");

                // remove spaces from parts
                for (String part : parts) {
                    mItems.add(part);
                }
            }
        }

        @Override
        public int getCount() {
            return mCount;
        }

        @Override
        public Object getItem(final int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {

            View view = convertView;

            if (view == null) {
                view = LayoutInflater.from(parent.getContext()).inflate
                        (android.R.layout.simple_list_item_1, parent, false);
            }

            final TextView text = (TextView) view.findViewById(android.R.id.text1);

            text.setText(mItems.get(position));

            return view;
        }
    }

    private class UpdateTask extends AsyncTask<String, String, String> {
        protected String doInBackground(String... urls) {
            JSONObject json;
            try {
                JSONArray jArray = new JSONArray(Jsoup.connect(urls[0]).ignoreContentType(true).get());
                json = jArray.getJSONObject(0);
                mRepo = json.getString("repo");
                mLastUpdates = json.getString("last_updated");
                mId = json.getString("id");
                mSubject = json.getString("subject");
            } catch (JSONException e) {
                // json shitted itself
                e.printStackTrace();
            } catch (IOException e) {
                // can't connect to url (jsoup shitted itself)
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute() {
            final GridView grid = (GridView) findViewById(R.id.gridView);
            final ArrayList<String> items = new ArrayList<>();

            items.add(mRepo);
            items.add(mSubject);

            grid.setAdapter(new GridAdapter(items));
        }
    }
}
