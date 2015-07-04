package com.example.customviewpager.widget;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public abstract class AbstractWorkspace extends ViewGroup {

	private static final String TAG = "log";
	private static final boolean DEBUG = true;
	
	private static final boolean USE_CACHE = false;
	private static final int MAX_SETTLE_DURATION = 600; // ms
	private static final int MIN_DISTANCE_FOR_FLING = 35; // dips
	private static final Interpolator sInterpolator = new Interpolator() {
		public float getInterpolation(float t) {
			t -= 1.0f;
			return t * t * t * t * t + 1.0f;
		}
	};
	
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	
	private static final int INVALID_POINTER = -1;
	private static final int INVALID_INDEX = -1;
	
	Runnable mThisRunnable = new Runnable(){
		@Override
		public void run() {
			final View child = getChildAt(mCurrentIndex);
			if(child != null)
				child.dispatchDisplayHint(View.VISIBLE);
		}
	};
	
	private boolean mAllowLongPress;
	private boolean mSmoothScrollEnabled = true;
	private boolean mIsUnableToDrag = false;
	private boolean mFirstLayout = true;
	private boolean mScrollEnabled = false;
	
	private int mCurrentIndex = INVALID_INDEX;
	private int mPageIndex;
	
	protected VelocityTracker mVelocityTracker;
	protected int mActivePointerId = INVALID_POINTER;
	
	private Scroller mScroller;
	
	private float mInitialMotionX;
	private float mLastMotionX;
	private float mLastMotionY;
	
	private int mTouchState = TOUCH_STATE_REST;
	private int mTouchSlop;
	private int mPageSlop;
	private int mDragOver = 50;
	private int mMaximumVelocity;
	private int mMinimumVelocity;
	private int mFlingDistance;
	
	public abstract void setTabSelected(int position, float offset);
	
	public AbstractWorkspace(Context context) { this(context, null); };
	public AbstractWorkspace(Context context, AttributeSet attrs){ 
		super(context, attrs, 0); 
		setHapticFeedbackEnabled(false);
		setHorizontalFadingEdgeEnabled(false);
		
		//new OvershootInterpolator()
		mScroller = new Scroller(context, sInterpolator);
		mPageIndex = 0;
		
		ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
		mTouchSlop = viewConfiguration.getScaledTouchSlop();
		mPageSlop = viewConfiguration.getScaledPagingTouchSlop();
		mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
		mMinimumVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
		
		final float density = context.getResources().getDisplayMetrics().density;
		mFlingDistance = (int) (MIN_DISTANCE_FOR_FLING * density);
	}
	
	///////////////////////////// make child click able active onInterceptTouchEvent /////////////////////////////
	@Override
	public void addView(View child) {
		child.setClickable(true);
		super.addView(child);
	}

	@Override
	public void addView(View child, int index) {
		child.setClickable(true);
		super.addView(child, index);
	}

	@Override
	public void addView(View child, int width, int height) {
		child.setClickable(true);
		super.addView(child, width, height);
	}

	@Override
	public void addView(View child, LayoutParams params) {
		child.setClickable(true);
		super.addView(child, params);
	}

	@Override
	public void addView(View child, int index, LayoutParams params) {
		child.setClickable(true);
		super.addView(child, index, params);
	}

	///////////////////////////// Click or Keyboard on focus fix /////////////////////////////
	
	
	@Override
	protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
		if(DEBUG) Log.i(TAG, "onRequestFocusInDescendants()");
		if(mCurrentIndex != INVALID_INDEX){
			mCurrentIndex = mPageIndex;
			View view = getChildAt(mPageIndex);
			if(view != null)
				view.requestFocus(direction, previouslyFocusedRect);
		}
		return false;
	}
	
	@Override
	public int getVerticalFadingEdgeLength() {
		return 0;
	}

	@Override
	public int getHorizontalFadingEdgeLength() {
		return 0;
	}

	@Override
	public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
		if(DEBUG) Log.i(TAG, "addFocusables()");
		super.addFocusables(views, direction, focusableMode);
	}

	@Override
	public void focusableViewAvailable(View v) {
		if(DEBUG) Log.i(TAG, "focusableViewAvailable()");
		super.focusableViewAvailable(v);
	}
	
	@Override
	public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
		if(DEBUG) Log.i(TAG, "requestChildRectangleOnScreen()");
		return super.requestChildRectangleOnScreen(child, rectangle, immediate);
	}

	@Override
	public boolean dispatchUnhandledMove(View focused, int direction) {
		if(DEBUG) Log.i(TAG, "dispatchUnhandledMove()");
		return super.dispatchUnhandledMove(focused, direction);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
//		if(DEBUG) Log.i(TAG, "dispatchDraw()");
		boolean fastDraw = mTouchState != TOUCH_STATE_SCROLLING && mCurrentIndex == INVALID_INDEX;
		if(fastDraw){
			if(getChildAt(mPageIndex) != null){
				drawChild(canvas, getChildAt(mPageIndex), getDrawingTime());
			}
		}else{
			final long drawingTime = getDrawingTime();
			if(mCurrentIndex > 0 && mCurrentIndex < getChildCount() && Math.abs(mPageIndex - mCurrentIndex) == 1){
//				if(DEBUG) Log.v(TAG, "Draw has previous child.");
				drawChild(canvas, getChildAt(mPageIndex), drawingTime);
				drawChild(canvas, getChildAt(mCurrentIndex), drawingTime);
			}else{
//				if(DEBUG) Log.v(TAG, "only do drawing.");
				// If we are scrolling, draw all of our children
                final int count = getChildCount();
                for (int i = 0; i < count; i++) {
                    drawChild(canvas, getChildAt(i), drawingTime);
                }
			}
		}
	}
	///////////////////////////// Click or Keyboard on focus fix /////////////////////////////
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if(DEBUG) Log.d(TAG, "onSizeChanged()");
		super.onSizeChanged(w, h, oldw, oldh);
		setCurrentScreen(mPageIndex);
		setTabSelected(mPageIndex, getCurrentScreenFraction());
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int total = getChildCount();
		int index = 0;
		int width = 0;
		
		while(index < total){
			View child = getChildAt(index);
			if(child.getVisibility() != View.GONE){
				int childWidth = child.getMeasuredWidth();
				child.layout(width, 0, width + childWidth, child.getMeasuredHeight());
				width += childWidth;
			}
			index++;
		}
		
		mScrollEnabled = true;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		if(View.MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY 
				|| View.MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY)
			throw new IllegalStateException("CustomViewPager can only be used in EXACTLY mode.");
		
		int width = View.MeasureSpec.getSize(widthMeasureSpec);
		int total = getChildCount();
		int index = 0;
		
		while(index < total){
			getChildAt(index).measure(widthMeasureSpec, heightMeasureSpec);
			index++;
		}
		
		if(mFirstLayout){
			setHorizontalScrollBarEnabled(false);
			scrollTo(width * mPageIndex, 0);
			setHorizontalScrollBarEnabled(true);
			mFirstLayout = false;
		}
	}
	
	/*
	 * children must be set child.setClickable(true) -> onInterceptTouchEvent() default return false
	 * or manual set onInterceptTouchEvent() return false
	 * parent's onInterceptTouchEvent() : ACTION_MOVE it can be use
	 * flow: parent:onintercept -> child:onintercept-> parent:ontouch->child:ontouch
	 */
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
//		if(DEBUG) Log.v(TAG, "onInterceptTouchEvent()");
		if(getChildCount() == 0 || mIsUnableToDrag)
			return false;
		
		final int action = ev.getAction();
		//avoid get new x/y again.
		if(action == MotionEvent.ACTION_MOVE && mTouchState == TOUCH_STATE_SCROLLING)
			return true;
		
		switch(action & MotionEventCompat.ACTION_MASK){
			case MotionEvent.ACTION_DOWN:
				if(DEBUG) Log.v(TAG, "onInterceptTouchEvent() - ACTION_DOWN");
					int index = MotionEventCompat.getActionIndex(ev);
					mActivePointerId = MotionEventCompat.getPointerId(ev, index);
					if(mActivePointerId == INVALID_POINTER){
						break;
					}
					
					mAllowLongPress = true;
					
					mLastMotionX = mInitialMotionX = MotionEventCompat.getX(ev, index);
					mLastMotionY = MotionEventCompat.getY(ev, index);
					mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
				
				break;
			case MotionEvent.ACTION_MOVE:{
				if(mTouchState != TOUCH_STATE_SCROLLING){
					final int activePointerId = ev.findPointerIndex(mActivePointerId);
					final int pointerIndex = getPointerIndex(ev, activePointerId);
					if(activePointerId == INVALID_POINTER || pointerIndex == INVALID_POINTER)
						break;
					
					final float x = MotionEventCompat.getX(ev, pointerIndex);
					final float y = MotionEventCompat.getY(ev, pointerIndex);
					final int xDiff = (int) Math.abs(x - mLastMotionX);
					final int yDiff = (int) Math.abs(y - mLastMotionY);
					if(xDiff > mDragOver){
//						boolean xPaged = xDiff > mPageSlop;
						boolean xMoved = xDiff > mTouchSlop;
						boolean yMoved = yDiff > mTouchSlop;
						
						if(xMoved || yMoved){
//							if(xPaged){
								if(xMoved && !yMoved){
									mTouchState = TOUCH_STATE_SCROLLING;
									mLastMotionX = x;
								}
//							}
						}
					}
					
					if(mAllowLongPress){
						mAllowLongPress = false;
						final View currentScreen = getChildAt(mPageIndex);
						if(currentScreen != null){
							currentScreen.cancelLongPress();
						}
					}

				}
				break;
			}
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				mTouchState = TOUCH_STATE_REST;
				mAllowLongPress = false;
	            mActivePointerId = INVALID_POINTER;
	            if (mVelocityTracker != null) {
	    			mVelocityTracker.recycle();
	    			mVelocityTracker = null;
	    		}
				break;
			case MotionEventCompat.ACTION_POINTER_UP:
				onSecondaryPointerUp(ev);
				break;
		}
		
		if(mVelocityTracker == null)
			mVelocityTracker = VelocityTracker.obtain();
		
		mVelocityTracker.addMovement(ev);

		return mTouchState != TOUCH_STATE_REST;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
