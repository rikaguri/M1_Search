package com.minireader.sdevice.rfid;

import java.util.ArrayList;

import com.asreader.component.ListData;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


public class ListViewDialog extends Dialog 
{
	private ArrayList<ListData>   mListData = new ArrayList<ListData>();
	private ListViewDialogSelectListener mListener;
	private ListView mListView = null;
	private ListViewAdapter mAdapter = null;
	
	
	public ArrayAdapter<ListData> adapter;


	public void clearData()
	{
		synchronized (mListData) {
			mListData.clear();
			mAdapter.notifyDataSetChanged();
		}


	}


	public void AddData(Drawable icon,String strAddr,String strName)
	{
		synchronized (mListData) {
			boolean isNew = true;
			for(int i=0; i<mListData.size();i++)
			{
				ListData data = mListData.get(i);
				String strAdd = data.mAddr;

				if((strAdd.equals(strAddr))&&(data.mName.equals(strName)))
					isNew = false;
			}
			
			if(isNew)
			{
				mAdapter.addItem(icon,strAddr,strName);
				mAdapter.notifyDataSetChanged();
			}
		}

	}


	public void setTopBar(int nTop, int nLeft, int nRight)
	{
		LinearLayout lay = (LinearLayout) findViewById(R.id.top_bar);
		lay.setBackgroundResource(nTop);
	   
		 Button mBtnLeft = (Button) findViewById(R.id.btn_search);
		 Button mBtnRight = (Button) findViewById(R.id.btn_cancel);
		 if(nLeft!=0)
		 mBtnLeft.setBackgroundResource(nLeft);
		 
		 if(nRight!=0)
		 mBtnRight.setBackgroundResource(nRight);
		
	}
	
	
	public ListViewDialog(Context context,int nlayOut) 
	{
		super(context);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(nlayOut);		
		mListView = (ListView) findViewById(R.id.list);
		Context mContext = this.getContext();
		mAdapter = new ListViewAdapter(mContext);
		mListView.setAdapter(mAdapter);


		mListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
			{
				Log.i("Touch", "onItemClick");
				mListener.onSetOnItemClickListener(arg2);
			}
		});

	}


	public ListData getDataIndex(int index)
	{
		if(index>mListData.size()-1)
		{
			return null;
		}
		else
			return mListData.get(index);
	}

	private class ListViewAdapter extends BaseAdapter {

		private Context mContext = null;

		public ListViewAdapter(Context mContext) 
		{
			super();
			this.mContext = mContext;
		}

		public void addItem(Drawable icon, String mTitle, String mDate)
		{
			ListData addInfo = null;
			addInfo = new ListData();

			addInfo.mIcon = icon;
			addInfo.mAddr = mTitle;
			addInfo.mName = mDate;
			mListData.add(addInfo);
		}


		@Override
		public int getCount() {
			return mListData.size();
		}

		@Override
		public Object getItem(int position) {
			return mListData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}


		private class ViewHolder
		{
			public ImageView mIcon;
			public TextView mText;
			public TextView mDate;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			ViewHolder holder;
			if (convertView == null) 
			{
				holder = new ViewHolder();        
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.cell_ble, parent);
				holder.mIcon = (ImageView) convertView.findViewById(R.id.mImage);
				holder.mText = (TextView) convertView.findViewById(R.id.mText);
				holder.mDate = (TextView) convertView.findViewById(R.id.mDate);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}

			ListData mData = mListData.get(position);
			if (mData.mIcon != null) 
			{
				holder.mIcon.setVisibility(View.VISIBLE);
				holder.mIcon.setImageDrawable(mData.mIcon);
			}
			else
			{
				holder.mIcon.setVisibility(View.GONE);
			}
			holder.mText.setText(mData.mAddr);
			holder.mDate.setText(mData.mName);
			return convertView;
		}

	}


	/**
	 * listener
	 */
	public void onOnSetItemClickListener(ListViewDialogSelectListener listener)
	{
		mListener = listener;
	}


	/**
	 * interface...
	 */
	public interface ListViewDialogSelectListener
	{
		void onSetOnItemClickListener(int position);
	}
}