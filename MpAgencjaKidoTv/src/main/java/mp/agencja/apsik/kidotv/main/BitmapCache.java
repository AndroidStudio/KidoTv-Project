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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;

public class BitmapCache {

    private static final String LOG_TAG = "Cache";
    private final LruCache<String, Bitmap> mBitmapCache;
    private final ArrayList<String> mCurrentTasks;
    private ImageView imageView;


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
        Log.d(LOG_TAG, "MaxSize: " + mBitmapCache.maxSize() + " bytes");
        Log.d(LOG_TAG, "Size:    " + mBitmapCache.size() + " bytes");
        return mBitmapCache.get(key);
    }

    public void loadBitmap(String imagesId, ImageView imageView, ProgressBar progressBar, ViewPagerAdapter.GridViewAdapter gridViewAdapter) {
        this.imageView = imageView;
        final Bitmap bitmap = getBitmapFromCache(imagesId);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            progressBar.setVisibility(View.GONE);
        } else {
            if (!mCurrentTasks.contains(imagesId)) {
                final DecodeImageTask decodeImageTask = new DecodeImageTask(imagesId, gridViewAdapter);
                decodeImageTask.execute();
            }
        }
    }

    public class DecodeImageTask extends AsyncTask<Integer, Integer, Bitmap> {
        private final String url;
        private final ViewPagerAdapter.GridViewAdapter gridViewAdapter;

        public DecodeImageTask(String url, ViewPagerAdapter.GridViewAdapter gridViewAdapter) {
            this.url = url;
            this.gridViewAdapter = gridViewAdapter;
        }

        @Override
        protected void onPreExecute() {
            mCurrentTasks.add(url);
        }

        @Override
        protected Bitmap doInBackground(Integer... id) {
            final HttpClient client = AndroidHttpClient.newInstance("Android");
            final HttpGet request = new HttpGet(url);
            Bitmap bitmap = null;

            try {
                HttpResponse response = client.execute(request);
                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    Log.w(LOG_TAG, "Error while retrieving bitmap from " + url);
                    return null;
                }
                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    try {
                        byte[] bytes = EntityUtils.toByteArray(entity);
                        if (bytes != null) {
                            bitmap = decodeBitmap(bytes);
                            addBitmapToCache(url, bitmap);
                            return bitmap;
                        }
                        return null;
                    } finally {
                        entity.consumeContent();
                    }
                }
            } catch (Exception e) {
                request.abort();
                Log.w(LOG_TAG, "Error while retrieving bitmap from " + url, e);
            } finally {
                ((AndroidHttpClient) client).close();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mCurrentTasks.remove(url);
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
                    (int) (0.7 * imageView.getWidth()), (int) (0.7 * imageView.getWidth()) * options.outHeight / options.outWidth, true);
        }

    }
}