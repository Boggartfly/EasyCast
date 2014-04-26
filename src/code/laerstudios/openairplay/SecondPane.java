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
package code.laerstudios.openairplay;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
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
	MainActivity obj=new MainActivity();
	String[] projection = {MediaStore.Images.Thumbnails._ID};
	private Cursor cursor;
	private int columnIndex;
	public static final String NONE = "None";
    public static final String SLIDE_LEFT = "SlideLeft";
    public static final String SLIDE_RIGHT = "SlideRight";
    public static final String DISSOLVE = "Dissolve";
    
    
    ArrayList<String> obj1 = new ArrayList<String>();
	ArrayList<ByteArrayOutputStream> obj2 = new ArrayList<ByteArrayOutputStream>();
	ArrayList<Map<String,String>> obj3 = new ArrayList<Map<String,String>>();
	String put="PUT";
	String photosl="/photo";
	Map<String, String> headers = new HashMap<String, String>();
	   
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
			try
			{
			Log.d(TAG,"Image decoded");
			photoRaw(image,NONE);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			// Use this path to do further processing, i.e. full screen display
		}
	});
	


       
        
        
        return root;
    }

public void photoRaw(Bitmap image, String transition) throws IOException {
		
		Log.i("photoraw", "photoraw called");
		
		
		headers.put("X-Apple-Transition", transition);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		 
		
		image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		obj1.add(put);
		obj1.add(photosl);
		obj2.add(stream);
		obj3.add(headers);
		new PhotoAirplay().execute();
		
	}
private class PhotoAirplay extends AsyncTask<Void,Void,Void> {
	 @Override
	    protected void onPreExecute() {
		
	    }

	

	@Override
	protected Void doInBackground(Void... arg0) {
		BufferedReader reader=null;
		 
		try
        { 	String data = URLEncoder.encode("name", "UTF-8");
          
            // Defined URL  where to send data
            URL url = new URL(obj.Url);
             
         // Send POST data request

          URLConnection conn = url.openConnection(); 
          conn.setDoOutput(true); 
          OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()); 
          wr.write( data ); 
          wr.flush(); 
      
          // Get the server response 
           
        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line = null;
        
        // Read Server Response
        while((line = reader.readLine()) != null)
            {
                   // Append server response in string
                   sb.append(line + "\n");
            }
            
            
           
        }
        catch(Exception ex)
        {
             
        }
        finally
        {
            try
            {
 
                reader.close();
            }

            catch(Exception ex) {}
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
