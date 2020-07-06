package com.minireader.sdevice.rfid;

import com.asreader.component.SegmentedRadioGroup;
import com.asreader.event.IOnAsDeviceRfidEvent;
import com.asreader.event.IOnHandlerMessage;
import com.asreader.sdevice.AsDeviceMngr;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class HoppingView extends Activity implements  IOnAsDeviceRfidEvent,IOnHandlerMessage, android.widget.RadioGroup.OnCheckedChangeListener
{
	private TextView txtHoppingInfo;	

	private int readtime; 
	private int idletime;
	private int sensetime;
	private int lbtlevel;
	private int fhmode;
	private int lbtmode;
	private int cwmode;
	private boolean bSamrtMode;
	private RelativeLayout mLay_channel;
	private SegmentedRadioGroup segment_hopping;
	private EditText mEditChannel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hopping);
		bSamrtMode = false;
		mLay_channel   = (RelativeLayout) findViewById(R.id.lay_channel);
		txtHoppingInfo = (TextView) findViewById(R.id.txt_hopping);
		segment_hopping = (SegmentedRadioGroup) findViewById(R.id.segment_hoppingSelect);
		segment_hopping.setOnCheckedChangeListener(this);
		mEditChannel = (EditText) findViewById(R.id.edit_channel);
		AsDeviceMngr.getInstance().setDelegateRFID(this);
		mLay_channel.setVisibility(View.GONE);
	}


	private void updateInfomation()
	{
		txtHoppingInfo.setText("");


		AsDeviceMngr.getInstance().getOTG().getFrequencyHoppingMode();


		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		AsDeviceMngr.getInstance().getOTG().getFhLbtParam();



		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		AsDeviceMngr.getInstance().getOTG().getChannel();




	}

	public void onClickevent(View v)
	{
		int nID = v.getId();

		switch(nID)
		{
		case R.id.popsetting_navigation_back_button:
		{
			onBackPressed();
		}
		break;

		case R.id.btn_saveData:
		{
			AsDeviceMngr.getInstance().getOTG().updateRegistry();

		}
		break;
		case R.id.btn_getInfo:
		{
			updateInfomation();
		}

		break;

		case R.id.btn_optimum:
		{
			AsDeviceMngr.getInstance().getOTG().setOptiFreqHoppingTable();

		}
		break;

		case R.id.btn_setSmart:
		{
			ToggleButton btn = (ToggleButton) findViewById(R.id.btn_smart);
			AsDeviceMngr.getInstance().getOTG().setSmartHoppingOnOff(btn.isChecked());

		}
		break;

		case R.id.btn_channel:
		{
			int nChannel = Integer.parseInt(mEditChannel.getText().toString());

			AsDeviceMngr.getInstance().getOTG().setChannel(nChannel, 0);

		}
		break;

		case R.id.btn_setHoppingMode:
		{
			if(m_nLBT == -1)
			{
				Toast.makeText(getApplicationContext(), "Please Select FH Mode", Toast.LENGTH_SHORT).show();
				return;
			}

			AsDeviceMngr.getInstance().getOTG().setFhLbtParam(readtime, idletime, sensetime, lbtlevel,m_nFH,m_nLBT,0);

			updateInfomation();
		}
		break;

		}

	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		updateInfomation();
	}

	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
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

	}

	@Override
	public void onSuccessReceived(final int[] data, int commandCode) 
	{
		runOnUiThread(new Runnable() 
		{
			public void run()
			{	
				if(data[0] == 0x00)
					Toast.makeText(HoppingView.this, "Success",
							Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public void onFailureReceived(int[] errCode) {
		switch(errCode[1])
		{
		case 0xE4:
		case 0xE6:
		case 0xD2:
		{
			runOnUiThread(new Runnable()
			{
				public void run()
				{
					Toast.makeText(getApplicationContext(), "The retrun loss of antenna is too large to optimize channel.", Toast.LENGTH_SHORT).show();
				}
			});
		}
		break;
		default:
			break;
		}

	}



	@Override
	public void onReaderInfoReceived(int nOnTime,int nOffTime,int nSensTiem,int nLBTLevel,int nFhEnable,int nLbtEnable,int nCWEnable,int nPwer,int nMinPower, int nMaxPower) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReceivedLeakageData(int i, int i1, int i2, int i3, int i4, int i5, int i6) {

	}

	@Override
	public void onBatteryStateReceived(final int dest ,final int nCharging){
		// TODO Auto-generated method stub

	}



	@Override
	public void onRegionReceived(int region) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSelectParamReceived(int[] selParam) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onQueryParamReceived(int[] data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onChannelReceived(final int channel, int channelOffset) {


		runOnUiThread(new Runnable() 
		{
			public void run()
			{
				mEditChannel.setText(Integer.toString(channel));
			}
		});

	}

	String strIngo;
	@Override
	public void onFhLbtReceived(int nOnTime,int nOffTime,int nSensTime,int nLBTLevel,int nFhEnable,int nLbtEnable,int nCWEnable) {
		// TODO Auto-generated method stub
		//00 64 01 F4 00 05 FD 1C 01 02 00 
		readtime  = nOnTime;
		idletime  = nOffTime;
		sensetime =nSensTime;
	

		lbtlevel  =  nLBTLevel;

		fhmode    = nFhEnable;
		lbtmode   = nLbtEnable;
		cwmode    = 0;

		if(bSamrtMode)
		{
			strIngo = "R:"+readtime
					+"\nI:"+idletime
					+"\nS:"+sensetime
					+"\nLbtL:"+lbtlevel
					+"\nFh:"+fhmode
					+"\nlbt:"+lbtmode
					+"\nCw:"+cwmode
					+"\nSamrtMode : ON";		
		}
		else
		{
			strIngo = "R:"+readtime
					+"\nI:"+idletime
					+"\nS:"+sensetime
					+"\nLbtL:"+lbtlevel
					+"\nFh:"+fhmode
					+"\nlbt:"+lbtmode
					+"\nCw:"+cwmode
					+"\nSamrtMode : OFF";
		}

		runOnUiThread(new Runnable()
		{
			public void run()
			{

				mLay_channel.setVisibility(View.GONE);
				if(fhmode>lbtmode)
				{
					segment_hopping.check(R.id.segment_FH);
					m_nFH = 2;
					m_nLBT = 1;

				}
				if(fhmode<lbtmode)
				{
					segment_hopping.check(R.id.segment_LBT);
					m_nFH = 2;
					m_nLBT = 1;
				}

				if(fhmode==lbtmode)
				{
					segment_hopping.check(R.id.segment_AllOff);
					m_nFH = 0;
					m_nLBT = 0;
					mLay_channel.setVisibility(View.VISIBLE);
				}



				txtHoppingInfo = (TextView) findViewById(R.id.txt_hopping);
				txtHoppingInfo.setText(strIngo);

			}
		});

	}

	int m_nFH  = -1;
	int m_nLBT = -1;


	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		if (checkedId == R.id.segment_FH)
		{
			m_nFH  = 2;
			m_nLBT = 1;
		}
		else if (checkedId == R.id.segment_LBT)
		{
			m_nFH  = 1;
			m_nLBT = 2;
		}
		else if (checkedId == R.id.segment_AllOff)
		{
			m_nFH  = 0;
			m_nLBT = 0;
		}

	}


	@Override
	public void onTagMemoryReceived(int[] data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTagMemoryLongReceived(int[] dest) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPcEpcSensorDataReceived(int[] ints, int[] ints1) {

	}


	@Override
	public void onSessionReceived(int session) {
		// TODO Auto-generated method stub

	}


	@Override
	public void handlerMessage(Message msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void didSetOptiFreqHPTable(int status) {
		if(status == 0)
		{
			runOnUiThread(new Runnable()
			{
				public void run()
				{
					Toast.makeText(getApplicationContext(), "Wait!! Start OptiMum Freq Start", Toast.LENGTH_SHORT).show();

				}
			});
		}

		if(status == 1)
		{
			runOnUiThread(new Runnable()
			{
				public void run()
				{
					Toast.makeText(getApplicationContext(), "Finish  OptiMum Freq", Toast.LENGTH_SHORT).show();

				}
			});
		}


	}

	@Override
	public void didSetSmartMode(int state) {
		// TODO Auto-generated method stub
		if(state ==0)
			runOnUiThread(new Runnable()
			{
				public void run()
				{
					Toast.makeText(getApplicationContext(), "Success FH Mode Setting ", Toast.LENGTH_SHORT).show();

					AsDeviceMngr.getInstance().getOTG().getFrequencyHoppingMode();


				}
			});
		else
			runOnUiThread(new Runnable()
			{
				public void run()
				{
					Toast.makeText(getApplicationContext(), "Fail FH Mode Setting ", Toast.LENGTH_SHORT).show();
				}
			});
	}

	@Override
	public void onReceiveSmartMode(int state) {
		// TODO Auto-generated method stub
		bSamrtMode = state != 0;


		if(bSamrtMode)
		{
			strIngo = "R:"+readtime
					+"\nI:"+idletime
					+"\nSensTime:"+sensetime
					+"\nLbtL:"+lbtlevel
					+"\nFh:"+fhmode
					+"\nlbt:"+lbtmode
					+"\nCw:"+cwmode
					+"\nSamrtMode : ON";		
		}
		else
		{
			strIngo = "R:"+readtime
					+"\nI:"+idletime
					+"\nSensTime:"+sensetime
					+"\nLbtL:"+lbtlevel
					+"\nFh:"+fhmode
					+"\nlbt:"+lbtmode
					+"\nCw:"+cwmode
					+"\nSamrtMode : OFF";
		}

		runOnUiThread(new Runnable()
		{
			public void run()
			{
				txtHoppingInfo = (TextView) findViewById(R.id.txt_hopping);
				txtHoppingInfo.setText(strIngo);

				ToggleButton btn = (ToggleButton) findViewById(R.id.btn_smart);
				if(bSamrtMode)
					btn.setChecked(true);
				else
					btn.setChecked(false);


			}
		});


	}

	@Override
	public void didUpdateRegistry(int state) {
		// TODO Auto-generated method stub
		if(state == 0)
			runOnUiThread(new Runnable()
			{
				public void run()
				{
					Toast.makeText(getApplicationContext(), "Success Update Registry", Toast.LENGTH_SHORT).show();
				}
			});

	}


	@Override
	public void onReceiveAntimode(int nMode, int nStart, int nMax, int nMin, int nCounter) {
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




}
