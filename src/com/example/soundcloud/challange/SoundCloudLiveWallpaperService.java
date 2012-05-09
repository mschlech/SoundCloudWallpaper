package com.example.soundcloud.challange;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
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

	public int mCurrentTrackIndex;

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
		mCurrentTrackIndex = 0;
		return new SoundCloudWallpaperEngine();
	}

	class SoundCloudWallpaperEngine extends Engine implements
			SharedPreferences.OnSharedPreferenceChangeListener,
			OnGestureListener {

		private SharedPreferences mPrefs = PreferenceManager
				.getDefaultSharedPreferences(SoundCloudLiveWallpaperService.this);

		private String soundCloudUser = mPrefs.getString("login", "mschlech");

		private String soundCloudPassword = mPrefs.getString("password",
				"linus123");

		private String soundCloudSource = mPrefs.getString("source",
				"favorites");

		private TrackListLoader mTrackListLoader;

		private boolean mVisible;

		private WaveFormDrawManager mDrawManager = new WaveFormDrawManager();

		private List<Tracks> tracks = new ArrayList<Tracks>();

		SoundCloudApi mApiWrapper;

		private GestureDetector mGestureDetector;

		private boolean mIsUpdating = true;

		SoundCloudWallpaperEngine() {
			mApiWrapper = new SoundCloudApi(soundCloudUser, soundCloudPassword,
					soundCloudSource);

			mPrefs.registerOnSharedPreferenceChangeListener(this);
			onSharedPreferenceChanged(mPrefs, null);
		}

		void drawWavePic() {
			long now = SystemClock.currentThreadTimeMillis();
			// Log.i(LOG_TAG, " drawPic invoked" + run + " invocationTime " +
			// (SystemClock.currentThreadTimeMillis()/(60*1000)));
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
				// Log.i(LOG_TAG, "mVisible = " + mVisible);
				mHandler.postDelayed(mDrawWaveFrameRunnable, 1000 / 25);
			}
			// mHandlerTrack.postDelayed(mNextRun, 1000 / 25);

		}

		/**
		 * calls the WaveFormDrawManager
		 * 
		 * @param c
		 */

		private void drawFrame(final Canvas c) {
			mDrawManager.onDraw(c);
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
					mHandler.postDelayed(this, 5000); 
				}
			}
		};

		/**
		 * 
		 */
		private void getRandomTrack() {
			Log.i(LOG_TAG, "next getRandomRunTask" + tracks.size());
			if(tracks.size()!=0) {
			Random randomWavForm = new Random();
			int max = tracks.size() - 1;
			int min = 0;
			mCurrentTrackIndex = (randomWavForm.nextInt(max - min + 1) + min);
			mDrawManager.setTrack(tracks.get(mCurrentTrackIndex));
			}
		}

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
		
				// Log.i(LOG_TAG, "in onCreate tracks to pass = " + tracks);
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
					// if the second tap hadn't been released and it's being
					// moved
					if (e.getAction() == MotionEvent.ACTION_MOVE) {
						Log.i(LOG_TAG,
								" could invoke something else because a movement on the second tab");
					} else if (e.getAction() == MotionEvent.ACTION_UP)// user
																		// released
																		// the
																		// screen
					{
					}
					return false;
				}

				@Override
				public boolean onSingleTapConfirmed(MotionEvent e) {
					Log.i(LOG_TAG,
							"could invoke something else on one tab because double tab was not proper");

					mHandler.removeCallbacks(mNextRun);
					mHandler.post(mNextRun);
					return false;
				}
			});

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
			mCurrentTrackIndex = 0;
			mHandler.removeCallbacks(mDrawWaveFrameRunnable);
			mHandler.removeCallbacks(mNextRun);
			mIsUpdating = false;
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
			// mHandlerTrack.removeCallbacks(mNextRun);

		}

		@Override
		public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			super.onSurfaceRedrawNeeded(holder);
		}

		@Override
		public void onTouchEvent(MotionEvent event) {

			super.onTouchEvent(event);
			Log.i(LOG_TAG, "on touch -> " + event);

			mGestureDetector.onTouchEvent(event);

		}

		/**
		 * the OnGestureListener Events to get the double tap feature
		 */

		@Override
		public boolean onDown(MotionEvent arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
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
				mIsUpdating = true;
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

			mCurrentTrackIndex = 0;
		}

		public void setTracks(List<Tracks> result) {
			tracks.clear();
			tracks.addAll(result);
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

		private WaveFormDrawManager mDrawManager;
		private SoundCloudWallpaperEngine mSoundCloudWallpaperEngine;

		public TrackListLoader(SoundCloudWallpaperEngine swe) {
			mSoundCloudWallpaperEngine = swe;
		}

		final static String LOG_TAG = "TrackListLoader";

		@Override
		protected List<Tracks> doInBackground(Void... params) {
			try {
				Log.i(LOG_TAG, "loading the track information");
				return SoundCloudApi.getMyWaveformUrl(SoundCloudApi.getApiWrapper());
			} catch (Exception e) {
				Log.e(LOG_TAG, e.getMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<Tracks> result) {
			Log.i(LOG_TAG, "done and post execute");
			mSoundCloudWallpaperEngine.setTracks(result);
			// new GetBitmap(result.get(run).waveformUrl,
			// mDrawManager).execute();
			// super.onPostExecute(result);

		}
	}

	// private static class GetBitmap extends AsyncTask<Void, Void, Bitmap> {
	//
	// final static String LOG_TAG = "GetBitMap";
	// private URL mUrl;
	// private WaveFormDrawManager mDrawManager;
	//
	// public GetBitmap(final String uri, WaveFormDrawManager drawManager) {
	// mDrawManager = drawManager;
	// try {
	// Log.i(LOG_TAG, "constructor of GetBitmap invoked ");
	//
	// mUrl = new URL(uri);
	//
	// } catch (MalformedURLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	//
	// @Override
	// protected Bitmap doInBackground(Void... params) {
	// Bitmap result = null;
	// if (mUrl != null) {
	// HttpURLConnection connection;
	// try {
	// Log.i(LOG_TAG, " Bitmap fetch ");
	// connection = (HttpURLConnection) mUrl.openConnection();
	// connection.setDoInput(true);
	// connection.connect();
	// InputStream input = connection.getInputStream();
	// result = BitmapFactory.decodeStream(input);
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// }
	// Log.i(LOG_TAG, "result" + result);
	//
	// return result;
	//
	// }
	//
	// @Override
	// protected void onPostExecute(Bitmap result) {
	// super.onPostExecute(result);
	// Log.i(LOG_TAG, "done and post execute");
	//
	// mDrawManager.setBitmap(result);
	//
	// }
	// }
}
