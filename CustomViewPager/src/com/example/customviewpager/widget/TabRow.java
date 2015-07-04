package com.example.customviewpager.widget;

import com.example.customviewpager.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.view.View;

public class TabRow extends HorizontalScrollView {

	private static final String TAG = "log";
	private static final boolean DEBUG = true;
	
	private LinearLayout.LayoutParams defaultTabLayoutParams;
	private LinearLayout.LayoutParams expandedTabLayoutParams;
	
	private LinearLayout mTabContainer;
	private Paint rectPaint;
	private Paint dividerPaint;
	
	private int dividerPadding = 12;
	private int indicatorHeight = 8;
	private int underlineHeight = 1;
	private int dividerWidth = 1;
	
	private int indicatorColor = 0xFF666666;
	private int underlineColor = 0x1A000000;
	private int dividerColor = 0x1A000000;
	
	private String[] mTabItems;
	
	private int mCurrentPosition = 0;
	private float mCurrentOffset = 0;
	private float mLastScrollX = 0;
	
	public TabOnClickListener mTabOnClickListener;
	public interface TabOnClickListener{
		public void onTabClick(int position);
	}
	
	public TabRow(Context context){ super(context, null); }
	public TabRow(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabRow);
		int itemsResId = a.getResourceId(R.styleable.TabRow_items, -1);
		
		mTabContainer = new LinearLayout(context);
		mTabContainer.setOrientation(LinearLayout.HORIZONTAL);
		mTabContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		addView(mTabContainer);
		setHorizontalScrollBarEnabled(false);
		setFillViewport(true);//true = fit screen width
		setWillNotDraw(false);

		defaultTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		expandedTabLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
		
		rectPaint = new Paint();
		rectPaint.setAntiAlias(true);
		rectPaint.setStyle(Style.FILL);

		dividerPaint = new Paint();
		dividerPaint.setAntiAlias(true);
		dividerPaint.setStrokeWidth(dividerWidth);

		if(itemsResId != -1){
			mTabItems = getResources().getStringArray(itemsResId);
			if(mTabItems.length > 0){
				for(int i=0;i<mTabItems.length;i++){
					addTextTab(i, mTabItems[i]);
				}
			}
		}
		
		a.recycle();
	}
	
	private void addTextTab(final int position, String title){
		TextView tab = new TextView(getContext());
		tab.setText(title);
		tab.setGravity(Gravity.CENTER);
		tab.setSingleLine();

		addTab(position, tab);
	}
	
	private void addTab(final int position, View tab){
		tab.setFocusable(true);
		tab.setPadding(25, 0, 25, 0);
		tab.setOnClickListener(new OnClickListener(){
			@Override public void onClick(View v) {
				if(mTabOnClickListener != null)
					mTabOnClickListener.onTabClick(position);
			}
		});
		
		mTabContainer.addView(tab, position, expandedTabLayoutParams);
	}
	
	public void setTabOnClickListener(TabOnClickListener l){
		mTabOnClickListener = l;
	}
	
	public void setTabSelected(int position, float offset){
		if(mTabContainer.getChildCount() == 0)
			return;
		
		if(DEBUG) Log.d(TAG, "Tab Offset : " + offset + ", position : " + (int)offset);
		
		mCurrentPosition = (int)offset;
		mCurrentOffset = (float) (offset - Math.floor(offset));
//		float newScrollX = mTabContainer.getChildAt(position).getWidth() * (position > 0 ? Math.abs(position - offset) : offset);
////		if (position > 0 || offset > 0) {
////			newScrollX -= position;
////		}
//		
//		if(newScrollX != mLastScrollX){
//			mLastScrollX = newScrollX;
////			scrollTo((int) 10 d0, 0);
//		}
		
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		final int height = mTabContainer.getHeight();
		final View child = mTabContainer.getChildAt(mCurrentPosition);
		if(child != null){
			float lineLeft = child.getLeft();
			float lineRight = child.getRight();
			if(mCurrentOffset > 0 && mCurrentPosition < mTabContainer.getChildCount() - 1){

				View nextTab = mTabContainer.getChildAt(mCurrentPosition + 1);
				final float nextTabLeft = nextTab.getLeft();
				final float nextTabRight = nextTab.getRight();
				
				if(DEBUG) Log.i(TAG, "onDraw() offset " + mCurrentOffset + ", position " + mCurrentPosition);
				
				lineLeft = (mCurrentOffset * nextTabLeft + (1f - mCurrentOffset) * lineLeft);
				lineRight = (mCurrentOffset * nextTabRight + (1f - mCurrentOffset) * lineRight);
			}
			
			rectPaint.setColor(indicatorColor);
			canvas.drawRect(lineLeft, height - indicatorHeight, lineRight, height, rectPaint);
		}
		
		rectPaint.setColor(underlineColor);
		canvas.drawRect(0, height - underlineHeight, mTabContainer.getWidth(), height , rectPaint);
		
		dividerPaint.setColor(dividerColor);
		for (int i = 0; i < mTabContainer.getChildCount() - 1; i++) {
			View tab = mTabContainer.getChildAt(i);
			canvas.drawLine(tab.getRight(), dividerPadding, tab.getRight(), height - dividerPadding, dividerPaint);
		}
	}
	
	
}
