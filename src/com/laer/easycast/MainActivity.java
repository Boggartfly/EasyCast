/*
 * Copyright 2014 Parth Sane

  
 */


package com.laer.easycast;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import com.laer.easycast.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;






public class MainActivity extends FragmentActivity {

	
	private String type = "_airplay._tcp.local.";
    private JmDNS jmdns = null;
    private ServiceListener listener = null;
    //private ServiceInfo serviceInfo;
    android.net.wifi.WifiManager.MulticastLock lock;
    android.os.Handler handler = new android.os.Handler();
    final static String TAG="Open Airplay";
    boolean isfound=false;
	
	public ProgressBar spinner;
	SecondPane sender;
	public String URL;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final View root = getLayoutInflater().inflate(R.layout.activity_main, null);
        setContentView(root);
        final SlidingPaneLayout slidingPaneLayout = SlidingPaneLayout.class.cast(root.findViewById(R.id.slidingpanelayout));
        slidingPaneLayout.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
        	
            @Override
            public void onPanelSlide(View view, float v) {
            }

            @Override
            public void onPanelOpened(View view) {

                switch (view.getId()) {
                    case R.id.fragment_secondpane:
                        getSupportFragmentManager().findFragmentById(R.id.fragment_firstpane).setHasOptionsMenu(true);
                        getSupportFragmentManager().findFragmentById(R.id.fragment_secondpane).setHasOptionsMenu(false);
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
                        getSupportFragmentManager().findFragmentById(R.id.fragment_firstpane).setHasOptionsMenu(false);
                        getSupportFragmentManager().findFragmentById(R.id.fragment_secondpane).setHasOptionsMenu(true);
                        System.out.println("Panel Closed");
                        break;
                    default:
                        break;
                }
            }
        });

        new DeviceSearch().execute();
        android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager)getSystemService(android.content.Context.WIFI_SERVICE);
        lock = wifi.createMulticastLock("Airlock");
        lock.setReferenceCounted(false);
        lock.acquire();
        
		
		
		
    }    /** Called when the activity is first created. */
       

    
        


    
  //This method will override by child class. Then base class can get the fragment
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
    Log.i(TAG,"Lock Released");
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
    Log.i(TAG,"Lock Released");
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
    Log.i(TAG,"Lock Released");
	super.onStop();
}
	@Override
    protected void onResume() {
	new DeviceSearch().execute();
	super.onResume();
}
	
	 private class DeviceSearch extends AsyncTask<Void,Void,Integer> {
		 @Override
		    protected void onPreExecute() {
			 spinner = (ProgressBar)findViewById(R.id.progressBar1);
			 spinner.setVisibility(View.VISIBLE);  
		    }

		@Override
		protected Integer doInBackground(Void... arg0) {
			
			
	        try {
	            jmdns = JmDNS.create();
	            jmdns.addServiceListener(type, listener = new ServiceListener() {

	                @SuppressWarnings("deprecation")
					@Override
	                public void serviceResolved(ServiceEvent ev) {
	                    buildURL("http://" +ev.getInfo().getHostAddress()+":"+ ev.getInfo().getPort());
	                }

	                @Override
	                public void serviceRemoved(ServiceEvent ev) {
	                  buildURL("Service removed: " + ev.getName());
	                  //spinner.setVisibility(View.VISIBLE);
	                }

	                @Override
	                public void serviceAdded(ServiceEvent event) {
	                    // Required to force serviceResolved to be called again (after the first search)
	                    jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
	                }
	            });
	          //  serviceInfo = ServiceInfo.create("_test._tcp.local.", "AndroidTest", 0, "plain test service from android");
	            //jmdns.registerService(serviceInfo);
	            Log.i(TAG," Service Discovery Started");
	        } catch (IOException e) {
	            e.printStackTrace();
	            return 1;
	        }
	        
	        
			return 1;
		}
		protected void onPostExecute(Integer result) {
			 spinner.setVisibility(View.GONE);
        }
        
			
		 
}
	 public void buildURL(final String url) {
	        handler.postDelayed(new Runnable() {
	            public void run() {
	            	Toast.makeText(getApplicationContext(), "Apple TV Found!", Toast.LENGTH_SHORT).show();
	      /*  TextView t = (TextView)findViewById(R.id.text);
	        t.setText(msg+"\n=== "+t.getText());
	        */}
	            }, 1);
	
	       
	    	Log.i(TAG," Apple TV Found!");
	    	Log.i(TAG," IP Address Is:"+url);
	    	isfound=true;
	    	URL=url;
	    	
		 }
}
		 

