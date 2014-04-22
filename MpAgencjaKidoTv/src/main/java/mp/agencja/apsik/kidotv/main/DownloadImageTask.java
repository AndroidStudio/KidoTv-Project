package mp.agencja.apsik.kidotv.main;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	private final ImageView bmImage;

	public DownloadImageTask(ImageView bmImage) {
		this.bmImage = bmImage;
	}

	protected Bitmap doInBackground(String... urls) {
		final String urldisplay = urls[0];
		Bitmap bitmap = null;
		try {
			InputStream in = new URL(urldisplay).openStream();
			Options options = new Options();
			options.inPreferredConfig = Config.RGB_565;
			bitmap = BitmapFactory.decodeStream(in, null, options);
		} catch (Exception e) {
			Log.e("DownloadImageTask", "Error");
		}
		return bitmap;
	}

	protected void onPostExecute(Bitmap bitmap) {
		if (bitmap != null) {
			bmImage.setImageBitmap(bitmap);
		}
	}
}