/*
 * Copyright 2014 Parth Sane
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.laer.easycast;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

@SuppressWarnings("deprecation")
public class MainActivity extends FragmentActivity {


    final static String TAG = "Open Airplay";
    public String URL;
    // private ServiceInfo serviceInfo;
    android.net.wifi.WifiManager.MulticastLock lock;
    android.os.Handler handler = new android.os.Handler();
    boolean isfound = false;
    private String type = "_airplay._tcp.local.";
    private JmDNS jmdns = null;
    private ServiceListener listener = null;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        View root = getLayoutInflater().inflate(R.layout.activity_main, null);

        setContentView(root);

        // Initialize the ViewPager and set an adapter
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new TabsStatePagerAdapter((getSupportFragmentManager())));
        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        //Expand tabs as required.
        tabs.setShouldExpand(true);
        tabs.setViewPager(pager);
        // Bind the tabs to the ViewPager

        // continued from above
        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
        });
        new DeviceSearch().execute();
        android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) getSystemService(android.content.Context.WIFI_SERVICE);
        lock = wifi.createMulticastLock("Airlock");
        lock.setReferenceCounted(false);
        lock.acquire();

    }

    /**
     * Called when the activity is first created.
     */

    // This method will override by child class. Then base class can get the
    // fragment
    protected Fragment getSampleFragment() {
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        if (jmdns != null) {
            if (listener != null) {
                jmdns.removeServiceListener(type, listener);
                listener = null;
            }
            jmdns.unregisterAllServices();
            try {
                jmdns.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            jmdns = null;
        }

        lock.release();
        Log.i(TAG, "Lock Released");
        super.onStop();
    }

    @Override
    protected void onStop() {
        if (jmdns != null) {
            if (listener != null) {
                jmdns.removeServiceListener(type, listener);
                listener = null;
            }
            jmdns.unregisterAllServices();
            try {
                jmdns.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            jmdns = null;
        }

        lock.release();
        Log.i(TAG, "Lock Released");
        super.onStop();
    }

    @Override
    protected void onPause() {
        if (jmdns != null) {
            if (listener != null) {
                jmdns.removeServiceListener(type, listener);
                listener = null;
            }
            jmdns.unregisterAllServices();
            try {
                jmdns.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            jmdns = null;
        }

        lock.release();
        Log.i(TAG, "Lock Released");
        super.onStop();
    }

    @Override
    protected void onResume() {
        new DeviceSearch().execute();
        super.onResume();
    }

    public void buildURL(final String url) {
        handler.postDelayed(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "Apple TV Found!",
                        Toast.LENGTH_SHORT).show();
                /*
				 * TextView t = (TextView)findViewById(R.id.text);
				 * t.setText(msg+"\n=== "+t.getText());
				 */
            }
        }, 1);

        Log.i(TAG, " Apple TV Found!");
        Log.i(TAG, " IP Address Is:" + url);
        isfound = true;
        URL = url;

    }

    private class DeviceSearch extends AsyncTask<Void, Void, Integer> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Integer doInBackground(Void... arg0) {

            try {
                jmdns = JmDNS.create();
                jmdns.addServiceListener(type,
                        listener = new ServiceListener() {

                            @Override
                            public void serviceResolved(ServiceEvent ev) {
                                buildURL("http://"
                                        + ev.getInfo().getHostAddress() + ":"
                                        + ev.getInfo().getPort());
                            }

                            @Override
                            public void serviceRemoved(ServiceEvent ev) {
                                buildURL("Service removed: " + ev.getName());
                                // spinner.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void serviceAdded(ServiceEvent event) {
                                // Required to force serviceResolved to be
                                // called again (after the first search)
                                jmdns.requestServiceInfo(event.getType(),
                                        event.getName(), 1);
                            }
                        });
                // serviceInfo = ServiceInfo.create("_test._tcp.local.",
                // "AndroidTest", 0, "plain test service from android");
                // jmdns.registerService(serviceInfo);
                Log.i(TAG, " Service Discovery Started");
            } catch (IOException e) {
                e.printStackTrace();
                return 1;
            }

            return 0;
        }

        protected void onPostExecute(Integer result) {

        }

    }


}
