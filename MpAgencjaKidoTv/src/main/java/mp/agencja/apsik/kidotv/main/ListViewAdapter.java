package mp.agencja.apsik.kidotv.main;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mp.agencja.apsik.kidotv.R;

class ListViewAdapter extends BaseAdapter {

    private final Context context;
    private List<HashMap<String, String>> list;
    private final LayoutInflater inflater;
    private Typeface typeface;

    public ListViewAdapter(Context context) {
        this.context = context;
        typeface = Typeface.createFromAsset(context.getAssets(), "font.TTF");
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        getItems();
    }

    public void getItems() {
        list = new ArrayList<HashMap<String, String>>();
        final Cursor cursor = PlayListScene.database.getAllPlayList();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                final String title = cursor.getString(0);
                final String play_list_id = cursor.getString(1);
                final String is_favorite = cursor.getString(2);
                final String duration = cursor.getString(3);
                HashMap<String, String> map = new HashMap<String, String>(3);
                map.put("title", title);
                map.put("play_list_id", play_list_id);
                map.put("is_favorite", is_favorite);
                map.put("duration", duration);
                list.add(map);
            }
        } else {
            final XmlPullParser parser = context.getResources().getXml(R.xml.play_list);
            try {
                assert parser != null;
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    String name = parser.getName();
                    String title = null;
                    String play_list_id = null;
                    String duration = null;
                    if ((name != null) && name.equals("play_list")) {
                        int size = parser.getAttributeCount();
                        for (int i = 0; i < size; i++) {
                            String attrName = parser.getAttributeName(i);
                            String attrValue = parser.getAttributeValue(i);
                            if ((attrName != null) && attrName.equals("title")) {
                                title = attrValue;
                            } else if ((attrName != null) && attrName.equals("play_list_id")) {
                                play_list_id = attrValue;
                            } else if ((attrName != null) && attrName.equals("duration")) {
                                duration = attrValue;
                            }
                        }
                        if (title != null && play_list_id != null && duration != null) {
                            PlayListScene.database.addItem(title, play_list_id, "false", duration);
                            HashMap<String, String> map = new HashMap<String, String>(3);
                            map.put("title", title);
                            map.put("play_list_id", play_list_id);
                            map.put("is_favorite", "fasle");
                            map.put("duration", duration);
                            list.add(map);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            String locale = context.getResources().getConfiguration().locale.getCountry();
//            Log.w("country", "" + locale);

        }
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
    public View getView(final int i, View v, ViewGroup viewGroup) {
        final HashMap<String, String> map = list.get(i);
        View view = inflater.inflate(R.layout.list_row, null);
        assert view != null;
        TextView duration = (TextView) view.findViewById(R.id.duration);
        duration.setMaxWidth(duration.getWidth());
        duration.setTypeface(typeface);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setMaxWidth(title.getWidth());
        title.setTypeface(typeface);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBoxFavorites);
        if (map.get("is_favorite").equals("true")) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }
        title.setText(map.get("title"));
        duration.setText(map.get("duration"));
        view.setTag(map.get("play_list_id"));
        return view;
    }

    public void getAllFavorites() {
        list = new ArrayList<HashMap<String, String>>();
        final Cursor cursor = PlayListScene.database.getAllFavoritesItems();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                final String title = cursor.getString(0);
                final String play_list_id = cursor.getString(1);
                final String is_favorite = cursor.getString(2);
                final String duration = cursor.getString(3);
                HashMap<String, String> map = new HashMap<String, String>(3);
                map.put("title", title);
                map.put("play_list_id", play_list_id);
                map.put("is_favorite", is_favorite);
                map.put("duration", duration);
                list.add(map);
            }
        }
    }

    public void searchItems(String s) {
        list = new ArrayList<HashMap<String, String>>();
        final Cursor cursor = PlayListScene.database.getItemByTitle(s);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                final String title = cursor.getString(0);
                Log.w(title, title);
                final String play_list_id = cursor.getString(1);
                final String is_favorite = cursor.getString(2);
                final String duration = cursor.getString(3);
                HashMap<String, String> map = new HashMap<String, String>(3);
                map.put("title", title);
                map.put("play_list_id", play_list_id);
                map.put("is_favorite", is_favorite);
                map.put("duration", duration);
                list.add(map);
            }
            notifyDataSetChanged();
        }
    }
}