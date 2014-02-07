package mp.agencja.apsik.kidotv.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Debug;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.io.IOException;
import java.io.InputStream;

import mp.agencja.apsik.kidotv.R;

public class ViewPagerParallax extends ViewPager {
    private int background_id = -1;
    private int background_saved_id = -1;
    private int saved_width = -1;
    private int saved_height = -1;
    private int saved_max_num_pages = -1;
    private Bitmap saved_bitmap;
    private boolean insufficientMemory = false;

    private int max_num_pages = 0;
    private int imageHeight;
    private float zoom_level;
    private float overlap_level;
    private final Rect src = new Rect();
    private final Rect dst = new Rect();

    public ViewPagerParallax(Context context) {
        super(context);
    }

    public ViewPagerParallax(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void set_new_background() {
        if (background_id == -1)
            return;

        if (max_num_pages == 0)
            return;

        if (getWidth() == 0 || getHeight() == 0)
            return;

        if ((saved_height == getHeight()) && (saved_width == getWidth()) &&
                (background_saved_id == background_id) &&
                (saved_max_num_pages == max_num_pages))
            return;

        InputStream is;

        try {
            Context context = getContext();
            if (context != null) {
                is = context.getResources().openRawResource(background_id);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                BitmapFactory.decodeStream(is, null, options);

                imageHeight = options.outHeight;
                int imageWidth = options.outWidth;
                zoom_level = ((float) imageHeight) / getHeight();

                options.inJustDecodeBounds = false;
                options.inSampleSize = Math.round(zoom_level);

                if (options.inSampleSize > 1) {
                    imageHeight = imageHeight / options.inSampleSize;
                    imageWidth = imageWidth / options.inSampleSize;
                }
                double max = Runtime.getRuntime().maxMemory();
                double heapSize = Runtime.getRuntime().totalMemory();
                double heapRemaining = Runtime.getRuntime().freeMemory();
                double nativeUsage = Debug.getNativeHeapAllocatedSize();
                double remaining = max - (heapSize - heapRemaining) - nativeUsage;

                int freeMemory = (int) (remaining / 1024);
                int bitmap_size = imageHeight * imageWidth * 4 / 1024;
                if (bitmap_size > freeMemory / 2) {
                    insufficientMemory = true;
                    return;
                }

                zoom_level = ((float) imageHeight) / getHeight();
                overlap_level = zoom_level * Math.min(Math.max(imageWidth / zoom_level - getWidth(), 0) / (max_num_pages - 1), getWidth() / 2);

                is.reset();
                saved_bitmap = BitmapFactory.decodeStream(is, null, options);
                is.close();
            }
        } catch (IOException e) {
            background_id = -1;
            return;
        }

        saved_height = getHeight();
        saved_width = getWidth();
        background_saved_id = background_id;
        saved_max_num_pages = max_num_pages;
    }

    private int current_position = -1;
    private float current_offset = 0.0f;

    @Override
    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);
        current_position = position;
        current_offset = offset;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!insufficientMemory) {
            if (current_position == -1)
                current_position = getCurrentItem();
            src.set((int) (overlap_level * (current_position + current_offset)), 0,
                    (int) (overlap_level * (current_position + current_offset) + (getWidth() * zoom_level)), imageHeight);
            dst.set(getScrollX(), 0,
                    getScrollX() + canvas.getWidth(), canvas.getHeight());
            canvas.drawBitmap(saved_bitmap, src, dst, null);
        }
    }

    public void setMaxPages(int pages) {
        max_num_pages = pages;
        set_new_background();
    }

    public int getMaxPages() {
        return max_num_pages;
    }

    public void setBackgroundAsset() {
        background_id = R.drawable.bg_playlist_activity;
        set_new_background();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!insufficientMemory)
            set_new_background();
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item);
        current_position = item;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return !isFakeDragging() && super.onInterceptTouchEvent(event);
    }

    protected void onDetachedFromWindow() {
        if (saved_bitmap != null) {
            saved_bitmap.recycle();
            saved_bitmap = null;
        }
        super.onDetachedFromWindow();
    }
}
