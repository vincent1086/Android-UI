package com.example.instagramlistview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private ListView mListView;
	private List<HashMap<String, String>> mListData = new ArrayList<HashMap<String, String>>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		
		mListView = (ListView)findViewById(R.id.listView);
		for(int i=0;i<10;i++){
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("title", "Title " + i);
			map.put("content", "content " + i);
			mListData.add(map);
		}
		
		CustomAdapter adapter = new CustomAdapter(this, mListData);
		mListView.setAdapter(adapter);
		
		mListView.setOnScrollListener(new OnScrollListener(){
			
			int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mScrollState = scrollState;
			}

			@Override
			public void onScroll(AbsListView list, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if(mScrollState == OnScrollListener.SCROLL_STATE_IDLE)
					return;
				
				// the listview has only few children (of course according to
				// the height of each child) who are visible
				for (int i = 0; i < list.getChildCount(); i++) {
					View child = list.getChildAt(i);
					ViewHolder holder = (ViewHolder) child.getTag();

					// if the view is the first item at the top we will do some
					// processing
					if (i == 0) {
						boolean isAtBottom = child.getHeight() <= holder.header.getBottom();
						int offset = holder.previousTop - child.getTop();
						if (!(isAtBottom && offset > 0)) {
							holder.previousTop = child.getTop();
							holder.header.offsetTopAndBottom(offset);
							holder.header.invalidate();
						}
					} // if the view is not the first item it "may" need some
						// correction because of view re-use
					else if (holder.header.getTop() != 0) {
						int offset = -1 * holder.header.getTop();
						holder.header.offsetTopAndBottom(offset);
						holder.previousTop = 0;
						holder.header.invalidate();
					}
				}
				
			}
		});
		
	}

	private class CustomAdapter extends BaseAdapter {

		private Context mContext;
		private List<HashMap<String, String>> mList;
		
		public CustomAdapter(Context context, List<HashMap<String, String>> list){
			mContext = context;
			mList = list;
		}
		
		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder mHolder;
			if(convertView == null){
				mHolder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_row, null);
				mHolder.header = (TextView)convertView.findViewById(R.id.header);
				mHolder.content = (TextView)convertView.findViewById(R.id.content);
				convertView.setTag(mHolder);
			}else{
				mHolder = (ViewHolder)convertView.getTag();
			}
			
			HashMap<String, String> map = mList.get(position);
			
			mHolder.header.setText(map.get("title"));
			mHolder.content.setText(map.get("content"));
			
			return convertView;
		}
	
	}
	
	private class ViewHolder{
		private TextView header;
		private TextView content;
		private int previousTop;
	}
}
