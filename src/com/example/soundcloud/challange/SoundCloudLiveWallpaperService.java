package com.example.soundcloud.challange;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.example.soundcloud.challange.api.SoundCloudApi;
import com.example.soundcloud.challange.data.Tracks;

public class SoundCloudLiveWallpaperService extends WallpaperService implements
		LoaderManager.LoaderCallbacks<List<Tracks>> {

	final String LOG_TAG = "SoundCloudLiveWallpaperActivity";

	public static final String SHARED_PREFS_NAME = "soundcloudLiveWallSettings";

	/**
	 * a Handler to fetch the png from the url
	 */
	private Handler mHandler = new Handler();

	private AndroidHttpClient ahc;

	/**
	 * a good idea to persist the png in the local filesystem
	 */
	// private Input mImagePath = openFileInput(name);

	/**
	 * thread fetching the images from the tracks waveform Url
	 */
	private Runnable mFetchImage = new Runnable() {

		@Override
		public void run() {
			/*
			 * do the url AndroidHttpConnection here
			 */
			try {

			} catch (Exception e) {
				Log.i(LOG_TAG, e.getMessage());
			}

		}
	};

	/**
	 * offset for time bound reload / randomized displayed waveform png
	 */
	static long timeOffset = System.currentTimeMillis();

	
	@Override
	public Engine onCreateEngine() {

		return new SoundCloudWallpaperEngine();
	}

	class SoundCloudWallpaperEngine extends Engine implements
			SharedPreferences.OnSharedPreferenceChangeListener {

		private SharedPreferences mPrefs;

		private boolean mVisible;

		private WaveFormDrawManager mDrawManager = new WaveFormDrawManager();
		
		void drawWavePic() {
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
				mHandler.postDelayed(mDrawWaveFrameRunnable, 1000 / 25);
			}
		}

		private void drawFrame(final Canvas c) {
			mDrawManager.onDraw(c);
		}

		private final Runnable mDrawWaveFrameRunnable = new Runnable() {
			public void run() {
				drawWavePic();
			}
		};

		

		@Override
		public int getDesiredMinimumHeight() {
			// TODO Auto-generated method stub
			return super.getDesiredMinimumHeight();
		}

		@Override
		public int getDesiredMinimumWidth() {
			// TODO Auto-generated method stub
			return super.getDesiredMinimumWidth();
		}

		@Override
		public SurfaceHolder getSurfaceHolder() {
			// TODO Auto-generated method stub
			return super.getSurfaceHolder();
		}

		@Override
		public boolean isPreview() {
			// TODO Auto-generated method stub
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
			mDrawManager.onCreate(getApplicationContext());
			super.onCreate(surfaceHolder);
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
			mHandler.removeCallbacks(mDrawWaveFrameRunnable);
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset,
				float xOffsetStep, float yOffsetStep, int xPixelOffset,
				int yPixelOffset) {
			// TODO Auto-generated method stub
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep,
					xPixelOffset, yPixelOffset);
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
			mVisible = visible;
			if (mVisible) {
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

			/**
			 * which service to fetch user tracks or user favorites
			 */
			String shape = sharedPreferences.getString("soundcloudPreferences",
					"service");

			// read the 3D model from the resource
			// readPreferences();
		}

	}

	/**
	 * get the track information here
	 * 
	 * @author marcus
	 * 
	 */
	static class TrackListLoader extends AsyncTaskLoader<List<Tracks>> {

		final String LOG_TAG = "TrackListLoader";

		public TrackListLoader(final Context context) {
			super(context);
			Log.i("TracklistLoader", "Constructore invoked");

		}

		@Override
		public List<Tracks> loadInBackground() {
			Log.i("TracklistLoader", "load in Backround ");
			List<Tracks> tracks = new ArrayList<Tracks>();

			try {
				Log.i(LOG_TAG,
						"load in Backround "
								+ SoundCloudApi.getMyWaveformUrl("tracks"));

				return SoundCloudApi.getMyTracksFromSoundCloud();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.i(LOG_TAG, " Buuuummmmm ");
			return tracks;
		}
	}

	/**
	 * 
	 */
	@Override
	public Loader<List<Tracks>> onCreateLoader(int arg0, Bundle arg1) {
		Log.i(LOG_TAG, "onCreateLoader ");

		return new TrackListLoader(this.getApplicationContext());
	}

	@Override
	public void onLoadFinished(Loader<List<Tracks>> arg0, List<Tracks> arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoaderReset(Loader<List<Tracks>> arg0) {
		// TODO Auto-generated method stub

	}
}
