package mp.agencja.apsik.kidotv.main;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import mp.agencja.apsik.kidotv.R;

class DownloadPlayListTask extends AsyncTask<String, Integer, Boolean> {
    private final static String TAG_LOG = "DownloadPlayListTask";
    private final HttpClient httpClient = CustomHttpClient.getHttpClient();
    private SharedPreferences sharedPreferences;
    private final Context context;
    private NotificationManager notificationManager;
    private ArrayList<HashMap<String, String>> list;
    private int version;

    public DownloadPlayListTask(SharedPreferences sharedPreferences, Context context) {
        this.sharedPreferences = sharedPreferences;
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.list = new ArrayList<HashMap<String, String>>();
        this.version = 0;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(String... urls) {
        InputStream inputStream;
        try {
            final HttpGet request = new HttpGet(urls[0]);
            final HttpResponse response = httpClient.execute(request);
            final StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                final HttpEntity httpEntity = response.getEntity();
                inputStream = httpEntity.getContent();
                try {
                    final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    final StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    final JSONObject jsonObject = new JSONObject(sb.toString());

                    version = jsonObject.getInt("Version");
                    if (version == sharedPreferences.getInt("version", 0))
                        return false;
                    final JSONArray jsonArray = jsonObject.getJSONArray("KidoTvPlayList");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        final JSONObject jsonRow = jsonArray.getJSONObject(i);
                        HashMap<String, String> map = new HashMap<String, String>();
                        String play_list_id = jsonRow.getString("PlayList");
                        String title = jsonRow.getString("Title");
                        String duration = jsonRow.getString("Duration");

                        map.put("title", title);
                        map.put("play_list_id", play_list_id);
                        map.put("is_favorite", "false");
                        map.put("duration", duration);
                        list.add(map);
                    }
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    inputStream.close();
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result) {
            if (sharedPreferences.getString("Notified", "").equals("false")) {
                final Intent notificationIntent = new Intent(context, PlayListScene.class);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                notificationIntent.putExtra("list", list);
                notificationIntent.putExtra("version", version);
                final PendingIntent contentIntent = PendingIntent.getActivity(context, 10, notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                final NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
                                .setContentIntent(contentIntent)
                                .setContentTitle("Kido TV")
                                .setContentText("Click to update play list")
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                                .setAutoCancel(true);

                notificationManager.notify(1, notification.build());
                final SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("Notified", "true");
                editor.commit();
            }
        }
    }
}