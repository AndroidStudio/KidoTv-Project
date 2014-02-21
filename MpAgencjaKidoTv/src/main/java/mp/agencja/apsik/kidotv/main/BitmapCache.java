package mp.agencja.apsik.kidotv.main;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class BitmapCache {

    private static final String LOG_TAG = "Cache";
    private final LruCache<String, Bitmap> mBitmapCache;
    private final ArrayList<String> mCurrentTasks;

    public BitmapCache(int size) {
        this.mCurrentTasks = new ArrayList<String>();
        this.mBitmapCache = new LruCache<String, Bitmap>(size) {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
                    return bitmap.getHeight() * bitmap.getWidth() * 2;
                }
                return bitmap.getByteCount();
            }
        };
    }

    private void addBitmapToCache(String key, Bitmap bitmap) {
        synchronized (this) {
            if (getBitmapFromCache(key) == null) {
                mBitmapCache.put(key, bitmap);
            }
        }
    }

    private Bitmap getBitmapFromCache(String key) {
//        Log.d(LOG_TAG, "MaxSize: " + mBitmapCache.maxSize() + " bytes");
//        Log.d(LOG_TAG, "Size:    " + mBitmapCache.size() + " bytes");
        return mBitmapCache.get(key);
    }

    public void loadBitmap(String play_list_id, ImageView imageView, ViewPagerAdapter.GridViewAdapter gridViewAdapter, ProgressBar progressBar) {
        final Bitmap bitmap = getBitmapFromCache(play_list_id);
        if (bitmap != null) {
            progressBar.setVisibility(View.INVISIBLE);
            imageView.setImageBitmap(bitmap);
        } else {
            if (!mCurrentTasks.contains(play_list_id)) {
                final DecodeImageTask decodeImageTask = new DecodeImageTask(play_list_id, gridViewAdapter, imageView);
                decodeImageTask.execute();
            }
        }
    }

    public class DecodeImageTask extends AsyncTask<Integer, Integer, Bitmap> {
        private final String play_list_id;
        private final ViewPagerAdapter.GridViewAdapter gridViewAdapter;
        private final ImageView imageView;

        public DecodeImageTask(String play_list_id, ViewPagerAdapter.GridViewAdapter gridViewAdapter, ImageView imageView) {
            this.play_list_id = play_list_id;
            this.gridViewAdapter = gridViewAdapter;
            this.imageView = imageView;
        }

        @Override
        protected void onPreExecute() {
            mCurrentTasks.add(play_list_id);
        }

        @Override
        protected Bitmap doInBackground(Integer... id) {

            final String imageUrl = getPlayListImageUrl(play_list_id);
            if (imageUrl == null) return null;

            final HttpClient client = AndroidHttpClient.newInstance("Android");
            final HttpGet request = new HttpGet(imageUrl);
            Bitmap bitmap = null;

            try {
                HttpResponse response = client.execute(request);
                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    Log.w(LOG_TAG, "Error while retrieving bitmap from " + play_list_id);
                    return null;
                }
                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    try {
                        byte[] bytes = EntityUtils.toByteArray(entity);
                        if (bytes != null) {
                            bitmap = decodeBitmap(bytes);
                            addBitmapToCache(play_list_id, bitmap);
                            return bitmap;
                        }
                        return null;
                    } finally {
                        entity.consumeContent();
                    }
                }
            } catch (Exception e) {
                request.abort();
                Log.w(LOG_TAG, "Error while retrieving bitmap from " + play_list_id, e);
            } finally {
                ((AndroidHttpClient) client).close();
            }
            return bitmap;
        }

        private String getPlayListImageUrl(String play_list_id) {
            final HttpClient httpClient = CustomHttpClient.getHttpClient();
            final String youtubeData = "http://gdata.youtube.com/feeds/api/playlists/" + play_list_id + "?v=2&prettyprint=true&alt=json";
            String imageUrl = null;
            try {
                final HttpGet request = new HttpGet(youtubeData);
                final HttpResponse response = httpClient.execute(request);
                final StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    final HttpEntity httpEntity = response.getEntity();
                    InputStream inputStream = httpEntity.getContent();
                    try {
                        final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                        final StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        final JSONObject jsonObject = new JSONObject(sb.toString());
                        final JSONObject feed = jsonObject.getJSONObject("feed");
                        final JSONObject mediaAndGroup = feed.getJSONObject("media$group");
                        final JSONArray mediaThumbial = mediaAndGroup.getJSONArray("media$thumbnail");
                        final JSONObject jsonRow;
                        jsonRow = mediaThumbial.getJSONObject(2);
                        imageUrl = jsonRow.getString("url");

                        br.close();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.getMessage());
                        return null;
                    } finally {
                        inputStream.close();
                    }
                } else {
                    return null;
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                return null;
            }
            Log.w(LOG_TAG, imageUrl);
            return imageUrl;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mCurrentTasks.remove(play_list_id);
            if (bitmap != null) {
                gridViewAdapter.notifyDataSetChanged();
            }
        }

        private Bitmap decodeBitmap(byte[] bytes) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = true;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            return Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options),
                    (int) (0.7 * Integer.valueOf(imageView.getTag().toString())),
                    (int) (0.7 * Integer.valueOf(imageView.getTag().toString())) * options.outHeight / options.outWidth, true);
        }

    }
}