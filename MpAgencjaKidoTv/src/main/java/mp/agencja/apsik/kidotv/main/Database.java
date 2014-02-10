package mp.agencja.apsik.kidotv.main;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database {
    private static final String DATABASENAME = "KIDOTV";
    private static final int DATABASEVERSION = 3;
    private final Context context;

    private SQLiteHelper sqLiteHelper;
    private SQLiteDatabase sqLiteDatabase;

    private static final String TABLE_FAVORITES = "favorites_palylist";
    private static final String KEY_FAVORITES_ID = "id";
    private static final String KEY_FAVORITES_ID_PLAYLIST = "id_playlist";

    private static final String SCRIPT_CREATE_TABLE_FAVORITES = "create table "
            + TABLE_FAVORITES + " ("
            + KEY_FAVORITES_ID + " integer primary key autoincrement, "
            + KEY_FAVORITES_ID_PLAYLIST + " string not null);";

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

    public Cursor getAllFavoritesPlayList() {
        return sqLiteDatabase.query(TABLE_FAVORITES, new String[]{KEY_FAVORITES_ID_PLAYLIST}, null, null, null, null, null);
    }

    public void addFavoriteItem(String play_list_id) {
        final ContentValues values = new ContentValues();
        values.put(KEY_FAVORITES_ID_PLAYLIST, play_list_id);
        sqLiteDatabase.insert(TABLE_FAVORITES, null, values);
    }

    public void deleteFavoriteItem(String play_list_id) {
        String whereClause = KEY_FAVORITES_ID_PLAYLIST + "=?";
        String[] whereArgs = new String[]{play_list_id};
        sqLiteDatabase.delete(TABLE_FAVORITES, whereClause, whereArgs);
    }

    private class SQLiteHelper extends SQLiteOpenHelper {
        public SQLiteHelper(Context context) {
            super(context, Database.DATABASENAME, null, Database.DATABASEVERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SCRIPT_CREATE_TABLE_FAVORITES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i2) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
            onCreate(db);
        }
    }
}
