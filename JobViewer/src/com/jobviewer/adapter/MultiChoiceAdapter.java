package com.jobviewer.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.lanesgroup.jobviewer.R;

public class MultiChoiceAdapter extends ArrayAdapter<MultiChoiceItem> {
	
	private List<MultiChoiceItem> list;
	private LayoutInflater inflator;

	public MultiChoiceAdapter(Context context, List<MultiChoiceItem> list) {
		super(context, R.layout.multichoice_row, list);
		this.setList(list);
		inflator = ((Activity)context).getLayoutInflater();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		if (convertView == null) {
			convertView = inflator.inflate(R.layout.multichoice_row, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.chk = (CheckBox) convertView.findViewById(R.id.checkbox);
			convertView.setTag(holder);
			convertView.setTag(R.id.title, holder.title);
			convertView.setTag(R.id.checkbox, holder.chk);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.chk.setTag(position);

		holder.title.setText(getList().get(position).getText());
		holder.chk.setChecked(getList().get(position).isChecked());

		return convertView;
	}

	public List<MultiChoiceItem> getList() {
		return list;
	}

	public void setList(List<MultiChoiceItem> list) {
		this.list = list;
	}

	static class ViewHolder {
		protected TextView title;
		protected CheckBox chk;
	}

	public void refresh(int position, MultiChoiceItem multiChoiceItem) {
		list.set(position, multiChoiceItem);
		notifyDataSetChanged();
		
	}

}
