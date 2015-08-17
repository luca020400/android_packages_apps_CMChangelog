package com.luca020400.cmchangelog;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends Activity {
    public String mDevice;
    public String mCyanogenMod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDevice = cmd.exec("getprop ro.cm.device | awk '{print $1}'");
        mCyanogenMod = cmd.exec("getprop ro.cm.version | cut -f1 -d'-' | awk '{print $1}'");

        Log.i("Device", String.valueOf(mDevice));
        Log.i("CyanogenMod", String.valueOf(mCyanogenMod));

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDialog()
                .build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
                .penaltyLog()
                .build());

        // Assume it's known
        final int ROW_ITEMS = 3;

        final class GridAdapter extends BaseAdapter {

            final ArrayList<String> mItems;
            final int mCount;

            /**
             * Default constructor
             * @param items to fill data to
             */
            private GridAdapter(final ArrayList<String> items) {

                mCount = items.size() * ROW_ITEMS;
                mItems = new ArrayList<String>(mCount);

                // for small size of items it's ok to do it here, sync way
                for (String item : items) {
                    // get separate string parts, divided by ,
                    final String[] parts = item.split(",");

                    // remove spaces from parts
                    for (String part : parts) {
                        part.replace(" ", "");
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
                    view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
                }

                final TextView text = (TextView) view.findViewById(android.R.id.text1);

                text.setText(mItems.get(position));

                return view;
            }
        }

        final GridView grid = (GridView) findViewById(R.id.gridView);
        final ArrayList<String> items = new ArrayList<String>();
        grid.setAdapter(new GridAdapter(items));

        JSONObject json = null;
        String str = "";
        HttpResponse response;
        HttpClient myClient = new DefaultHttpClient();
        HttpPost myConnection = new HttpPost(String.format("http://api.cmxlog.com/changes/%s/%s", mCyanogenMod.substring(0, 3), mDevice));

        try {
            response = myClient.execute(myConnection);
            str = EntityUtils.toString(response.getEntity(), "UTF-8");

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            JSONArray jArray = new JSONArray(str);
            json = jArray.getJSONObject(0);
            json.getString("repo");
            json.getString("last_updated");
            json.getString("id");
            json.getString("subject");
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
}
