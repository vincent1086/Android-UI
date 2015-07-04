package com.example.customviewpager;

import com.example.customviewpager.widget.TabRow;
import com.example.customviewpager.widget.TabRow.TabOnClickListener;
import com.example.customviewpager.widget.Workspace;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

public class MainActivity extends Activity implements TabOnClickListener{

	private Workspace mWorkspace;
	private TabRow mTabRow;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		Log.i("log", "1f=" + dipTopx(this, 100f));
		
		mWorkspace = (Workspace)findViewById(R.id.workspace);
		mTabRow = (TabRow)findViewById(R.id.tab);
		mTabRow.setTabOnClickListener(this);
		mWorkspace.setTabRow(mTabRow);
	}

	public static int dipTopx(Activity activity, float dipValue){ 
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
	    final float scale = dm.scaledDensity; 
	    return (int)(dipValue * scale + 0.5f); 
	}

	@Override
	public void onTabClick(int position) {
		mWorkspace.setCurrentPage(position, 0);
	}
}
