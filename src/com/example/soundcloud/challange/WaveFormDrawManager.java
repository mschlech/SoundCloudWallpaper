package com.example.soundcloud.challange;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

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
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.example.soundcloud.challange.SoundCloudLiveWallpaperService.SoundCloudWallpaperEngine;
import com.example.soundcloud.challange.data.Tracks;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * 
 * @author marcus
 * 
 */
public class WaveFormDrawManager implements AnimatorListener {

	@SuppressWarnings("unused")
	private String LOG_TAG = WaveFormDrawManager.class.getSimpleName();

	// private Bitmap mWaveformBitmap;
	private Bitmap mSoundCloudLogoBitmap;

	private final TextPaint mTextPaint;
	private final Paint mRectPaint;
	private final Paint mWaveFormPaint;
	private final Paint mSoundCloudLogoPaint;
	private Animator mFadeInAnimator;
	private Animator mFadeOutAnimator;
	private Animator mTextOutAnimator;
	private Animator mTextInAnimator;
	private Tracks mLastTrack;
	private int mTitleColor = Color.WHITE;
	private int mOwnerColor = Color.WHITE;
	private ObjectAnimator mTextAnimator;
	private ObjectAnimator mTextScaleAnimator;
	private int mYPosTitle, mYPosOwner, mYPosLogo;

	/**
	 * get the actual information of the passed text object
	 */
	private Tracks mTrack;
	private boolean isLayoutValid = true;

	private int mScrollOffset = 0;

	private final Rect mGradientRect = new Rect();
	/**
	 * textlayout helper class
	 */
	private StaticLayout mStaticLayoutTitle;
	private StaticLayout mStaticLayoutOwner;
	private StaticLayout mStaticLayoutLastTitle;
	private StaticLayout mStaticLayoutLastOwner;

	private final static int[] COLORS = { 0, Color.rgb(255, 127, 0),
			0xffffffff, Color.rgb(255, 127, 0), 0 };
	private final static float[] POSITIONS = { 0, 0.3f, 0.5f, 0.7f, 1 };

	private int mWidth;
	private int mHeight;
	private SoundCloudWallpaperEngine mEngine;

