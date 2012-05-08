package com.example.soundcloud.challange;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.example.soundcloud.challange.api.SoundCloudApi;
import com.example.soundcloud.challange.data.Tracks;
import com.soundcloud.api.ApiWrapper;

/**
 * 
 * @author marcus
 * 
 */
public class SoundCloudLiveWallpaperService extends WallpaperService {

	final String LOG_TAG = "SoundCloudLiveWallpaperActivity";

	private Handler mHandler = new Handler();

	private boolean isDestroyed = false;

	private AndroidHttpClient ahc;

	/**
	 * a good idea to persist the png in the local filesystem
	 */
	// private Input mImagePath = openFileInput(name);

	/**
	 * offset for time bound reload / randomized displayed waveform png
	 */
	static long timeOffset = System.currentTimeMillis();

	@Override
	public Engine onCreateEngine() {
		try {
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new SoundCloudWallpaperEngine();
	}

	class SoundCloudWallpaperEngine extends Engine implements
			SharedPreferences.OnSharedPreferenceChangeListener {

		private SharedPreferences mPrefs = PreferenceManager
				.getDefaultSharedPreferences(SoundCloudLiveWallpaperService.this);

		private String soundCloudUser = mPrefs.getString("login", "mschlech");

		private String soundCloudPassword = mPrefs.getString("password",
				"linus123");

		private String soundCloudSource = mPrefs.getString("source", "tracks");

		private TrackListLoader mTrackListLoader;

		private boolean mVisible;

		private WaveFormDrawManager mDrawManager = new WaveFormDrawManager();

		private List<Tracks> tracks = null;

		SoundCloudApi mApiWrapper;

		private int run = 0;

		SoundCloudWallpaperEngine() {
			mApiWrapper = new SoundCloudApi(soundCloudUser, soundCloudPassword);
			mPrefs.registerOnSharedPreferenceChangeListener(this);
			onSharedPreferenceChanged(mPrefs, null);
		}

		void drawWavePic() {
			Log.i(LOG_TAG, " drawPic invoked");
			final SurfaceHolder holder = getSurfaceHolder();

			Canvas c = null;
			try {
				c = holder.lockCanvas();
				if (c != null) {
					// draw something
					drawFrame(c);
				}
			} finally {
				if (c != null)
					holder.unlockCanvasAndPost(c);
			}

			// Reschedule the next redraw
			mHandler.removeCallbacks(mDrawWaveFrameRunnable);
			if (mVisible) {
				//Log.i(LOG_TAG, "mVisible = " + mVisible);
				mHandler.postDelayed(mDrawWaveFrameRunnable, 1000 / 25);
			}
		}

		private void drawFrame(final Canvas c) {
			//Log.i(LOG_TAG, " drawFrame invoked" + run);
			// content
			mDrawManager.onDraw(c, tracks.get(run).genre,
					tracks.get(run).trackName, tracks.get(run).permalink_url);
		}

		private final Runnable mDrawWaveFrameRunnable = new Runnable() {
			public void run() {
				//Log.i(LOG_TAG, " mDrawWaveFrameRunnable thread invoked");

				drawWavePic();
			}
		};

		@Override
		public int getDesiredMinimumHeight() {
			return super.getDesiredMinimumHeight();

		}

		@Override
		public int getDesiredMinimumWidth() {
			return super.getDesiredMinimumWidth();
		}

		@Override
		public SurfaceHolder getSurfaceHolder() {
			return super.getSurfaceHolder();
		}

		@Override
		public boolean isPreview() {
			return super.isPreview();
		}

		@Override
		public boolean isVisible() {
			// TODO Auto-generated method stub
			return super.isVisible();
		}

		@Override
		public Bundle onCommand(String action, int x, int y, int z,
				Bundle extras, boolean resultRequested) {
			/**
			 * open the browser
			 */
			return super.onCommand(action, x, y, z, extras, resultRequested);
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			// Log.i(LOG_TAG, "onCreate in Engine invoked run " + run);
			setTouchEventsEnabled(true);

			try {
				Log.i(LOG_TAG, "get tracks object");
				tracks = new TrackListLoader(mDrawManager,
						mApiWrapper.getApiWrapper()).doInBackground();
				// Log.i(LOG_TAG, "tracks in onCreate with the SurfaceHolder"
				// + tracks.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (tracks != null) {
				// Log.i(LOG_TAG, "in onCreate tracks to pass = " + tracks);
				mDrawManager.onCreate(getApplicationContext(),
						tracks.get(run).waveformUrl);
				run = run <= tracks.size() ? run = +1 : 0;
				// Log.i(LOG_TAG, "next run " + run);
			} else {
				// Log.i(LOG_TAG, "no text from network");

				mDrawManager.onCreate(getApplicationContext(), "defaultImage");
			}
		}

		@Override
		public void onDesiredSizeChanged(int desiredWidth, int desiredHeight) {
			// TODO Auto-generated method stub
			super.onDesiredSizeChanged(desiredWidth, desiredHeight);
		}

		/**
		 * set bitmap free release the bitmap resources
		 */
		@Override
		public void onDestroy() {
			// TODO Auto-generated method stub
			super.onDestroy();
			mDrawManager.onDestroy();
			if (mTrackListLoader != null) {
				mTrackListLoader.cancel(true);
			}
			isDestroyed = true;
			mHandler.removeCallbacks(mDrawWaveFrameRunnable);
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset,
				float xOffsetStep, float yOffsetStep, int xPixelOffset,
				int yPixelOffset) {
			// TODO Auto-generated method stub
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep,
					xPixelOffset, yPixelOffset);
			// Log.i(LOG_TAG, "onOffsetsChanged and drawWavePic to be invoked");

			drawWavePic();
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);
			mDrawManager.onSizedChanged(width, height);
			drawWavePic();
		}

