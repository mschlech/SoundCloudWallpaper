package com.example.soundcloud.challange;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

/**
 * 
 * @author marcus
 * 
 */
public class WaveFormDrawManager {

	private String LOG_TAG = WaveFormDrawManager.class.getSimpleName();

	private Bitmap mWaveformBitmap;
	private Bitmap mSoundCloudLogoBitmap;

	private final Paint mTextPaint;
	private final Paint mRectPaint;
	private final Paint mWaveFormPaint;
	private final Paint mSoundCloudLogoPaint;

	private int mCenterY;
	private int mCenterX;

	private static long TIME_OFFSET = System.currentTimeMillis();

	private final Rect mRect = new Rect();

	private float mDensity = 1;

	private final static int[] COLORS = { 0, Color.rgb(255, 127, 0),
			Color.rgb(247, 247, 247) };
	private final static float[] POSITIONS = { 0, 0.5f, 1 };

	private URL mUrl;

	private Context mContext;

	/**
	 * default constructor defining the style static initializer
	 */
	public WaveFormDrawManager() {

		/*
		 * The matrix is stored in a single array, and its treated as follows: [
		 * a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t ] When
		 * applied to a color [r, g, b, a], the resulting color is computed as
		 * (after clamping) ; Ê R' = a*R + b*G + c*B + d*A + e; G' = f*R + g*G +
		 * h*B + i*A + j; B' = k*R + l*G + m*B + n*A + o; A' = p*R + q*G + r*B +
		 * s*A + t;
		 */
		Paint paint = new Paint();
		float[] matrix = { 1, 1, 1, 1, 1, // red
				0, 0, 0, 0, 0, // green
				0, 0, 0, 0, 0, // blue
				1, 1, 1, 1, 1 // alpha
		};

		/**
		 * optional task to display the trackname / url on the homescreen
		 */
		mTextPaint = new Paint() {
			{
				setTextSize(30f);
				setColor(Color.rgb(255, 255, 240));
				setMaskFilter(new BlurMaskFilter(3, Blur.SOLID));
			}

		};
		mWaveFormPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {

			{
				/**
				 * antialias the bitmap onle edges
				 */
				setFilterBitmap(true);
				setAntiAlias(true);
				// 0xA R G B
				// white = 255 255 255 255
				// 255 0 0 0
				setColorFilter(new LightingColorFilter(0xff000000, 0x0));
			}

		};

		mSoundCloudLogoPaint = new Paint() {
			{
				setAntiAlias(true);
			}
		};

		mRectPaint = new Paint() {
			{
				setStyle(Style.FILL);
			}

		};

	}

	/**
	 * 
	 * @param context
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 */
	public void onCreate(final Context context, String waveformUrl) {

		Log.i(LOG_TAG, " onCreate in WaveFormDrawManager " + waveformUrl);
		mContext = context;
		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics metrics = new DisplayMetrics();
		windowManager.getDefaultDisplay().getMetrics(metrics);
		/**
		 * for high or low dpi device
		 */
		mDensity = metrics.density;

		mWaveformBitmap = getBitmapFromSoundCloud(waveformUrl);
		mSoundCloudLogoBitmap = BitmapFactory.decodeResource(
				context.getResources(), R.drawable.soundcloudlogo);

	}

