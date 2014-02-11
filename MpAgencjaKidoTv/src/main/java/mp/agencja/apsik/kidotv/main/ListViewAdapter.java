package mp.agencja.apsik.kidotv.main;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mp.agencja.apsik.kidotv.R;

class ListViewAdapter extends BaseAdapter {
    private List<HashMap<String, String>> list;
    private final LayoutInflater inflater;

    public ListViewAdapter(Context context) {
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
                HashMap<String, String> map = new HashMap<String, String>(3);
                map.put("title", title);
                map.put("play_list_id", play_list_id);
                map.put("is_favorite", is_favorite);
                list.add(map);
            }
        } else {
            list = DefaultPlayLists.getDefaultPlayLists();
            for (HashMap<String, String> map : list) {
                PlayListScene.database.addItem(map.get("title"), map.get("play_list_id"), map.get("is_favorite"));
            }
        }
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
    public View getView(final int i, View v, ViewGroup viewGroup) {
        final HashMap<String, String> map = list.get(i);
        View view = inflater.inflate(R.layout.list_row, null);
        assert view != null;
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setMaxWidth(title.getWidth());
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBoxFavorites);
        if (map.get("is_favorite").equals("true")) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }
        title.setText(map.get("title"));
        view.setTag(map.get("play_list_id"));
        return view;
    }
}