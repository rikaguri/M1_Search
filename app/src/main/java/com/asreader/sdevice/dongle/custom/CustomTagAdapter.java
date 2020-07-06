package com.asreader.sdevice.dongle.custom;

import java.util.ArrayList;

import com.minireader.sdevice.rfid.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomTagAdapter extends ArrayAdapter<CellData>
{
    private int mResource;
    private ArrayList<CellData> mList;
    private LayoutInflater mInflater;

    public CustomTagAdapter(Context context, int layoutResource,
	    ArrayList<CellData> objects)
    {
	super(context, layoutResource, objects);
	this.mResource = layoutResource;
	this.mList = objects;
	this.mInflater = (LayoutInflater) context
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
	final CellData cell = mList.get(position);

	if (convertView == null)
	{
	    convertView = mInflater.inflate(mResource, null);
	}

	if (cell != null)
	{
	    TextView tTextView = (TextView) convertView
		    .findViewById(R.id.list_tag);
	    TextView cTextView = (TextView) convertView
		    .findViewById(R.id.list_count);
	    TextView rssiTextView = (TextView) convertView
			    .findViewById(R.id.list_rssi);
	    

	    tTextView.setText(cell.getName());
	    cTextView.setText(cell.getValue());
	    rssiTextView.setText(cell.getRssi());
	    rssiTextView.setText(cell.getRFM());

	}

	return convertView;

    }
}
