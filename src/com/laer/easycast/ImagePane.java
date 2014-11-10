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


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.util.LruCache;
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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ImagePane extends ListFragment {

    public static final String NONE = "None";
    public static final String SLIDE_LEFT = "SlideLeft";
    public static final String SLIDE_RIGHT = "SlideRight";
    public static final String DISSOLVE = "Dissolve";
    protected static final String TAG = "ImagePane";
    // Get max available VM memory, exceeding this amount will throw an
    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
    // int in its constructor.
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    // Use 1/8th of the available memory for this memory cache.
    final int cacheSize = maxMemory / 8;
    private final String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
    public String recieved;
    ViewGroup myViewGroup;
    String put = "PUT";
    String photosl = "/photo";
    Map<String, String> headers = new HashMap<String, String>();
    ByteArrayOutputStream wr = new ByteArrayOutputStream();
    HttpURLConnection conn;
    Bitmap mPlaceHolderBitmap;
    int pos;
    private LruCache<String, Bitmap> mMemoryCache;
    private Cursor cursor;
    private int columnIndex;
    private byte[] data;

    public static boolean cancelPotentialWork(int data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final int bitmapData = bitmapWorkerTask.imageID;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == 0 || bitmapData != data) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlaceHolderBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.loading);
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

    }

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
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,

                projection, // Which columns to return

                null, // Return all rows

                null,

                MediaStore.Images.Media.DEFAULT_SORT_ORDER);

        GridView gridView = (GridView) root.findViewById(R.id.gridView1);

        gridView.setAdapter(new ImageAdapter(getActivity()));


        gridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {


                columnIndex = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToPosition(position);


                // Get image filename
                String imagePath = cursor.getString(columnIndex);
                Bitmap image = BitmapFactory.decodeFile(imagePath);

                Log.i("ImagePath=", imagePath);
                Log.d(TAG, "Image decoded");
                photoRaw(image, NONE);

                // Use this path to do further processing, i.e. full screen
                // display
            }
        });

        return root;
    }

    public void photoRaw(Bitmap image, String transition) {

        Log.i("photoraw", "photoraw called");

        headers.put("X-Apple-Transition", transition);

        image.compress(Bitmap.CompressFormat.JPEG, 100, wr);
        MainActivity obj = (MainActivity) getActivity();
        WifiManager wifi = (WifiManager) getActivity().getSystemService(
                Context.WIFI_SERVICE);
        if (obj.URL != null) {
            new PhotoAirplay().execute();
        } else if (obj.URL == null && !wifi.isWifiEnabled()) {
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
                "WiFi needs to be on for com.laer.easycast.streaming to a device. Would you like to turn it on?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};

        //This method was deprecated in API level 11
        //Cursor cursor = managedQuery(contentUri, proj, null, null, null);

        CursorLoader cursorLoader = new CursorLoader(
                getActivity(),
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        int column_index =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    /**
     * Async task for loading the images from the SD card.
     */
    public Uri downScale(Uri uri) {

        BufferedInputStream in;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getActivity().getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();
        Bitmap b = BitmapFactory.decodeFile(filePath, options);
        File tempdir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/EasyCast/");
        tempdir.mkdirs();
        File temp = new File(tempdir, "temp.png");
        if (temp.exists()) {
            temp.delete();
        }
        try {

            FileOutputStream out = new FileOutputStream(temp);
            b.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.parse(temp.getPath());
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public void loadBitmap(int resId, ImageView imageView) {
        if (cancelPotentialWork(resId, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(resId, imageView);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(getResources(), mPlaceHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(resId);

        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {

            return bitmapWorkerTaskReference.get();
        }
    }

    private class PhotoAirplay extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            // BufferedReader reader=null;

            try {
                MainActivity obj = (MainActivity) getActivity();
                recieved = obj.URL;
                // Defined URL where to send data
                // URL url = new URL("http://192.168.1.101:7000"+videosl);
                URL url = new URL(recieved + photosl);
                Log.i("Whats the URL", recieved + photosl);
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
                } else {
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

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return null;

            // Show response on activity

        }

    }

    private class ImageAdapter extends BaseAdapter {

        ArrayList<String> itemList = new ArrayList<String>();
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
                cursor = getActivity().getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,

                        projection, // Which columns to return

                        null, // Return all rows

                        null,

                        MediaStore.Images.Media.DEFAULT_SORT_ORDER);

                cursor.moveToPosition(position);

                // Get the current value for the requested column
                int imageID = cursor.getInt(columnIndex);


                loadBitmap(imageID, picturesView);

                picturesView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                picturesView.setPadding(8, 8, 8, 8);

                picturesView
                        .setLayoutParams(new GridView.LayoutParams(250, 250));

            } else {

                picturesView = (ImageView) convertView;

            }

            return picturesView;

        }


    }

    class BitmapWorkerTask extends AsyncTask<Integer, Void, Uri> {
        private final WeakReference<ImageView> imageViewReference;
        private int imageID = 0;

        public BitmapWorkerTask(int imageID, ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.imageID = imageID;
        }

        // Decode image in background.
        @Override
        protected Uri doInBackground(Integer... params) {

            return downScale(Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + imageID));
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Uri uri) {
            if (isCancelled()) {
                uri = null;
            }
            if (imageViewReference != null && uri != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask =
                        getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageURI(uri);


                }
            }
        }


    }
}
