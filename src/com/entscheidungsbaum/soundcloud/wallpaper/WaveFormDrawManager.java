package com.entscheidungsbaum.soundcloud.wallpaper;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.entscheidungsbaum.soundcloud.wallpaper.SoundCloudLiveWallpaperService.SoundCloudWallpaperEngine;
import com.entscheidungsbaum.soundcloud.wallpaper.data.Tracks;
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

	private String LOG_TAG = WaveFormDrawManager.class.getSimpleName();

	private Bitmap mSoundCloudLogoBitmap;

	// all about animation and its properties
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
	private ObjectAnimator mTextColorAnimator;
	private ObjectAnimator mTextRotationAnimator;
	private int mYPosTitle, mYPosOwner, mYPosLogo;
	private float mCurrentAlpha;
	private float mLastAlpha;
	private float mCurrentTextPosition;
	private float mLastTextPosition;
	private int mTitleRotation = 0;
	private int mOwnerRotation = 0;
	private Camera mCamera =new Camera();
	private Matrix mMatrix = new Matrix();

	/**
	 * get the actual information of the passed text object
	 */
	private Tracks mTrack;
	private boolean isLayoutValid = true;

	private int mScrollOffset = 0;
	private float mScrollOffsetRel=0;

	private final Rect mGradientRect = new Rect();
	/**
	 * textlayout helper class
	 */
	private StaticLayout mStaticLayoutTitle;
	private StaticLayout mStaticLayoutOwner;
	private StaticLayout mStaticLayoutLastTitle;
	private StaticLayout mStaticLayoutLastOwner;

	private final static int[] COLORS = { 0, Color.rgb(255, 127, 0), 0xffffffff, Color.rgb(255, 127, 0), 0 };
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

		mSoundCloudLogoPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
			{
				setAlpha(200);
				setFilterBitmap(true);
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

		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics metrics = new DisplayMetrics();
		windowManager.getDefaultDisplay().getMetrics(metrics);
		/**
		 * for high or low dpi device
		 * TODO put font size into preference screen
		 */
		mTextPaint.setTextSize(24 * metrics.density);

		if (mSoundCloudLogoBitmap == null) {
			mSoundCloudLogoBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.soundcloudlogochallenge);
		}
	}
	
	private void makeRotationMatrix(final Matrix outMatrix, final int degrees, boolean x, boolean y, boolean z, int translateY){
		mCamera.save();
		if (x) mCamera.rotateX(degrees);
		if (y) mCamera.rotateY(degrees);
		if (z) mCamera.rotateZ(degrees);
		mCamera.getMatrix(mMatrix);
		mCamera.restore();
		outMatrix.preTranslate(-mWidth/2, -translateY);
		outMatrix.postTranslate(mWidth/2, translateY);
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
		makeRotationMatrix(mMatrix, mOwnerRotation,true,false,false,mTrack.waveFormURLPng.getHeight()/2);
		c.concat(mMatrix);
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
		final float total = mWidth-mSoundCloudLogoBitmap.getWidth();
		final float offset = total * mScrollOffsetRel;
		c.translate(offset, mYPosLogo);
		c.drawBitmap(mSoundCloudLogoBitmap, 0, 0, mSoundCloudLogoPaint);
		c.restore();

		/* draw title */
		mTextPaint.setColor(mTitleColor);
		c.save();
		c.translate(mCurrentTextPosition, mStaticLayoutTitle.getHeight());
		makeRotationMatrix(mMatrix, mTitleRotation,false,true,false,mStaticLayoutTitle.getHeight()/2);
		c.concat(mMatrix);
		mStaticLayoutTitle.draw(c, null, mTextPaint, 0);
		c.restore();

		if (mStaticLayoutLastTitle != null && mLastTrack != null) {
			c.save();
			c.translate(mLastTextPosition, mStaticLayoutLastTitle.getHeight());
			mTextPaint.setColor(mTitleColor);
			mStaticLayoutLastTitle.draw(c, null, mTextPaint, 0);
			c.restore();
		}

		/* draw owner */
		if (mStaticLayoutOwner != null) {
			c.save();
			c.translate(mCurrentTextPosition, mYPosTitle);
			mTextPaint.setColor(mOwnerColor);
			mStaticLayoutOwner.draw(c, null, mTextPaint, 0);
			c.restore();
		}
		if (mStaticLayoutLastOwner != null && mLastTrack != null) {
			c.save();
			c.translate(mLastTextPosition, mYPosTitle);
			c.rotate(mOwnerRotation);
			mTextPaint.setColor(mOwnerColor);
			mStaticLayoutLastOwner.draw(c, null, mTextPaint, 0);
			c.restore();
		}

		// mTextPaint.setMaskFilter(new BlurMaskFilter((float) scale,
		// Blur.SOLID));
	}

	/**
	 * check which region is taped and initialize the appropriate color
	 * aninmation.
	 * 
	 * @param x
	 *            the x coordinate of the issued tab event
	 * @param y
	 *            the y coordinate of the issued tab event
	 * @return
	 */
	public boolean onTap(final float x, final float y) {
		final int index = (int) ((Math.random() * 100) % 3);
		Log.i(LOG_TAG, "new animation type = " + index);
		/* hit test: which text was tapped? */
		if (y < mYPosTitle) {
			initColorAnimation("titleColor", "titleRotation");
			return true;

		} else if (y < mYPosOwner) {
			initColorAnimation("ownerColor", "ownerRotation");
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param propertyName
	 *            String propertyname of the setter funktion to invoke. set the
	 *            appropriate property for Username oder titlename in the text
	 *            aninmation process
	 * @param sizeProperty
	 */
	private void initColorAnimation(final String propertyName, final String propertyName2) {
		if (mTextColorAnimator != null && mTextColorAnimator.isRunning()) {
			mTextColorAnimator.addListener(new AnimatorListener() {

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
					startAnimation(propertyName, propertyName2);
				}
			});
			mTextColorAnimator.cancel();
		}
		startAnimation(propertyName, propertyName2);
	}

	private void startAnimation(final String propertyName, final String propertyName2) {
		mTextColorAnimator = ObjectAnimator.ofInt(this, propertyName, Color.rgb(217, 217, 217), Color.rgb(255, 127, 0),Color.rgb(217, 217, 217));
		mTextRotationAnimator = ObjectAnimator.ofInt(this, propertyName2, 0, 360);

		mTextColorAnimator.setEvaluator(new ArgbEvaluator());
		AnimatorSet set = new AnimatorSet();
		set.playTogether(mTextColorAnimator, mTextRotationAnimator);
		set.setDuration(2000);
		set.start();
	}


	public void setTitleRotation(final int val) {
		mTitleRotation = val;
	}

	public void setOwnerRotation(final int val) {
		mOwnerRotation = val;
	}

	public void setTitleColor(final int color) {
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
			mStaticLayoutTitle = new StaticLayout(mTrack.trackName, mTextPaint, mWidth, Alignment.ALIGN_CENTER, 1, 0, false);

			if (mTrack.userName != null) {
				mStaticLayoutLastOwner = mStaticLayoutOwner;
				mStaticLayoutOwner = new StaticLayout(mTrack.userName, mTextPaint, mWidth, Alignment.ALIGN_CENTER, 1, 0, false);
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

	public void setScrollOffset(final int xOffset, float xOffsetRel) {
		mScrollOffset = xOffset;
		mScrollOffsetRel = xOffsetRel;
		mEngine.scheduleDraw();
	}

	public synchronized void setTrack(Tracks track) {

		isLayoutValid = false;
		mLastTrack = mTrack;
		mTrack = track;
		if (mTrack.waveFormURLPng != null) {
			mGradientRect.set(0, 0, mWidth, mTrack.waveFormURLPng.getHeight());
			/* the horizontal gradient */
			final int y1 = 0;
			final int x1 = mWidth / 2;
			final int y2 = mTrack.waveFormURLPng.getHeight();
			final int x2 = x1;
			mRectPaint.setShader((new LinearGradient(x1, y1, x2, y2, COLORS, POSITIONS, TileMode.CLAMP)));
			// start fade in and fade out animation
			mFadeInAnimator = ObjectAnimator.ofFloat(this, "currentAlpha", 0, 1);
			mFadeOutAnimator = ObjectAnimator.ofFloat(this, "lastAlpha", 1, 0);
			mTextInAnimator = ObjectAnimator.ofFloat(this, "currentTextPosition", mWidth, 0);
			mTextOutAnimator = ObjectAnimator.ofFloat(this, "lastTextPosition", 0, -mWidth);

			AnimatorSet set = new AnimatorSet();
			set.playTogether(mFadeInAnimator, mFadeOutAnimator, mTextInAnimator, mTextOutAnimator);
			set.setDuration(1000);

			set.addListener(this);
			set.start();
		}
	}

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
	}

	@Override
	public void onAnimationEnd(Animator animation) {
		mLastTrack = null;
	}

	@Override
	public void onAnimationCancel(Animator animation) {
	}

	@Override
	public void onAnimationRepeat(Animator animation) {
	}

}