	/**
	 * 
	 * @param uri
	 * @return
	 */
	private Bitmap getBitmapFromSoundCloud(String url) {
		try {
			mUrl = new URL(url);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {

		}
		Bitmap result = null;
		if (url != null) {
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

	/**
	 * 
	 * @param c
	 *            a Canvas to draw on
	 */
	public void onDraw(final Canvas c, String genre, String title,
			String permalink_url) {
		Log.i(LOG_TAG, "genre = " + genre + " title " + title
				+ " permalink_url " + permalink_url);

		c.drawColor(Color.BLACK);
		final int translateY = mCenterY - mWaveformBitmap.getHeight() / 2;
		// Log.i(LOG_TAG, "TRANSLATE Y = " + translateY);
		c.save();

		c.translate(0, translateY);
		c.drawRect(mRect, mRectPaint);

		c.drawBitmap(mWaveformBitmap, 0, 0, mWaveFormPaint);
		/**
		 * @TODO resize the image to the appropriate position via onSizedChanged
		 */
		c.drawBitmap(mSoundCloudLogoBitmap, 100, 100, mSoundCloudLogoPaint);
		/**
		 * moving the backround not the object
		 */
		c.restore();

		/**
		 * permanent moving value between -1 and 1
		 */
		final double time = System.currentTimeMillis() - TIME_OFFSET;
		final double scale = (Math.sin(time) + 1) * 8 * mDensity;
		mTextPaint.setMaskFilter(new BlurMaskFilter((float) scale, Blur.SOLID));

		/**
		 * rotate and flying text
		 */
		// long et = SystemClock.elapsedRealtime();
		// float mXrotation = ((float) (et - TIME_OFFSET)) / 1000;
		// float mYrotation = ((float) (0.5f - mCenterY) * 2.0f);
		// float newY = (float) (Math.sin(mXrotation) * (mCenterX) + Math
		// .cos(mXrotation) * (mCenterY));
		//
		// float newZ = (float) (Math.cos(mXrotation) * mCenterX - Math
		// .sin(mXrotation) * mCenterY);
		//
		// float newX = (float)(Math.sin(mYrotation) * newZ +
		// Math.cos(mYrotation) * mXrotation);
		// newZ = (float)(Math.cos(mYrotation) * newZ - Math.sin(mYrotation) *
		// mXrotation);

		/**
		 * optional task to display the trackname / url on the homescreen
		 */
		int x = (int) ((int) (300) + (Math.sin(time) + 1) * 2);
		int y = (int) ((int) (400) + (Math.sin(time) + 1) * 2);
		Rect rect = new Rect();
		mTextPaint.getTextBounds(genre, 0, genre.length(), rect);
		c.translate(x, y);
		mTextPaint.setStyle(Paint.Style.FILL);

		c.translate(-x, -y);
		c.rotate(-45, x + rect.exactCenterX(), y + rect.exactCenterY());
		mTextPaint.setStyle(Paint.Style.FILL);
		mTextPaint.setFakeBoldText(true);
		c.drawText(genre, x, y, mTextPaint);

		c.drawText(genre, 100, 100, mTextPaint);
		c.drawText(title, 140, 140, mTextPaint);
		c.drawText(permalink_url, 180, 180, mTextPaint);

	}

	/**
	 * @TODO read bitmap google blog handling bitmaps clean up native heap
	 *       memory
	 */
	public void onDestroy() {
		if (mWaveformBitmap != null && mSoundCloudLogoBitmap != null) {
			mWaveformBitmap.recycle();
			mSoundCloudLogoBitmap.recycle();

			mWaveformBitmap = null;
			mSoundCloudLogoBitmap = null;
		}

	}

	/**
	 * some visual effects to manipulate the image
	 * 
	 * @param width
	 * @param height
	 */
	public void onSizedChanged(final int width, final int height) {
		mCenterY = height / 2;
		if (mWaveformBitmap != null) {
			mRect.set(0, 0, width, mWaveformBitmap.getHeight());

			/**
			 * the horizontal gradient
			 */
			final int y1 = 0;
			final int x1 = width / 2;
			final int y2 = mWaveformBitmap.getHeight();
			final int x2 = x1;
			// a linear gradient beginning from left right corner to right down
			// corner
			mRectPaint.setShader((new LinearGradient(x1, y1, x2, y2, COLORS,
					POSITIONS, TileMode.CLAMP)));

		} else {
			Log.w(LOG_TAG, " forgot to call onCreate Bitmap is "
					+ mWaveformBitmap);
		}
	}

	/**
	 * 
	 * @param result
	 */
	public void setBitmap(Bitmap result) {
		if (mWaveformBitmap != null && mSoundCloudLogoBitmap != null) {
			mSoundCloudLogoBitmap.recycle();
			mWaveformBitmap.recycle();

		}
		mSoundCloudLogoBitmap = BitmapFactory.decodeResource(
				mContext.getResources(), R.drawable.soundcloudlogo);
		mWaveformBitmap = result;
	}
}
