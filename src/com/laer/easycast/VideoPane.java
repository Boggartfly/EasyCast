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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class VideoPane extends Fragment {

	ViewGroup myViewGroup;
	String[] projection = { MediaStore.Video.Thumbnails._ID };
	private Cursor cursor;
	private int columnIndex;
	public static final String NONE = "None";
	public static final String SLIDE_LEFT = "SlideLeft";
	public static final String SLIDE_RIGHT = "SlideRight";
	public static final String DISSOLVE = "Dissolve";
	private byte[] data;
	public String recieved;
	protected static final String TAG = "VideoPane";

	String put = "PUT";
	String videosl = "/play";
	Map<String, String> headers = new HashMap<String, String>();
	ByteArrayOutputStream wr = new ByteArrayOutputStream();
	HttpURLConnection conn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = new View(getActivity());
		root.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
		root.setBackgroundColor(Color.WHITE);
		root = inflater.inflate(R.layout.imagepane, container, false);
		setHasOptionsMenu(true);
		myViewGroup = container;

		cursor = getActivity().getContentResolver().query(
				MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,

				projection, // Which columns to return

				null, // Return all rows

				null, MediaStore.Video.Thumbnails.VIDEO_ID);

		// Get the column index of the Thumbnails Image ID

		columnIndex = cursor
				.getColumnIndexOrThrow(MediaStore.Video.Thumbnails._ID);

		GridView gridView = (GridView) root.findViewById(R.id.gridView1);

		gridView.setAdapter(new ImageAdapter(getActivity()));

		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				String[] projection = { MediaStore.Video.Media.DATA };
				try {
					cursor = getActivity().getContentResolver().query(
							MediaStore.Video.Media.EXTERNAL_CONTENT_URI,

							projection, // Which columns to return

							null, // Return all rows

							null,

							null);
					columnIndex = cursor
							.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
				} catch (SQLiteException e) {

				}

				cursor.moveToPosition(position);

				// Get image filename
				String imagePath = cursor.getString(columnIndex);
				// Bitmap image = BitmapFactory.decodeFile(imagePath);

				Log.i("VideoPath=", imagePath);
				Log.d(TAG, "Video decoded");
				// videoRaw(image,NONE);

				// Use this path to do further processing, i.e. full screen
				// display
			}
		});

		return root;
	}

	public void videoRaw(Bitmap image, String transition) {

		Log.i("photoraw", "photoraw called");

		headers.put("X-Apple-Transition", transition);

		image.compress(Bitmap.CompressFormat.JPEG, 100, wr);
		MainActivity obj = (MainActivity) getActivity();

		WifiManager wifi = (WifiManager) getActivity().getSystemService(
				Context.WIFI_SERVICE);
		if (obj.URL != null) {
			new VideoAirplay().execute();
		} else if (obj.URL == null || wifi.isWifiEnabled() != true) {
			WiFiOptions();

			if (obj.URL == null) {
				Toast.makeText(getActivity(), "No compatible devices found",
						Toast.LENGTH_SHORT).show();
			}

		}

	}

	public void WiFiOptions() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					// Yes button clicked
					WifiManager wifiManager = (WifiManager) getActivity()
							.getSystemService(Context.WIFI_SERVICE);
					wifiManager.setWifiEnabled(true);
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// No button clicked
					break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(
				"WiFi needs to be on for streaming to a device. Would you like to turn it on?")
				.setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("No", dialogClickListener).show();
	}

	private class VideoAirplay extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			// BufferedReader reader=null;

			try {
				MainActivity obj = (MainActivity) getActivity();
				recieved = obj.URL;
				// Defined URL where to send data
				// URL url = new URL("http://192.168.1.101:7000"+videosl);
				URL url = new URL(recieved + videosl);
				Log.i("Whats the URL", recieved + videosl);
				// Send PUT data request

				conn = (HttpURLConnection) url.openConnection();
				conn.setUseCaches(false);
				conn.setDoOutput(true);
				conn.setRequestMethod(put);
				if (headers.size() > 0) {

					conn.setRequestProperty("User-Agent", "MediaControl/1.0");
					Object[] keys = headers.keySet().toArray();
					for (int i = 0; i < keys.length; i++) {
						conn.setRequestProperty((String) keys[i],
								(String) headers.get(keys[i]));
					}
				}
				if (wr != null) {
					data = wr.toByteArray();
					Log.i("OutputStream", "Not Null Yay!");
					Log.i("ByteStringEquivalent", data.toString());
				}

				else {
					Log.e("Output Stream", "NULL!!");
				}

				conn.setRequestProperty("Content-Length", "" + data.length);
				conn.connect();
				wr.writeTo(conn.getOutputStream());
				wr.flush();

				// Get the server response
				if (conn.getResponseCode() == 401) {

					throw new IOException("Incorrect password");
				}
				if (conn.getResponseCode() == 200) {
					Log.v("HTTPResponse", "Response code 200 OK");
				}

				Log.v("HTTPResponse", conn.getResponseMessage());

				// StringBuilder sb = new StringBuilder();
				/*
				 * String line;
				 * 
				 * // Read Server Response InputStream is =
				 * conn.getInputStream(); BufferedReader rd = new
				 * BufferedReader(new InputStreamReader(is));
				 * 
				 * StringBuffer response = new StringBuffer(); while((line =
				 * rd.readLine()) != null) { response.append(line);
				 * response.append("\r\n"); } rd.close();
				 * Log.v("Apple-TV Response",response.toString());
				 */
			} catch (IOException ex) {
				ex.printStackTrace();

			} finally {
				try {

					wr.reset();
					wr.close();

				}

				catch (IOException ex) {
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
				int videoID = cursor.getInt(columnIndex);

				// Set the content of the image based on the provided URI

				picturesView.setImageURI(Uri.withAppendedPath(
						MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, ""
								+ videoID));

				picturesView.setScaleType(ImageView.ScaleType.FIT_CENTER);

				picturesView.setPadding(8, 8, 8, 8);

				picturesView
						.setLayoutParams(new GridView.LayoutParams(250, 250));

			}

			else {

				picturesView = (ImageView) convertView;

			}

			return picturesView;

		}

	}

}