		@Override
		public void onSurfaceCreated(SurfaceHolder holder) {
			/**
			 * 
			 */
			holder.setFormat(PixelFormat.RGBA_8888);
			// TODO Auto-generated method stub
			super.onSurfaceCreated(holder);
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			super.onSurfaceDestroyed(holder);
			mHandler.removeCallbacks(mDrawWaveFrameRunnable);
		}

		@Override
		public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			super.onSurfaceRedrawNeeded(holder);
		}

		@Override
		public void onTouchEvent(MotionEvent event) {

			super.onTouchEvent(event);
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			// TODO Auto-generated method stub
			super.onVisibilityChanged(visible);
			Log.i(LOG_TAG, "onVisibilityChanged" + visible);

			mVisible = visible;
			if (mVisible) {
				Log.i(LOG_TAG,
						"onVisibilityChanged is true so invoke the drawWavePic method");

				drawWavePic();
			} else {
				mHandler.removeCallbacks(mDrawWaveFrameRunnable);
			}
		}

		@Override
		public void setTouchEventsEnabled(boolean enabled) {
			// TODO Auto-generated method stub
			super.setTouchEventsEnabled(enabled);
		}

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {

			Log.i(LOG_TAG, " Shared Preferences changed ");
			/**
			 * which service to fetch user tracks or user favorites
			 */
			soundCloudUser = sharedPreferences
					.getString("login", "mschlech123");
			soundCloudPassword = sharedPreferences.getString("password",
					"linus123");
			soundCloudSource = sharedPreferences.getString("source", "tracks");

			run = 0;
		}

	}

	/**
	 * get the track information here
	 * 
	 * @author marcus
	 * 
	 */
	static class TrackListLoader extends AsyncTask<Void, Void, List<Tracks>> {

		private WaveFormDrawManager mDrawManager;
		private ApiWrapper mApiWrapper;

		public TrackListLoader(WaveFormDrawManager drawManager,
				ApiWrapper apiWrapper) {
			mDrawManager = drawManager;
			mApiWrapper = apiWrapper;
		}

		final static String LOG_TAG = "TrackListLoader";

		@Override
		protected List<Tracks> doInBackground(Void... params) {
			try {
				Log.i(LOG_TAG, "loading the track information");
				return SoundCloudApi.getMyWaveformUrl("tracks", mApiWrapper);
			} catch (Exception e) {
				Log.e(LOG_TAG, e.getMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<Tracks> result) {
			Log.i(LOG_TAG, "done and post execute");

			new GetBitmap(result.get(0).waveformUrl, mDrawManager).execute();
			super.onPostExecute(result);

		}
	}

	private static class GetBitmap extends AsyncTask<Void, Void, Bitmap> {

		final static String LOG_TAG = "GetBitMap";
		private URL mUrl;
		private WaveFormDrawManager mDrawManager;

		public GetBitmap(final String uri, WaveFormDrawManager drawManager) {
			mDrawManager = drawManager;
			try {
				Log.i(LOG_TAG, "constructor of GetBitmap invoked ");

				mUrl = new URL(uri);

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			Bitmap result = null;
			if (mUrl != null) {
				HttpURLConnection connection;
				try {
					Log.i(LOG_TAG, " Bitmap fetch ");
					connection = (HttpURLConnection) mUrl.openConnection();
					connection.setDoInput(true);
					connection.connect();
					InputStream input = connection.getInputStream();
					result = BitmapFactory.decodeStream(input);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			Log.i(LOG_TAG, "result");

			return result;

		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			Log.i(LOG_TAG, "done and post execute");

			mDrawManager.setBitmap(result);

		}
	}
}
