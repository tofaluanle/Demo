package com.example.main;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

/**
 * 2013年10月25日 17:08:12
 * 
 * @author 洋葱
 * */
public class MyAdapter extends BaseAdapter {

	private List<String> arrays = null;
	private Context mContext;
	private float x, ux;

	public MyAdapter(Context mContext, List<String> arrays) {
		this.mContext = mContext;
		this.arrays = arrays;

	}

	public int getCount() {
		return this.arrays.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View view, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(R.layout.item, null);
			viewHolder.tvTitle = (TextView) view.findViewById(R.id.title);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		viewHolder.tvTitle.setText(this.arrays.get(position));

		return view;

	}

	final static class ViewHolder {
		TextView tvTitle;
	}
}