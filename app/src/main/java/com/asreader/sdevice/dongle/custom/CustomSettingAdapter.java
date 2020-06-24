package com.asreader.sdevice.dongle.custom;

import java.util.ArrayList;

import com.minireader.sdevice.rfid.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomSettingAdapter extends ArrayAdapter<CellData>
{
	private int mResource;
	private ArrayList<CellData> mList;
	private LayoutInflater mInflater;

	public CustomSettingAdapter(Context context, int layoutResource,ArrayList<CellData> objects)
	{
		super(context, layoutResource, objects);
		this.mResource = layoutResource;
		this.mList = objects;
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		final CellData stag = mList.get(position);

		if (convertView == null)
		{
			convertView = mInflater.inflate(mResource, null);
		}

		if (stag != null)
		{
			TextView tTextView = (TextView) convertView
					.findViewById(R.id.popsettingname);
			TextView cTextView = (TextView) convertView
					.findViewById(R.id.popsettingvalue);

			tTextView.setText(stag.getName());
			cTextView.setText(stag.getValue());
		}
		
		return convertView;
	}
}