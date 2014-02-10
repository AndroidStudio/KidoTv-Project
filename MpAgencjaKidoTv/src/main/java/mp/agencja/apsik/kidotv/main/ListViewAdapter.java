package mp.agencja.apsik.kidotv.main;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mp.agencja.apsik.kidotv.R;

class ListViewAdapter extends BaseAdapter {
    private ArrayList<HashMap<String, String>> list;
    private final LayoutInflater inflater;
    private List<Boolean> checkedBooleanList;
    public final Database database;

    public ListViewAdapter(Context context, ArrayList<HashMap<String, String>> mainList) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        database = new Database(context);
        database.openToWrite();
        createItems(mainList);
    }

    public void createItems(ArrayList<HashMap<String, String>> mainList) {
        list = new ArrayList<HashMap<String, String>>(mainList.size());
        checkedBooleanList = new ArrayList<Boolean>(mainList.size());
        for (HashMap<String, String> map : mainList) {
            checkedBooleanList.add(false);
            list.add(map);
        }
        final Cursor cursor = database.getAllFavoritesPlayList();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                final String play_list_id = cursor.getString(0);
                for (int i = 0; i < this.list.size(); i++) {
                    HashMap<String, String> map = this.list.get(i);
                    if (map.get("play_list_id").equals(play_list_id)) {
                        checkedBooleanList.set(i, true);
                        break;
                    }
                }
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
        checkBox.setChecked(checkedBooleanList.get(i));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkedBooleanList.set(i, b);
                if (b) {
                    database.addFavoriteItem(map.get("play_list_id"));
                } else {
                    database.deleteFavoriteItem(map.get("play_list_id"));
                }
                notifyDataSetChanged();
            }
        });

        title.setText(map.get("title"));
        view.setTag(map.get("play_list_id"));
        return view;
    }

    public void showFavorites() {
        for (int i = 0; i < checkedBooleanList.size(); i++) {
            if (!checkedBooleanList.get(i)) {
                checkedBooleanList.remove(i);
                list.remove(i);
                showFavorites();
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void search(String s) {
        for (int i = 0; i < list.size(); i++) {
            String title = list.get(i).get("title").toLowerCase();
            if ((!title.contains(s.toLowerCase()))) {
                checkedBooleanList.remove(i);
                list.remove(i);
                search(s);
                break;
            }
        }
        notifyDataSetChanged();
    }
}