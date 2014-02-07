package mp.agencja.apsik.kidotv.main;

import android.app.ActivityManager;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import mp.agencja.apsik.kidotv.R;

class ViewPagerAdapter extends PagerAdapter {
    private final LayoutInflater inflater;
    private final List<List<HashMap<String, String>>> mainKidoList;
    private BitmapCache cache;

    public ViewPagerAdapter(Context context, List<List<HashMap<String, String>>> mainKidoList) {
        this.mainKidoList = mainKidoList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        final int cacheSize = 1024 * 1024 * memClass;

        final RetainCache c = RetainCache.getOrCreateRetainableCache();
        this.cache = c.mRetainedCache;
        if (this.cache == null) {
            this.cache = new BitmapCache(cacheSize);
            c.mRetainedCache = this.cache;
        }

    }

    @Override
    public int getCount() {
        return mainKidoList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);

    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = inflater.inflate(R.layout.viewpager_row, null);
        if (view != null) {
            GridView gridView = (GridView) view.findViewById(R.id.gredView);
            List<HashMap<String, String>> list = mainKidoList.get(position);
            GridViewAdapter gridViewAdapter = new GridViewAdapter(list);
            gridView.setAdapter(gridViewAdapter);

            container.addView(view);
        }
        return view;
    }

    public class GridViewAdapter extends BaseAdapter {
        private final List<HashMap<String, String>> list;


        public GridViewAdapter(List<HashMap<String, String>> list) {
            this.list = list;

        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            View view = inflater.inflate(R.layout.gridview_item, viewGroup, false);
            if (view != null) {
                TextView tvTitle = (TextView) view.findViewById(R.id.title);
                tvTitle.setMaxWidth(tvTitle.getWidth());
                ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
                ImageView imageView = (ImageView) view.findViewById(R.id.video);
                HashMap<String, String> map = list.get(position);
                cache.loadBitmap(map.get("url"), imageView, progressBar, GridViewAdapter.this);
                tvTitle.setText(map.get("title"));
            }
            return view;
        }
    }

}
