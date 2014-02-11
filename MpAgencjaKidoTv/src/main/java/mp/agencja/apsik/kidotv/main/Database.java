package mp.agencja.apsik.kidotv.main;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database {
    private static final String DATABASENAME = "KIDOTV";
    private static final int DATABASEVERSION = 13;
    private final Context context;

    private SQLiteHelper sqLiteHelper;
    private SQLiteDatabase sqLiteDatabase;

    private static final String TABLE_CONTAINERS = "containers";
    private static final String KEY_CONTAINER_ID = "id";
    private static final String KEY_CONTAINER_PLAYLIST_TITLE = "title";
    private static final String KEY_CONTAINER_PLAYLIST_ID = "id_playlist";
    private static final String KEY_CONTAINER_IMAGE = "image";
    private static final String KEY_IS_CONTAINER_LOCKED = "is_locked";

    private static final String SCRIPT_CREATE_TABLE_CONTAINERS = "create table "
            + TABLE_CONTAINERS + " ("
            + KEY_CONTAINER_ID + " integer primary key, "
            + KEY_CONTAINER_PLAYLIST_TITLE + " string not null, "
            + KEY_CONTAINER_PLAYLIST_ID + " string, "
            + KEY_CONTAINER_IMAGE + " blob, "
            + KEY_IS_CONTAINER_LOCKED + " string not null);";

    private static final String TABLE_FAVORITES = "favorites_palylist";
    private static final String KEY_FAVORITES_ID = "id";
    private static final String KEY_FAVORITES_TITLE = "title";
    private static final String KEY_FAVORITES_ID_PLAYLIST = "id_playlist";
    private static final String KEY_IS_FAVORITE = "is_favorite";

    private static final String SCRIPT_CREATE_TABLE_FAVORITES = "create table "
            + TABLE_FAVORITES + " ("
            + KEY_FAVORITES_ID + " integer primary key autoincrement, "
            + KEY_FAVORITES_TITLE + " string not null, "
            + KEY_FAVORITES_ID_PLAYLIST + " string not null, "
            + KEY_IS_FAVORITE + " string not null);";

    public Database(Context context) {
        this.context = context;
    }

    public void openToWrite() {
        sqLiteHelper = new SQLiteHelper(context);
        sqLiteDatabase = sqLiteHelper.getWritableDatabase();
    }

    public void close() {
        sqLiteHelper.close();
    }

    public Cursor getAllPlayList() {
        return sqLiteDatabase.query(TABLE_FAVORITES, new String[]{KEY_FAVORITES_TITLE, KEY_FAVORITES_ID_PLAYLIST, KEY_IS_FAVORITE}, null, null, null, null, null);
    }

    public void addItem(String title, String play_list_id, String is_favorite) {
        final ContentValues values = new ContentValues();
        values.put(KEY_FAVORITES_TITLE, title);
        values.put(KEY_FAVORITES_ID_PLAYLIST, play_list_id);
        values.put(KEY_IS_FAVORITE, is_favorite);
        sqLiteDatabase.insert(TABLE_FAVORITES, null, values);
    }

    public void updateFavoriteItem(String play_list_id, String is_favorite) {
        final ContentValues values = new ContentValues();
        values.put(KEY_IS_FAVORITE, is_favorite);

        String whereClause = KEY_FAVORITES_ID_PLAYLIST + "=?";
        String[] whereArgs = new String[]{play_list_id};
        sqLiteDatabase.update(TABLE_FAVORITES, values, whereClause, whereArgs);
    }

    public Cursor getAllFavoritesItems() {
        String whereClause = KEY_IS_FAVORITE + "=?";
        return sqLiteDatabase.query(TABLE_FAVORITES, new String[]{KEY_IS_FAVORITE}, whereClause, new String[]{"true"}, null, null, null, null);
    }

    public Cursor getAllContainers() {
        return sqLiteDatabase.query(TABLE_CONTAINERS, new String[]{KEY_CONTAINER_PLAYLIST_TITLE, KEY_CONTAINER_PLAYLIST_ID,
                KEY_IS_CONTAINER_LOCKED, KEY_CONTAINER_ID}, null, null, null, null, null);
    }

    public void insertContainer(String title, String is_locked) {
        final ContentValues values = new ContentValues();
        values.put(KEY_CONTAINER_PLAYLIST_TITLE, title);
        values.put(KEY_CONTAINER_PLAYLIST_ID, "");
        values.put(KEY_IS_CONTAINER_LOCKED, is_locked);
        sqLiteDatabase.insert(TABLE_CONTAINERS, null, values);
    }

    public void updateContainer(String id, String title, String play_list_id) {
        final ContentValues values = new ContentValues();
        values.put(KEY_CONTAINER_PLAYLIST_TITLE, title);
        values.put(KEY_CONTAINER_PLAYLIST_ID, play_list_id);

        String whereClause = KEY_CONTAINER_ID + "=?";
        String[] whereArgs = new String[]{id};
        sqLiteDatabase.update(TABLE_CONTAINERS, values, whereClause, whereArgs);
    }

    public void updateContainerByPlayListId(String playListId) {
        final ContentValues values = new ContentValues();
        values.put(KEY_CONTAINER_PLAYLIST_TITLE, "Tap to add playlist");
        values.put(KEY_CONTAINER_PLAYLIST_ID, "");

        String whereClause = KEY_CONTAINER_PLAYLIST_ID + "=?";
        String[] whereArgs = new String[]{playListId};
        sqLiteDatabase.update(TABLE_CONTAINERS, values, whereClause, whereArgs);
    }

    public void updateContainerByNullPlayList(String title, String playListId) {
        Cursor cursor = getAllContainers();
        while (cursor.moveToNext()) {
            if (cursor.getString(1).equals("")) {
                String id = cursor.getString(3);
                updateContainer(id, title, playListId);
                return;
            }
        }
    }

    private class SQLiteHelper extends SQLiteOpenHelper {
        public SQLiteHelper(Context context) {
            super(context, Database.DATABASENAME, null, Database.DATABASEVERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SCRIPT_CREATE_TABLE_FAVORITES);
            db.execSQL(SCRIPT_CREATE_TABLE_CONTAINERS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i2) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTAINERS);
            onCreate(db);
        }
    }
}
