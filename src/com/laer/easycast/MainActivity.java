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

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SlidingPaneLayout;
import android.transition.Explode;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener {

	private ViewPager viewPager;
	private TabsPagerAdapter mAdapter;
	private ActionBar actionBar;

	// Tab titles
	private String[] tabs = { "Images", "Videos", "Music" };
	private String type = "_airplay._tcp.local.";
	private JmDNS jmdns = null;
	private ServiceListener listener = null;
	// private ServiceInfo serviceInfo;
	android.net.wifi.WifiManager.MulticastLock lock;
	android.os.Handler handler = new android.os.Handler();
	final static String TAG = "Open Airplay";
	boolean isfound = false;

	ImagePane sender;
	public String URL;

	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setExitTransition(new Explode());
		View root = getLayoutInflater().inflate(R.layout.activity_main, null);
		
		setContentView(root);
		SlidingPaneLayout slidingPaneLayout = SlidingPaneLayout.class.cast(root
				.findViewById(R.id.slidingpanelayout));
		slidingPaneLayout
				.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {

					@Override
					public void onPanelSlide(View view, float v) {
					}

					@Override
					public void onPanelOpened(View view) {

						switch (view.getId()) {
						case R.id.fragment_secondpane:
							getSupportFragmentManager().findFragmentById(
									R.id.fragment_firstpane).setHasOptionsMenu(
									true);
							getSupportFragmentManager().findFragmentById(
									R.id.fragment_secondpane)
									.setHasOptionsMenu(false);
							System.out.println("Panel Opened");
							break;
						default:
							break;
						}
					}

					@Override
					public void onPanelClosed(View view) {

						switch (view.getId()) {
						case R.id.fragment_secondpane:
							getSupportFragmentManager().findFragmentById(
									R.id.fragment_firstpane).setHasOptionsMenu(
									false);
							getSupportFragmentManager().findFragmentById(
									R.id.fragment_secondpane)
									.setHasOptionsMenu(true);
							System.out.println("Panel Closed");
							break;
						default:
							break;
						}
					}
				});
		viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();
		mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

		viewPager.setAdapter(mAdapter);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Adding Tabs
		for (String tab_name : tabs) {
			actionBar.addTab(actionBar.newTab().setText(tab_name)
					.setTabListener(this));
		}

		/**
		 * on swiping the viewpager make respective tab selected
		 * */
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// on changing the page
				// make respected tab selected
				actionBar.setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		new DeviceSearch().execute();
		android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) getSystemService(android.content.Context.WIFI_SERVICE);
		lock = wifi.createMulticastLock("Airlock");
		lock.setReferenceCounted(false);
		lock.acquire();

	}

	/** Called when the activity is first created. */

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

	public void buildURL(final String url) {
		handler.postDelayed(new Runnable() {
			public void run() {
				Toast.makeText(getApplicationContext(), "Apple TV Found!",
						Toast.LENGTH_SHORT).show();
				/*
				 * TextView t = (TextView)findViewById(R.id.text);
				 * t.setText(msg+"\n=== "+t.getText());
				 */}
		}, 1);

		Log.i(TAG, " Apple TV Found!");
		Log.i(TAG, " IP Address Is:" + url);
		isfound = true;
		URL = url;

	}

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		// on tab selected
		// show respected fragment view
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	public void makeVisible(final View myView) {
		// get the center for the clipping circle
		int cx = (myView.getLeft() + myView.getRight()) / 2;
		int cy = (myView.getTop() + myView.getBottom()) / 2;

		// get the initial radius for the clipping circle
		int initialRadius = myView.getWidth();

		// create the animation (the final radius is zero)
		ValueAnimator anim = ViewAnimationUtils.createCircularReveal(myView,
				cx, cy, initialRadius, 0);

		// make the view invisible when the animation is done
		anim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				myView.setVisibility(View.INVISIBLE);
			}
		});
	}

	public void hideView(View myView) {

		// get the center for the clipping circle
		int cx = (myView.getLeft() + myView.getRight()) / 2;
		int cy = (myView.getTop() + myView.getBottom()) / 2;

		// get the final radius for the clipping circle
		int finalRadius = myView.getWidth();

		// create and start the animator for this view
		// (the start radius is zero)
		ValueAnimator anim = ViewAnimationUtils.createCircularReveal(myView,
				cx, cy, 0, finalRadius);
		anim.start();
	}
}
