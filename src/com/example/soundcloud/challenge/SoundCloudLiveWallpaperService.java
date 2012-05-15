package com.example.soundcloud.challenge;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.example.soundcloud.challenge.api.SoundCloudApi;
import com.example.soundcloud.challenge.data.Tracks;

/**
 * 
 * @author marcus
 * 
 */
public class SoundCloudLiveWallpaperService extends WallpaperService {

	final String LOG_TAG = "SoundCloudLiveWallpaperActivity";
	public static final String SOUNDCLOUD_SETTINGS = "preferences";
	private Handler mHandler = new Handler();
	public int mCurrentTrackIndex;

	private boolean mLoginFirst = false;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(LOG_TAG, "Starting wallpaper service");

	}

	/**
	 * offset for time bound reload / randomized displayed waveform png
	 */
	static long timeOffset = System.currentTimeMillis();

	public SoundCloudLiveWallpaperService() {

	}

	/**
	 * create the toast for login credentials and return true if user has set it
	 * 
	 * @return true if login credentials has been set
	 */

	@Override
	public Engine onCreateEngine() {
		try {
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mCurrentTrackIndex = 0;
		return new SoundCloudWallpaperEngine();
	}

	class SoundCloudWallpaperEngine extends Engine implements
			SharedPreferences.OnSharedPreferenceChangeListener,
			OnGestureListener {

		private TrackListLoader mTrackListLoader;

		private boolean mVisible;

		private WaveFormDrawManager mDrawManager = new WaveFormDrawManager(this);

		private List<Tracks> tracks = new ArrayList<Tracks>();

		SoundCloudApi mApiWrapper;

		private GestureDetector mGestureDetector;

		private boolean mIsUpdating = true;

		SharedPreferences mPrefs;

		private String mSoundCloudUser = "challenge";
		private String mSoundCloudPassword = "challenge123";
		private String mSoundCloudSource = "tracks";
		private boolean mEnableDownloadFeature = false;
		private int mDownloadLimit = 8;

		// false);

		SoundCloudWallpaperEngine() {
			Log.i(LOG_TAG,
					"in constructor onSharedPreferenceListener has changed ");
			mApiWrapper = new SoundCloudApi(mSoundCloudUser,
					mSoundCloudPassword, mSoundCloudSource);
			mPrefs = SoundCloudLiveWallpaperService.this.getSharedPreferences(
					SOUNDCLOUD_SETTINGS, 0);
			mPrefs.registerOnSharedPreferenceChangeListener(this);
			onSharedPreferenceChanged(mPrefs, null);

		}

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			Log.i(LOG_TAG, "onSharedPreference changed now ");
			mSoundCloudPassword = mPrefs.getString("password", "linus123");
			mSoundCloudUser = mPrefs.getString("login", "mschlech");
			mSoundCloudSource = mPrefs.getString("source", "tracks");
			mEnableDownloadFeature = mPrefs.getBoolean("enableDownload", false);
			mDownloadLimit = Integer.parseInt(mPrefs.getString("downloadlimit",
					"8"));
			new SoundCloudApi(mSoundCloudUser, mSoundCloudPassword,
					mSoundCloudSource);
		}

		public void scheduleDraw() {
			if (mIsUpdating) {
				mHandler.removeCallbacks(mDrawWaveFrameRunnable);
				mHandler.post(mDrawWaveFrameRunnable);
			}
		}

		void drawWavePic() {
			final SurfaceHolder holder = getSurfaceHolder();

			Canvas c = null;
			try {
				c = holder.lockCanvas();
				if (c != null) {
					mDrawManager.onDraw(c);
				}
			} finally {
				if (c != null)
					holder.unlockCanvasAndPost(c);
			}
		}

		/**
		 * 
		 */
		private final Runnable mDrawWaveFrameRunnable = new Runnable() {
			public void run() {

				drawWavePic();
			}
		};

		/**
		 * task get a random run so a random waveform from the soundcloud
		 * resource
		 */
		private final Runnable mNextRun = new Runnable() {
			public void run() {
				getRandomTrack();
				if (mIsUpdating) {
					mHandler.postDelayed(this, 15000);
				}
			}
		};

		/**
		 * 
		 */
		private void getRandomTrack() {

			if (tracks.size() != 0) {
				Random randomWavForm = new Random();
				int max = tracks.size() - 1;
				int min = 0;
				mCurrentTrackIndex = (randomWavForm.nextInt(max - min + 1) + min);
				mDrawManager.setTrack(tracks.get(mCurrentTrackIndex));
			}
		}

		@Override
		public Bundle onCommand(String action, int x, int y, int z,
				Bundle extras, boolean resultRequested) {
			return super.onCommand(action, x, y, z, extras, resultRequested);
		}

		/**
		 * 
		 */
		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			setTouchEventsEnabled(true);

			try {
				Log.i(LOG_TAG, "get tracks object");
				new TrackListLoader(this).execute();
			} catch (Exception e) {
				e.printStackTrace();
			}

			mDrawManager.onCreate(getApplicationContext());

			/**
			 * optional task to open the browser or the installed soundcloud app
			 * and invoke the permalink_url
			 */
			mGestureDetector = new GestureDetector(this);

			mGestureDetector.setOnDoubleTapListener(new OnDoubleTapListener() {

				@Override
				public boolean onDoubleTap(MotionEvent e) {
					String soundCloudUrl = tracks.get(mCurrentTrackIndex).permalink_url;
					Intent soundCloudBrowser = new Intent(Intent.ACTION_VIEW);
					soundCloudBrowser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

					soundCloudBrowser.setData(Uri.parse(soundCloudUrl));
					mCurrentTrackIndex = 0;
					startActivity(soundCloudBrowser);

					return false;
				}

				@Override
				public boolean onDoubleTapEvent(MotionEvent e) {
					return false;
				}

				@Override
				public boolean onSingleTapConfirmed(MotionEvent e) {
					Log.i(LOG_TAG,
							"could invoke something else on one tab because double tab was not proper");
					if (!mDrawManager.onTap(e.getX(), e.getY())) {
						mHandler.removeCallbacks(mNextRun);
						mHandler.post(mNextRun);
					}
					return true;
				}
			});

		}

		/**
		 * set bitmap free release the bitmap resources
		 */
		@Override
		public void onDestroy() {
			super.onDestroy();
			mDrawManager.onDestroy();
			if (mTrackListLoader != null) {
				mTrackListLoader.cancel(true);
			}
			mCurrentTrackIndex = 0;
			mHandler.removeCallbacks(mDrawWaveFrameRunnable);
			mHandler.removeCallbacks(mNextRun);
			mIsUpdating = false;
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset,
				float xOffsetStep, float yOffsetStep, int xPixelOffset,
				int yPixelOffset) {
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep,
					xPixelOffset, yPixelOffset);
			mDrawManager.setScrollOffset(xPixelOffset, xOffset);
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
			holder.setFormat(PixelFormat.RGBA_8888);
			super.onSurfaceCreated(holder);
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			mHandler.removeCallbacks(mDrawWaveFrameRunnable);
		}

		@Override
		public void onTouchEvent(MotionEvent event) {
			super.onTouchEvent(event);
			mGestureDetector.onTouchEvent(event);

		}

		/**
		 * the OnGestureListener Events to get the double tap feature
		 */
		@Override
		public boolean onDown(MotionEvent arg0) {
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {

			/**
			 * prevent download when dragging the system tray down
			 */
			if ((Math.abs(e1.getY()) - Math.abs(e2.getY()) > 250)) {
				Log.i(LOG_TAG, "out of range");
				return false;
			}
			if (Math.abs(e2.getY()) - Math.abs(e2.getX()) > 180) {
				Log.i(LOG_TAG, "swipe iniated download "
						+ mEnableDownloadFeature);
				if (mEnableDownloadFeature) {
					Log.i(LOG_TAG,
							" captured appropriate movement for new download ");
					mHandler.removeCallbacks(mDrawWaveFrameRunnable);
					new TrackListLoader(this).execute();
				}

			}
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			super.onVisibilityChanged(visible);
			Log.i(LOG_TAG, "Preferences in OnVisibleChanged " + mSoundCloudUser);

			mVisible = visible;
			if (mVisible) {
				Log.i(LOG_TAG,
						"onVisibilityChanged is true so invoke the drawWavePic method");
				mIsUpdating = true;
				Log.i(LOG_TAG, "soundcloud User =" + mSoundCloudUser
						+ " soundcloud password =							" + mSoundCloudPassword);
				mHandler.removeCallbacks(mDrawWaveFrameRunnable);
				new TrackListLoader(this).execute();
				drawWavePic();
				mHandler.removeCallbacks(mNextRun);
				mHandler.post(mNextRun);
			} else {
				mHandler.removeCallbacks(mDrawWaveFrameRunnable);
				mHandler.removeCallbacks(mNextRun);
				mIsUpdating = false;
			}
		}

		@Override
		public void setTouchEventsEnabled(boolean enabled) {
			super.setTouchEventsEnabled(enabled);
		}

		public void setTracks(List<Tracks> result) {
			tracks.clear();
			if (result != null) {
				tracks.addAll(result);
			}
			mHandler.post(mNextRun);
		}

	}

	/**
	 * get the track information here and fill the tracks object
	 * 
	 * @author marcus
	 * 
	 */
	static class TrackListLoader extends AsyncTask<Void, Void, List<Tracks>> {

		private SoundCloudWallpaperEngine mSoundCloudWallpaperEngine;

		public TrackListLoader(SoundCloudWallpaperEngine swe) {
			mSoundCloudWallpaperEngine = swe;
		}

		final static String LOG_TAG = "TrackListLoader";

		@Override
		protected List<Tracks> doInBackground(Void... params) {
			try {
				Log.i(LOG_TAG, "loading the track information "
						+ mSoundCloudWallpaperEngine.mDownloadLimit);
				final int maxTrackCount = mSoundCloudWallpaperEngine.mDownloadLimit;
				/*
				 * in a real world app it would be necessary to create a bitmap
				 * cache on the sd card
				 */
				/*
				 * but for now i simply limit the number of tracks to keep in
				 * memory
				 */
				List<Tracks> list = SoundCloudApi
						.getMyWaveformUrl(SoundCloudApi.getApiWrapper(), mSoundCloudWallpaperEngine.mDownloadLimit);
				List<Tracks> result = new ArrayList<Tracks>(
						mSoundCloudWallpaperEngine.mDownloadLimit);
				for (int i = 0; i < mSoundCloudWallpaperEngine.mDownloadLimit
						&& i < list.size(); i++) {
					result.add(list.get(i));
				}
				for (Tracks track : result) {
					int maxHeight = (int) (mSoundCloudWallpaperEngine
							.getDesiredMinimumHeight() * 0.45);
					maxHeight = Math.min(maxHeight,
							track.waveFormURLPng.getHeight());
					Bitmap scaled = Bitmap
							.createScaledBitmap(track.waveFormURLPng,
									mSoundCloudWallpaperEngine
											.getDesiredMinimumWidth(),
									maxHeight, true);
					track.waveFormURLPng.recycle();
					track.waveFormURLPng = scaled;
				}
				return result;

			} catch (Exception e) {
				Log.e(LOG_TAG, e.getMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<Tracks> result) {
			Log.i(LOG_TAG, "done and post execute");
			mSoundCloudWallpaperEngine.setTracks(result);
		}
	}

}
