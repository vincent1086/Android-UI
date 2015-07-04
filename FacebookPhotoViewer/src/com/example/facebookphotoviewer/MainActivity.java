package com.example.facebookphotoviewer;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity {

	private static final int IMAGE_RES_ID = R.drawable.ic_image_test;
    private static final int ANIM_DURATION = 5000;
    private final Handler mHandler = new Handler();
    private ImageView mThumbnailImageView;
    private CustomImageView mFullImageView;
    private Point mFitSizeBitmap;
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		mFullImageView = (CustomImageView) findViewById(R.id.fullImageView);
		mThumbnailImageView = (ImageView) findViewById(R.id.thumbnailImageView);
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				prepareAndStartAnimation();
			}

		}, 2000);
	}

	private void prepareAndStartAnimation() {
		final int thumbX = mThumbnailImageView.getLeft(), thumbY = mThumbnailImageView
				.getTop();
		final int thumbWidth = mThumbnailImageView.getWidth(), thumbHeight = mThumbnailImageView
				.getHeight();
		final View container = (View) mFullImageView.getParent();
		final int containerWidth = container.getWidth(), containerHeight = container
				.getHeight();
		final Options bitmapOptions = getBitmapOptions(getResources(), IMAGE_RES_ID);
		mFitSizeBitmap = getFitSize(bitmapOptions.outWidth,
				bitmapOptions.outHeight, containerWidth, containerHeight);

		mThumbnailImageView.setVisibility(View.GONE);
		mFullImageView.setVisibility(View.VISIBLE);
		mFullImageView.setContentWidth(thumbWidth);
		mFullImageView.setContentHeight(thumbHeight);
		mFullImageView.setContentX(thumbX);
		mFullImageView.setContentY(thumbY);
		runEnterAnimation(containerWidth, containerHeight);
	}

	private Point getFitSize(final int width, final int height,
			final int containerWidth, final int containerHeight) {
		int resultHeight, resultWidth;
		resultHeight = height * containerWidth / width;
		if (resultHeight <= containerHeight) {
			resultWidth = containerWidth;
		} else {
			resultWidth = width * containerHeight / height;
			resultHeight = containerHeight;
		}
		return new Point(resultWidth, resultHeight);
	}

	public void runEnterAnimation(final int containerWidth,
			final int containerHeight) {
		final ObjectAnimator widthAnim = ObjectAnimator.ofInt(mFullImageView,
				"contentWidth", mFitSizeBitmap.x).setDuration(ANIM_DURATION);
		final ObjectAnimator heightAnim = ObjectAnimator.ofInt(mFullImageView,
				"contentHeight", mFitSizeBitmap.y).setDuration(ANIM_DURATION);
		final ObjectAnimator xAnim = ObjectAnimator.ofInt(mFullImageView,
				"contentX", (containerWidth - mFitSizeBitmap.x) / 2)
				.setDuration(ANIM_DURATION);
		final ObjectAnimator yAnim = ObjectAnimator.ofInt(mFullImageView,
				"contentY", (containerHeight - mFitSizeBitmap.y) / 2)
				.setDuration(ANIM_DURATION);
		widthAnim.start();
		heightAnim.start();
		xAnim.start();
		yAnim.start();
		// TODO check why using AnimatorSet doesn't work here:
		// final com.nineoldandroids.animation.AnimatorSet set = new
		// AnimatorSet();
		// set.playTogether(widthAnim, heightAnim, xAnim, yAnim);
	}

	public static BitmapFactory.Options getBitmapOptions(final Resources res, final int resId) {
		final BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, bitmapOptions);
		return bitmapOptions;
	}

}
