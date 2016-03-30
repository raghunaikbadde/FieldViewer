package com.jobviewer.util;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.lanesgroup.jobviewer.R;

public class CustomDialogAdapter extends BaseAdapter {
	
	private Context mContext;
	private String[]  mData;
	private String mLastSelectedValue;

	public CustomDialogAdapter(Context context, String[] data,String lastSelectedValue) {
		this.mContext = context;
		this.mData = data;
		this.mLastSelectedValue = lastSelectedValue;
	}
	
	@Override
	public int getCount() {
		return mData.length;
	}

	@Override
	public Object getItem(int position) {
		return mData[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	 @Override
     public int getViewTypeCount() {                 
         return mData.length;
     }

     @Override
     public int getItemViewType(int position) {
         return position;
     }

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder = null;
		LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.activity_list_row, null);
			holder = new ViewHolder();
			holder.mCheckbox = (CheckBox) convertView.findViewById(R.id.checkBox1);
			holder.mTextView = (TextView) convertView.findViewById(R.id.activity_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mTextView.setText(mData[position]);
		return convertView;
	}
	
	private class ViewHolder {
		
		CheckBox mCheckbox;
		TextView mTextView;
	}
}
=======
package com.jobviewer.util;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.lanesgroup.jobviewer.R;

public class CustomDialogAdapter extends BaseAdapter {
	
	private Context mContext;
	private String[]  mData;
	private String mLastSelectedValue;

	public CustomDialogAdapter(Context context, String[] data,String lastSelectedValue) {
		this.mContext = context;
		this.mData = data;
		this.mLastSelectedValue = lastSelectedValue;
	}
	
	@Override
	public int getCount() {
		return mData.length;
	}

	@Override
	public Object getItem(int position) {
		return mData[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	 @Override
     public int getViewTypeCount() {                 
         return mData.length;
     }

     @Override
     public int getItemViewType(int position) {
         return position;
     }

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder = null;
		LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.activity_list_row, null);
			holder = new ViewHolder();
			holder.mCheckbox = (CheckBox) convertView.findViewById(R.id.checkBox1);
			holder.mTextView = (TextView) convertView.findViewById(R.id.activity_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mTextView.setText(mData[position]);
		return convertView;
	}
	
	private class ViewHolder {
		
		CheckBox mCheckbox;
		TextView mTextView;
	}
}
