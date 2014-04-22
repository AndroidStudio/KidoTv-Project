package mp.agencja.apsik.kidotv.main;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
    public final int width;
    private final Typeface typeface;
    private Handler handler = new Handler();
    private ImageView buyPremiumScene;

    public ViewPagerAdapter(final PlayListScene activity, List<List<HashMap<String, String>>> mainKidoList) {
        this.width = activity.getResources().getDrawable(R.drawable.video_thumb_unlocked).getIntrinsicWidth();
        this.buyPremiumScene = (ImageView) activity.findViewById(R.id.image_buy_premium_full_scene);
        this.buyPremiumScene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.buyClick();
            }
        });

        this.typeface = Typeface.createFromAsset(activity.getAssets(), "font.TTF");
        this.mainKidoList = mainKidoList;
        this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final int memClass = ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
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
        final View view = inflater.inflate(R.layout.viewpager_row, null);
        if (view != null) {
            final GridView gridView = (GridView) view.findViewById(R.id.gredView);
            final List<HashMap<String, String>> list = mainKidoList.get(position);
            final GridViewAdapter gridViewAdapter = new GridViewAdapter(list);
            gridView.setAdapter(gridViewAdapter);
            gridView.setOnItemClickListener(onGridItemClickListener);
            container.addView(view);
        }
        return view;
    }


    private final GridView.OnItemClickListener onGridItemClickListener = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            final Context context = view.getContext();
            if (context != null) {
                final String playListId = view.getTag().toString();
                if (playListId.equals("item_locked")) {
                    handler.postDelayed(runnable, 5000);
                    buyPremiumScene.setVisibility(View.VISIBLE);
                } else if (playListId.equals("null")) {
                    Intent intent = new Intent(context, FavoritePlayListScene.class);
                    intent.putExtra("container", String.valueOf(i + 1));
                    context.startActivity(intent);
                } else {
                    final Intent intent = new Intent(context, YouTubePlayerScene.class);
                    intent.putExtra("playListId", playListId);
                    context.startActivity(intent);
                }
            }
        }
    };

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            buyPremiumScene.setVisibility(View.INVISIBLE);
        }
    };

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
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            final View view;
            view = inflater.inflate(R.layout.gridview_item, viewGroup, false);
            if (view != null) {
                final ImageView imageView = (ImageView) view.findViewById(R.id.video);
                imageView.setTag(width);
                final HashMap<String, String> map = list.get(position);
                final TextView textViewTitle = (TextView) view.findViewById(R.id.title);
                textViewTitle.setMaxWidth(textViewTitle.getWidth());
                textViewTitle.setText(map.get("title"));
                textViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, width / 17);
                textViewTitle.setTypeface(typeface);
                if (map.get("is_locked").equals("true")) {
                    view.setTag("item_locked");
                    textViewTitle.setPadding(textViewTitle.getPaddingLeft(), textViewTitle.getPaddingTop(),
                            textViewTitle.getPaddingRight() * 6, textViewTitle.getPaddingBottom());
                    view.findViewById(R.id.price_image).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.monkey).setVisibility(View.VISIBLE);
                    textViewTitle.setBackgroundResource(R.drawable.title_thumb_premium);
                    imageView.setBackgroundResource(R.drawable.video_thumb_buy_premium);
                } else {
                    view.setTag(map.get("play_list_id"));
                    if (!map.get("play_list_id").equals("null")) {
                        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progerssBar);
                        progressBar.setVisibility(View.VISIBLE);
                        cache.loadBitmap(map.get("play_list_id"), imageView, GridViewAdapter.this, progressBar);
                    } else {
                        imageView.setBackgroundResource(R.drawable.video_thumb_unlocked);
                    }
                }
            }
            return view;
        }
    }

}
