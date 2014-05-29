/*
 * Copyright 2014 Parth Sane

   
 */
package com.laer.easycast;

//import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;



public class SecondPane extends ListFragment  {
	
	protected static final String TAG = "Second Pane";
	ViewGroup myViewGroup;
	
	String[] projection = {MediaStore.Images.Thumbnails._ID};
	private Cursor cursor;
	private int columnIndex;
	public static final String NONE = "None";
    public static final String SLIDE_LEFT = "SlideLeft";
    public static final String SLIDE_RIGHT = "SlideRight";
    public static final String DISSOLVE = "Dissolve";
    private byte[] data;
    public String recieved;
    
    
	String put="PUT";
	String photosl="/photo";
	Map<String, String> headers = new HashMap<String, String>();
	ByteArrayOutputStream wr = new ByteArrayOutputStream(); 
	HttpURLConnection conn;
	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = new View(getActivity());
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.setBackgroundColor(Color.WHITE);
        root= inflater.inflate(R.layout.secondlayout,container,false);
        setHasOptionsMenu(true);
        myViewGroup=container;
        
        cursor = getActivity().getContentResolver().query( MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
				
				projection, // Which columns to return
				
				null,       // Return all rows
				
				null,MediaStore.Images.Thumbnails.IMAGE_ID);
				
				// Get the column index of the Thumbnails Image ID
				
				columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);
				
			GridView gridView=(GridView)root.findViewById(R.id.gridView1);	 
	 
	
	gridView.setAdapter(new ImageAdapter(getActivity()));
	
	gridView.setOnItemClickListener(new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			String[] projection = {MediaStore.Images.Media.DATA};
			
			cursor = getActivity().getContentResolver().query( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			
			projection, // Which columns to return
			
			null,       // Return all rows
			
			null,
			
			null);
			
			columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			
			cursor.moveToPosition(position);
			
			// Get image filename
			String imagePath = cursor.getString(columnIndex);
			Bitmap image = BitmapFactory.decodeFile(imagePath);
			
			Log.i("ImagePath=",imagePath);
			Log.d(TAG,"Image decoded");
			photoRaw(image,NONE);
			
			
			// Use this path to do further processing, i.e. full screen display
		}
	});
	


       
        
        
        return root;
    }

public void photoRaw(Bitmap image, String transition)  {
		
		Log.i("photoraw", "photoraw called");
		
		
		headers.put("X-Apple-Transition", transition);
		
		
		
		
		image.compress(Bitmap.CompressFormat.JPEG, 100, wr);
		
		new PhotoAirplay().execute();
		
	}
private class PhotoAirplay extends AsyncTask<Void,Void,Void> {
	 

	

	@Override
	protected Void doInBackground(Void... arg0) {
		//BufferedReader reader=null;
		
		try
        { 	
			MainActivity obj=(MainActivity)getActivity();
			recieved=obj.URL;
            // Defined URL  where to send data
            //URL url = new URL("http://192.168.1.101:7000"+photosl);
            URL url = new URL(recieved+photosl);
             Log.i("Whats the URL",recieved+photosl);
         // Send PUT data request

          conn =(HttpURLConnection) url.openConnection(); 
          conn.setUseCaches(false);
          conn.setDoOutput(true);
          conn.setRequestMethod(put);
          if(headers.size()>0){
        	  
          
          conn.setRequestProperty("User-Agent","MediaControl/1.0");
          Object[] keys = headers.keySet().toArray();
          for (int i = 0; i < keys.length; i++) {
                  conn.setRequestProperty((String) keys[i],(String) headers.get(keys[i]));
          }
          }
          if(wr!=null)
          {
        	  data = wr.toByteArray();
        	  Log.i("OutputStream","Not Null Yay!");
        	  Log.i("ByteStringEquivalent",data.toString());
          }
          
          else
          {
        	  Log.e("Output Stream","NULL!!");
          }
          
          conn.setRequestProperty("Content-Length",""+data.length);
          conn.connect();
          wr.writeTo(conn.getOutputStream()); 
          wr.flush(); 
      
          // Get the server response 
          if (conn.getResponseCode() == 401) {
         
             
                      throw new IOException("Incorrect password");
              }
          if(conn.getResponseCode()==200)
          {
        	  Log.v("HTTPResponse","Response code 200 OK");
          }
         
          Log.v("HTTPResponse",conn.getResponseMessage());
        
        //StringBuilder sb = new StringBuilder();
        /*String line;
        
        // Read Server Response
        InputStream is = conn.getInputStream();
                        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                        
                        StringBuffer response = new StringBuffer(); 
                        while((line = rd.readLine()) != null) {
                                response.append(line);
                                response.append("\r\n");
                        }
                        rd.close();
         Log.v("Apple-TV Response",response.toString());  */
        }
        catch(IOException ex)
        {
        	ex.printStackTrace();
             
        }
        finally
        {
            try
            {
 
                wr.reset();
            	wr.close();
            	
            	
            }

            catch(IOException ex) {
            	ex.printStackTrace();
            }
        }
		return null;
		
              
        // Show response on activity
        
        
    
		
       
	}
	
   
}		
	 
	private class ImageAdapter extends BaseAdapter {
	
		 
		
		private Context context;
		
		 
		
		public ImageAdapter(Context localContext) {
			
			context = localContext;
			
			}
		
		 
		
		public int getCount() {
		
		return cursor.getCount();
		
		}
		
		public Object getItem(int position) {
		
		return position;
		
		}
		
		public long getItemId(int position) {
		
		return position;
		
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
		
		ImageView picturesView;
				if (convertView == null) {
		
		picturesView = new ImageView(context);
		
		// Move cursor to current position
		
		cursor.moveToPosition(position);
		
		// Get the current value for the requested column
				int imageID = cursor.getInt(columnIndex);
		
		// Set the content of the image based on the provided URI
		
		picturesView.setImageURI(Uri.withAppendedPath(
		
		MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, "" + imageID));
		
		picturesView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		
		picturesView.setPadding(8, 8, 8, 8);
		
		picturesView.setLayoutParams(new GridView.LayoutParams(100, 100));
		
		}
		
		else {
		
		picturesView = (ImageView)convertView;
		
		}
		
		return picturesView;
		
		}
		
		}
	
}