	/**
	 * default constructor defining the style static initializer
	 */
	public WaveFormDrawManager(SoundCloudWallpaperEngine engine) {

		mEngine = engine;

		/**
		 * optional task to display the trackname / url on the homescreen
		 */
		mTextPaint = new TextPaint() {
			{
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
	public void onCreate(final Context context) {

		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics metrics = new DisplayMetrics();
		windowManager.getDefaultDisplay().getMetrics(metrics);
		/**
		 * for high or low dpi device
		 */
		mTextPaint.setTextSize(24 * metrics.density);

		if (mSoundCloudLogoBitmap == null) {
			mSoundCloudLogoBitmap = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.soundcloudlogo);
		}
	}

	/**
	 * 
	 * @param c
	 *            a Canvas to draw on
	 */
	public synchronized void onDraw(final Canvas c) {
		if (mTrack == null) {
			return;
		}

		if (!isLayoutValid) {
			createLayout();
		}

		c.drawColor(Color.BLACK);

		/* draw waveform */
		c.save();
		c.translate(0, mYPosOwner);
		c.drawRect(mGradientRect, mRectPaint);
		mWaveFormPaint.setAlpha((int) (mCurrentAlpha * 255));
		c.translate(mScrollOffset, 0);
		c.drawBitmap(mTrack.waveFormURLPng, 0, 0, mWaveFormPaint);

		if (mLastTrack != null) {
			mWaveFormPaint.setAlpha((int) (mLastAlpha * 255));
			c.drawBitmap(mLastTrack.waveFormURLPng, 0, 0, mWaveFormPaint);
		}
		c.restore();

		/* draw logo */
		c.save();

		final float scale = (mWidth * 1f) / mEngine.getDesiredMinimumWidth();
		final float pos = Math.abs(mScrollOffset) * scale;
		c.translate(pos, mYPosLogo);
		// c.translate(mWidth - mSoundCloudLogoBitmap.getWidth(), mYPosLogo);
		c.drawBitmap(mSoundCloudLogoBitmap, 0, 0, mSoundCloudLogoPaint);
		c.restore();

		/* draw title */
		c.save();
		c.translate(mCurrentTextPosition, mStaticLayoutTitle.getHeight());
		c.scale(mTitleSize, mTitleSize, mWidth / 2,
				mStaticLayoutTitle.getHeight() / 2);
		mTextPaint.setColor(mTitleColor);
		mStaticLayoutTitle.draw(c, null, mTextPaint, 0);
		c.restore();

		if (mStaticLayoutLastTitle != null && mLastTrack != null) {
			c.save();
			c.translate(mLastTextPosition, mStaticLayoutLastTitle.getHeight());
			c.scale(mTitleSize, mTitleSize, mWidth / 2,
					mStaticLayoutLastTitle.getHeight() / 2);
			mTextPaint.setColor(mTitleColor);
			mStaticLayoutLastTitle.draw(c, null, mTextPaint, 0);
			c.restore();
		}

		/* draw owner */
		if (mStaticLayoutOwner != null) {
			c.save();
			c.translate(mCurrentTextPosition, mYPosTitle);
			c.scale(mOwnerSize, mOwnerSize, mWidth / 2,
					mStaticLayoutOwner.getHeight() / 2);
			mTextPaint.setColor(mOwnerColor);
			mStaticLayoutOwner.draw(c, null, mTextPaint, 0);
			c.restore();
		}
		if (mStaticLayoutLastOwner != null && mLastTrack != null) {
			c.save();
			c.translate(mLastTextPosition, mYPosTitle);
			c.scale(mOwnerSize, mOwnerSize, mWidth / 2,
					mStaticLayoutLastOwner.getHeight() / 2);
			mTextPaint.setColor(mOwnerColor);
			mStaticLayoutLastOwner.draw(c, null, mTextPaint, 0);
			c.restore();
		}

		// mTextPaint.setMaskFilter(new BlurMaskFilter((float) scale,
		// Blur.SOLID));

	}

	/**
	 * check which region is taped and initialize the appropriate color aninmation.
	 * 
	 * @param x the x coordinate of the issued tab event
	 * @param y the y coordinate of the issued tab event
	 * @return
	 */
	public boolean onTap(final float x, final float y) {
		final int index = (int) ((Math.random() * 100) % 3);
		Log.i(LOG_TAG, "new animation type = " + index);
		/* hit test: which text was tapped? */
		if (y < mYPosTitle) {
			initColorAnimation("titleColor", "titleSize");
			return true;

		} else if (y < mYPosOwner) {
			initColorAnimation("ownerColor", "ownerSize");
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param propertyName String 
	 *            propertyname of the setter funktion to invoke. set the appropriate property
	 *            for Username oder titlename in the text aninmation process
	 * @param sizeProperty
	 */
	private void initColorAnimation(final String propertyName,
			final String sizeProperty) {
		if (mTextAnimator != null && mTextAnimator.isRunning()) {
			mTextAnimator.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator animation) {

				}

				@Override
				public void onAnimationRepeat(Animator animation) {
				}

				@Override
				public void onAnimationEnd(Animator animation) {
				}

				@Override
				public void onAnimationCancel(Animator animation) {
					startAnimation(propertyName, sizeProperty);
				}
			});
			mTextAnimator.cancel();
		}
		startAnimation(propertyName, sizeProperty);
	}

	private void startAnimation(final String propertyName,
			final String sizeProperty) {
		mTextAnimator = ObjectAnimator.ofInt(this, propertyName, Color.WHITE,
				Color.RED);
		mTextScaleAnimator = ObjectAnimator
				.ofFloat(this, sizeProperty, 1, 1.2f);

		mTextAnimator.setEvaluator(new ArgbEvaluator());
		AnimatorSet set = new AnimatorSet();
		set.playTogether(mTextAnimator, mTextScaleAnimator);
		set.setDuration(1000);
		mTextAnimator.setRepeatCount(1);
		mTextAnimator.setRepeatMode(ObjectAnimator.REVERSE);
		mTextScaleAnimator.setRepeatCount(1);
		mTextScaleAnimator.setRepeatMode(ObjectAnimator.REVERSE);
		set.start();
	}

	private float mTitleSize = 1;
	private float mOwnerSize = 1;

	public void setTitleSize(final float size) {
		mTitleSize = size;
	}

	public void setOwnerSize(final float size) {
		mOwnerSize = size;
	}

	public void setTitleColor(final int color) {
		Log.i(LOG_TAG, "setTitleColor" + color);
		mTitleColor = color;
		mEngine.scheduleDraw();
	}

	public void setOwnerColor(final int color) {
		mOwnerColor = color;
		mEngine.scheduleDraw();
	}

	/**
	 * 
	 * choose different mTextPaint for each text to display
	 */
	private void createLayout() {
		if (mTrack != null) {

			mStaticLayoutLastTitle = mStaticLayoutTitle;
			mStaticLayoutTitle = new StaticLayout(mTrack.trackName, mTextPaint,
					mWidth, Alignment.ALIGN_CENTER, 1, 0, false);

			if (mTrack.userName != null) {
				mStaticLayoutLastOwner = mStaticLayoutOwner;
				mStaticLayoutOwner = new StaticLayout(mTrack.userName,
						mTextPaint, mWidth, Alignment.ALIGN_CENTER, 1, 0, false);
			}

			final float startHeight = mHeight * 0.80f;

			mYPosLogo = (int) (startHeight - mSoundCloudLogoBitmap.getHeight());

			mYPosOwner = mYPosLogo - mTrack.waveFormURLPng.getHeight();
			final float remainingHeight = startHeight - mYPosOwner;
			mYPosTitle = (int) (remainingHeight * 0.67f);

			isLayoutValid = true;
		}
	}

	/**
	 * @TODO read bitmap google blog handling bitmaps clean up native heap
	 *       memory
	 */
	public void onDestroy() {
		if (mSoundCloudLogoBitmap != null) {
			mSoundCloudLogoBitmap.recycle();
			mSoundCloudLogoBitmap = null;
		}
	}

	public void setScrollOffset(final int xOffset) {
		mScrollOffset = xOffset;
		mEngine.scheduleDraw();
	}

	public synchronized void setTrack(Tracks track) {

		isLayoutValid = false;
		mLastTrack = mTrack;
		mTrack = track;
		if (mTrack.waveFormURLPng != null) {
			mGradientRect.set(0, 0, mWidth, mTrack.waveFormURLPng.getHeight());
			/**
			 * the horizontal gradient
			 */
			final int y1 = 0;
			final int x1 = mWidth / 2;
			final int y2 = mTrack.waveFormURLPng.getHeight();
			final int x2 = x1;
			// a linear gradient beginning from left right corner to right down
			// corner
			mRectPaint.setShader((new LinearGradient(x1, y1, x2, y2, COLORS,
					POSITIONS, TileMode.CLAMP)));
			// start fade in and fade out animation
			mFadeInAnimator = ObjectAnimator
					.ofFloat(this, "currentAlpha", 0, 1);
			mFadeOutAnimator = ObjectAnimator.ofFloat(this, "lastAlpha", 1, 0);
			mTextInAnimator = ObjectAnimator.ofFloat(this,
					"currentTextPosition", mWidth, 0);
			mTextOutAnimator = ObjectAnimator.ofFloat(this, "lastTextPosition",
					0, -mWidth);

			AnimatorSet set = new AnimatorSet();
			set.playTogether(mFadeInAnimator, mFadeOutAnimator,
					mTextInAnimator, mTextOutAnimator);
			set.setDuration(1000);

			set.addListener(this);
			set.start();
		}

	}

	private float mCurrentAlpha;
	private float mLastAlpha;
	private float mCurrentTextPosition;
	private float mLastTextPosition;

	public void setLastTextPosition(final float val) {
		mLastTextPosition = val;
	}

	public void setCurrentTextPosition(final float val) {
		mCurrentTextPosition = val;
	}

	public void setCurrentAlpha(final float val) {
		mCurrentAlpha = val;
		mEngine.scheduleDraw();

	}

	public void setLastAlpha(final float val) {
		mLastAlpha = val;
	}

	/**
	 * some visual effects to manipulate the image
	 * 
	 * @param width
	 * @param height
	 */
	public void onSizedChanged(final int width, final int height) {
		isLayoutValid = false;
		mWidth = width;
		mHeight = height;
	}

	@Override
	public void onAnimationStart(Animator animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationEnd(Animator animation) {
		mLastTrack = null;
	}

	@Override
	public void onAnimationCancel(Animator animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationRepeat(Animator animation) {
		// TODO Auto-generated method stub

	}

}
