package com.example.soundcloud.challange;

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
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

public class WaveFormDrawManager {

	private String LOG_TAG = WaveFormDrawManager.class.getSimpleName();

	private String mGenre = "HipHop";
	private String mTitle = "Hot sauce";
	private String mPermalinkUrl = "www.google.de";

	private Bitmap mWaveformBitmap;

	private final Paint mTextPaint;
	private final Paint mRectPaint;
	private final Paint mWaveFormPaint;

	private int mCenterY;
	private int mCenterX;

	private static long TIME_OFFSET = System.currentTimeMillis();

	private final Rect mRect = new Rect();

	private float mDensity = 1;

	private final static int[] COLORS = { 0, Color.WHITE, Color.BLACK };
	private final static float[] POSITIONS = { 0, 0.5f, 1 };

	/**
	 * default constructor defining the style static initializer
	 */
	public WaveFormDrawManager() {
		/*
		 * The matrix is stored in a single array, and its treated as follows: [
		 * a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t ] When
		 * applied to a color [r, g, b, a], the resulting color is computed as
		 * (after clamping) ; � R' = a*R + b*G + c*B + d*A + e; G' = f*R + g*G +
		 * h*B + i*A + j; B' = k*R + l*G + m*B + n*A + o; A' = p*R + q*G + r*B +
		 * s*A + t;
		 */
		Paint paint = new Paint();
		float[] matrix = { 1, 1, 1, 1, 1, // red
				0, 0, 0, 0, 0, // green
				0, 0, 0, 0, 0, // blue
				1, 1, 1, 1, 1 // alpha
		};

		mTextPaint = new Paint() {
			{
				setTextSize(50f);
				setColor(Color.WHITE);
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

		mRectPaint = new Paint() {
			{
				setStyle(Style.FILL);
			}

		};

	}

	public void onCreate(final Context context) {

		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics metrics = new DisplayMetrics();
		windowManager.getDefaultDisplay().getMetrics(metrics);
		/**
		 * for high or low dpi device 
		 */
		mDensity = metrics.density;
		mWaveformBitmap = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.testwave);

	}

	public void onDraw(final Canvas c) {
		c.drawColor(Color.BLACK);
		final int translateY = mCenterY - mWaveformBitmap.getHeight() / 2;
		Log.i(LOG_TAG, "TRANSLATE Y = " + translateY);
		c.save();

		c.translate(0, translateY);
		c.drawRect(mRect, mRectPaint);

		c.drawBitmap(mWaveformBitmap, 0, 0, mWaveFormPaint);

		/**
		 * moving the backround not the object
		 */
		c.restore();

		/**
		 * permanent moving value between -1 and 1
		 */
		final double time = System.currentTimeMillis() - TIME_OFFSET;
		final double scale = (Math.sin(time) + 1) * 8 * mDensity;
		mTextPaint.setMaskFilter(new BlurMaskFilter((float)scale, Blur.SOLID));

		c.drawText(mTitle, 100, 100, mTextPaint);
		c.drawText(mGenre, 140, 140, mTextPaint);
		c.drawText(mTitle, 180, 180, mTextPaint);

	}

	/**
	 * @TODO read bitmap google blog handling bitmaps clean up native heap
	 *       memory
	 */
	public void onDestroy() {
		if (mWaveformBitmap != null) {
			mWaveformBitmap.recycle();
			mWaveformBitmap = null;
		}

	}

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
}