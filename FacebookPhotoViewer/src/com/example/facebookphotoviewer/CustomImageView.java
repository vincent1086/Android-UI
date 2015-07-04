package com.example.facebookphotoviewer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;

public class CustomImageView extends ImageView {
	
	public CustomImageView(final Context context) {
		super(context);
	}

	public CustomImageView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setContentHeight(final int contentHeight) {
		final LayoutParams layoutParams = getLayoutParams();
		layoutParams.height = contentHeight;
		setLayoutParams(layoutParams);
	}

	public void setContentWidth(final int contentWidth) {
		final LayoutParams layoutParams = getLayoutParams();
		layoutParams.width = contentWidth;
		setLayoutParams(layoutParams);
	}

	public int getContentHeight() {
		return getLayoutParams().height;
	}

	public int getContentWidth() {
		return getLayoutParams().width;
	}

	public int getContentX() {
		return ((MarginLayoutParams) getLayoutParams()).leftMargin;
	}

	public void setContentX(final int contentX) {
		final MarginLayoutParams layoutParams = (MarginLayoutParams) getLayoutParams();
		layoutParams.leftMargin = contentX;
		setLayoutParams(layoutParams);
	}

	public int getContentY() {
		return ((MarginLayoutParams) getLayoutParams()).topMargin;
	}

	public void setContentY(final int contentY) {
		final MarginLayoutParams layoutParams = (MarginLayoutParams) getLayoutParams();
		layoutParams.topMargin = contentY;
		setLayoutParams(layoutParams);
	}
}