//		if(DEBUG) Log.v(TAG, "onTouchEvent()");
		if(mIsUnableToDrag)
			return false;
		
		final int action = ev.getAction();

		if(mVelocityTracker == null)
			mVelocityTracker = VelocityTracker.obtain();
		
		mVelocityTracker.addMovement(ev);

		switch(action & MotionEventCompat.ACTION_MASK){
			case MotionEvent.ACTION_DOWN:
				if (!mScroller.isFinished())
		              mScroller.abortAnimation();

				int index = MotionEventCompat.getActionIndex(ev);
				mActivePointerId = MotionEventCompat.getPointerId(ev, index);
				mLastMotionX = mInitialMotionX = MotionEventCompat.getX(ev, index);
				break;
			case MotionEvent.ACTION_MOVE:
				if(mTouchState != TOUCH_STATE_REST){
					final int activePointerId = mActivePointerId;
					final int poinerIndex = getPointerIndex(ev, activePointerId);
					if(activePointerId == INVALID_POINTER || poinerIndex == INVALID_POINTER)
						break;
					final float x = MotionEventCompat.getX(ev, poinerIndex);
					final float deltaX = mLastMotionX - x;
					final int maxScrollX = getChildAt(getChildCount() - 1).getRight() - getScrollX() - getWidth();
					mLastMotionX = x;

					if(deltaX < 0 && getScrollX() > 0){
						scrollBy((int) Math.max(-getScrollX(), deltaX), 0);
					}else if(deltaX > 0 && maxScrollX > 0){
						scrollBy((int) Math.min(maxScrollX, deltaX), 0);
					}
					
					awakenScrollBars();
					setTabSelected(mPageIndex, getCurrentScreenFraction());
				}
				break;
			case MotionEvent.ACTION_UP:
				if(mTouchState == TOUCH_STATE_SCROLLING){
					if(mActivePointerId != INVALID_POINTER){
						final float x = MotionEventCompat.getX(ev, mActivePointerId);
						final VelocityTracker velocityTracker = mVelocityTracker;
						velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
						int velocity = (int) velocityTracker.getXVelocity();
						
						if(Math.abs(mInitialMotionX - x) > mFlingDistance && Math.abs(velocity) > mMinimumVelocity){
							if(velocity > 500 && mPageIndex > 0){
								setCurrentPage(mPageIndex - 1, velocity);
							}else if(velocity < -500 && mPageIndex < getChildCount() - 1){
								setCurrentPage(mPageIndex + 1, velocity);
							}else{
								snapToDestination();
							}
						}else{
							snapToDestination();
						}

						if(mVelocityTracker != null){
							mVelocityTracker.recycle();
							mVelocityTracker = null;
						}
					}
				}

				mTouchState = TOUCH_STATE_REST;
				break;
			case MotionEventCompat.ACTION_POINTER_DOWN: {
				final int indexx = MotionEventCompat.getActionIndex(ev);
				mLastMotionX = MotionEventCompat.getX(ev, indexx);
				mActivePointerId = MotionEventCompat.getPointerId(ev, indexx);
				break;
			}
			case MotionEventCompat.ACTION_POINTER_UP:
				onSecondaryPointerUp(ev);
				break;
			case MotionEvent.ACTION_CANCEL:
				mActivePointerId = INVALID_POINTER;
		    	mTouchState = TOUCH_STATE_REST;
		}

		return true;
	}

	private void snapToDestination(){
		if(DEBUG) Log.v(TAG, "snapToDestination");
		final int screenWidth = getWidth();
		final int whichScreen = (getScrollX() + (screenWidth / 2)) / screenWidth;
		setCurrentPage(whichScreen, 0);
	}
	
	float getCurrentScreenFraction() {
        final int scrollX = getScrollX();
        final int screenWidth = getWidth();
       	return (float) scrollX / screenWidth;
    }
	
	@Override
	public void computeScroll() {
		if(mScroller.computeScrollOffset()){ //scrolling animation not yet finish
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			setTabSelected(mPageIndex, getCurrentScreenFraction());
			postInvalidate(); //important
		}else if(mCurrentIndex != INVALID_INDEX){
			mPageIndex = mCurrentIndex;
			mCurrentIndex = INVALID_INDEX;
		}
		
	}
	
	public void setCurrentScreen(int index){
		if(!mScroller.isFinished())
			mScroller.abortAnimation();
		
		mPageIndex = Math.max(index, Math.min(mCurrentIndex, getChildCount() - 1));
		scrollTo(mPageIndex * getWidth(), 0);
//		setTabRow(index);
		invalidate();
	}
	
	public void setCurrentPage(int index, int velocity){
//		if(DEBUG) Log.d(TAG, "setCurrentPage()");
		int whichScreen = Math.max(0, Math.min(index, getChildCount() - 1));
		int screenDelta = Math.abs(whichScreen - mPageIndex);
		mCurrentIndex = whichScreen;
		
		final int width = getWidth();
		final int newX = whichScreen * width;
		final int oldScrollX = getScrollX();
		final int newLeft = newX - oldScrollX; //differ scroll x
		final int halfWidth = width / 2;
		final float distanceRatio = Math.min(1f, 1.0f * Math.abs(newLeft) / width);
		final float distance = halfWidth + halfWidth * distanceInfluenceForSnapDuration(distanceRatio);
		int duration = 0;
		
		View view = getFocusedChild();
		
		if((view != null) && (screenDelta != 0) && (view == getChildAt(mPageIndex))){	
			view.clearFocus();
		}
		
		velocity = Math.abs(velocity);
		if(mSmoothScrollEnabled){
			if(velocity > 0){
				duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
			}else{
//			final float pageDelta = (float) Math.abs(newLeft) / width;
//			duration = (int) ((pageDelta + 1) * 100);
//			duration = MAX_SETTLE_DURATION;
				duration = screenDelta * 300;
				if(duration == 0)
					duration = Math.abs(newLeft) * 2;
			}
			awakenScrollBars(duration);
		}
		
		duration = Math.min(duration, MAX_SETTLE_DURATION);
		
		if(mCurrentIndex != mPageIndex && mCurrentIndex != INVALID_INDEX){
			getChildAt(mPageIndex).dispatchDisplayHint(View.INVISIBLE);
			removeCallbacks(mThisRunnable);
			postDelayed(mThisRunnable, duration + 10);
		}
		
		if(!mScroller.isFinished())
			mScroller.abortAnimation();
	
		mScroller.startScroll(oldScrollX, 0, newLeft, 0, duration);
		invalidate();
	}
	
	private int getPointerIndex(MotionEvent ev, int id){
		int activePointerId = MotionEventCompat.findPointerIndex(ev, id);
		if(activePointerId == -1)
			mActivePointerId = INVALID_POINTER;
		return activePointerId;
	}
	
	private void onSecondaryPointerUp(MotionEvent ev) {
		final int index = (MotionEventCompat.ACTION_POINTER_INDEX_MASK & ev.getAction()) >> MotionEventCompat.ACTION_POINTER_INDEX_SHIFT;
		final int pointerId = MotionEventCompat.getPointerId(ev, index);
		if (pointerId == mActivePointerId) {
			// This was our active pointer going up. Choose a new
			// active pointer and adjust accordingly.
			final int newPointerIndex = index == 0 ? 1 : 0;
			mLastMotionX = mInitialMotionX = ev.getX(newPointerIndex);
			mActivePointerId = ev.getPointerId(newPointerIndex);
			if (mVelocityTracker != null) {
				mVelocityTracker.clear();
			}
		}
	}

	// We want the duration of the page snap animation to be influenced by the distance that
	// the screen has to travel, however, we don't want this duration to be effected in a
	// purely linear fashion. Instead, we use this method to moderate the effect that the distance
	// of travel has on the overall snap duration.
	float distanceInfluenceForSnapDuration(float f) {
		f -= 0.5f; // center the values about 0.
		f *= 0.3f * Math.PI / 2.0f;
		return (float) FloatMath.sin(f);
	}

}
