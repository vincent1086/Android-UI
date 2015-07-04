package com.example.customviewpager.widget;

import com.example.customviewpager.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;

public class Workspace extends AbstractWorkspace {
	
	private static final String TAG = "log";
	private static final boolean DEBUG = true;
	
	private TabRow mTabRow;
//	private int mTabResId = -1;
	
	public Workspace(Context context) { super(context); }
	public Workspace(Context context, AttributeSet attrs){
		super(context, attrs);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Workspace);
//		mTabResId = a.getResourceId(R.styleable.Workspace_tab, -1);
		a.recycle();
	}
	
	public void setTabRow(TabRow tab){
		if(mTabRow == null)
			mTabRow = tab;
	}

	@Override
	public void setTabSelected(int position, float offset) {
		if(mTabRow != null) 
			mTabRow.setTabSelected(position, offset);
	}
}
