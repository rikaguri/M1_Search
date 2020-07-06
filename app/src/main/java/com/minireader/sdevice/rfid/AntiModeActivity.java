package com.minireader.sdevice.rfid;

import java.util.ArrayList;

import com.asreader.component.SegmentedRadioGroup;
import com.asreader.event.IOnAsDeviceRfidEvent;
import com.asreader.sdevice.AsDeviceMngr;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class AntiModeActivity extends Activity implements IOnAsDeviceRfidEvent,OnCheckedChangeListener
{
	private Button back;
	private Button done;
	private ListView outList;
	private ArrayAdapter<String> adapter;
	private ArrayList<String> array_list;
	private int mAntimode = 0;
	private TextView txtQueData;

	private LinearLayout layOutQue;
	private int m_nStart =4;
	private int m_nMax   =7;
	private int m_nMin   =2;

	private int m_nCounter = 0;
	private EditText mEditStart;
	private EditText mEditMax;
	private EditText mEditMin;
	private EditText mEditCounter;
	public void ShowToast(String msg)
	{
		final String strMsg = msg;
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				Toast.makeText(getApplicationContext(), strMsg,Toast.LENGTH_SHORT).show();
			}
		});

	}

	private SegmentedRadioGroup segment_Que;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anti);
		txtQueData   = (TextView) findViewById(R.id.txt_QueData);
		mEditStart   = (EditText) findViewById(R.id.edit_qstart);
		mEditMax     = (EditText) findViewById(R.id.edit_qmax);
		mEditMin     = (EditText) findViewById(R.id.edit_qmin);
		mEditCounter = (EditText) findViewById(R.id.edit_counter);
		layOutQue = (LinearLayout) findViewById(R.id.layoutQue);

		segment_Que = (SegmentedRadioGroup) findViewById(R.id.segment_que);
		segment_Que.setOnCheckedChangeListener(this);
		
		LinearLayout lay_QueMax  = (LinearLayout) findViewById(R.id.layoutQuMax);
		LinearLayout lay_QueMin  = (LinearLayout) findViewById(R.id.layoutQuMin);

		TextView txtQstart   = (TextView)findViewById(R.id.txt_counter);
		EditText editQstart  = (EditText)findViewById(R.id.edit_counter);

		array_list = new ArrayList<String>();
		for (int i = 0; i < 4; i++)
		{
			array_list.add("Mode"+ i);
		}

		adapter = new ArrayAdapter<String>(this, R.layout.cell_setting_check,array_list);

		back = (Button) findViewById(R.id.navigation_back_button);
		back.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				moveTaskToBack(false);
				finish();
			}
		});

		outList = (ListView) findViewById(R.id.type_listview1);
		outList.setAdapter(adapter);
		outList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		outList.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3)
			{
				mAntimode = arg2;		

			}
		});	

		adapter.notifyDataSetChanged();
		done = (Button) findViewById(R.id.power_done_btn);
		done.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{		
				m_nStart   = Integer.parseInt( mEditStart.getText().toString());
				m_nMax     = Integer.parseInt( mEditMax.getText().toString()); 
				m_nMin     = Integer.parseInt( mEditMin.getText().toString()); 
				m_nCounter = Integer.parseInt( mEditCounter.getText().toString()); 

				if(m_nCounter>255)
				{
					ShowToast("please Check Counter : 0 ~ 255");
					return;
				}

				AsDeviceMngr.getInstance().getOTG().setAnticollisionMode(mAntimode,m_nStart,m_nMax,m_nMin,m_nCounter);

				Toast.makeText(AntiModeActivity.this, "Try to change Setting  ... ",
						Toast.LENGTH_LONG).show();

				Handler handler04 = new Handler();
				handler04.postDelayed(new Runnable()
				{
					public void run() 
					{
						AsDeviceMngr.getInstance().getOTG().getAnticollisionMode();
					}
				}, 2000);
			}
		});
	}


	@Override
	public void onReceiveAntimode(final int nMode, final int nStart,final int nMax,final int nMin, final int nCounter) {
		runOnUiThread(new Runnable() 
		{
			public void run()
			{	
				outList.setItemChecked(nMode, true);
				txtQueData.setText("  Mode: "+nMode+" QStart: "+nStart+" QMax: "+nMax+" QMin: "+nMin);
				m_nStart =nStart;
				m_nMax   =nMax;
				m_nMin   =nMin;
				mEditStart.setText(""+nStart);
				mEditMax.setText(""+nMax);
				mEditMin.setText(""+nMin);
				mEditCounter.setText(""+nCounter);
				
				
				if(nMode == 0)
				{
					segment_Que.check( R.id.segment_fixed);
				}
				else if(nMode ==1)
				{
					segment_Que.check( R.id.segment_dynamic);
				}
				
			}
		});
	}



	@Override
	protected void onResume()
	{
		super.onResume();
		AsDeviceMngr.getInstance().setDelegateRFID(this);


		AsDeviceMngr.getInstance().getOTG().getAnticollisionMode();


		Handler handler04 = new Handler();
		handler04.postDelayed(new Runnable()
		{
			public void run() 
			{

				AsDeviceMngr.getInstance().getOTG().getAnticollisionMode();
			}
		}, 500);
	}



	@Override
	public void onReaderInfoReceived(int nOnTime,int nOffTime,int nSensTiem,int nLBTLevel,int nFhEnable,int nLbtEnable,int nCWEnable,int nPwer,int nMinPower, int nMaxPower)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onReceivedLeakageData(int i, int i1, int i2, int i3, int i4, int i5, int i6) {

	}

	@Override
	public void onRegionReceived(int region)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onSelectParamReceived(int[] data)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onQueryParamReceived(int[] data)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onChannelReceived(int channel, int channelOffset)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onFhLbtReceived(int nOnTime,int nOffTime,int nSensTime,int nLBTLevel,int nFhEnable,int nLbtEnable,int nCWEnable)
	{
		// TODO Auto-generated method stub

	}


	@Override
	public void onReceivedRFIDModuleVersion(String s) {

	}

	@Override
	public void onReaderAboutInfo(String s, String s1, String s2, byte b) {

	}

	@Override
	public void onResetReceived()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onSuccessReceived(int[] data, int code)
	{
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() 
		{
			public void run()
			{			
				Toast.makeText(AntiModeActivity.this, "Success",
						Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public void onFailureReceived(final int[] data)
	{
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() 
		{
			public void run()
			{	
				Toast.makeText(AntiModeActivity.this, "Error: Error Code = " + data[0],
						Toast.LENGTH_LONG).show();

			}
		});
	}

	@Override
	public void onBatteryStateReceived(final int dest ,final int nCharging)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onTagMemoryLongReceived(int[] dest)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onPcEpcSensorDataReceived(int[] ints, int[] ints1) {

	}


	@Override
	public void onTagMemoryReceived(int[] data)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onSessionReceived(final int session)
	{	


	}


	@Override
	public void didSetOptiFreqHPTable(int status) {
		// TODO Auto-generated method stub

	}

	@Override
	public void didSetSmartMode(int state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReceiveSmartMode(int state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void didUpdateRegistry(int state) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onTxPowerLevelReceived(int nPower,int nMinPower, int nMaxPower) {
		// TODO Auto-generated method stub

	}

	@Override
	public void didReceiveRegion(int state) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onTagReceived(int[] dest) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onTagWithTidReceived(int[] pcEpc, int[] tid) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onTagWithRssiReceived(int[] pcEpc, int rssi) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		if (checkedId == R.id.segment_fixed)
		{
			mAntimode = 0;
			layOutQue.setVisibility(View.GONE);
		}
		else if (checkedId == R.id.segment_dynamic)
		{
			mAntimode = 1;
			layOutQue.setVisibility(View.VISIBLE);
		}
	}


}
