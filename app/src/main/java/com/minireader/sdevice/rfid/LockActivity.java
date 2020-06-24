package com.minireader.sdevice.rfid;

import com.asreader.component.SegmentedRadioGroup;
import com.asreader.event.IOnAsDeviceRfidEvent;
import com.asreader.sdevice.AsDeviceLib;
import com.asreader.sdevice.AsDeviceMngr;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class LockActivity extends Activity implements OnCheckedChangeListener, IOnAsDeviceRfidEvent
{
	public Byte resultCode = 0x00;

	private Button back;
	private Button done;

	private SegmentedRadioGroup segment_TagMemory;
	private SegmentedRadioGroup segment_Action;

	private TextView lock_targetTag, lock_accessPass;

	private int seed = 0;
	private int targetMemory = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lock);

		AsDeviceMngr.getInstance().setDelegateRFID(this);

		segment_TagMemory = (SegmentedRadioGroup) findViewById(R.id.seg_group_three);
		segment_TagMemory.setOnCheckedChangeListener(this);
		segment_Action = (SegmentedRadioGroup) findViewById(R.id.seg_group_four);
		segment_Action.setOnCheckedChangeListener(this);

		lock_targetTag = (TextView) findViewById(R.id.lock_targetTag);
		lock_targetTag.setText(TagAccessActivity.nowTag);

		lock_accessPass = (TextView) findViewById(R.id.lock_accessPass);

		back = (Button) findViewById(R.id.lock_navigation_back_button);
		back.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				moveTaskToBack(false);
				finish();
			}
		});

		done = (Button) findViewById(R.id.lock_done_btn);
		done.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if (AsDeviceMngr.getInstance().getOTG().isOpen())
				{
					try
					{
						String epc = lock_targetTag.getText().toString();
						int lock = 0;			
						switch(targetMemory)
						{
						case 0:
							lock = (seed << 8) | (3 << 18);
							break;
						case 1:
							lock = (seed << 6) | (3 << 16);
							break;
						case 2:
							lock = (seed << 4) | (3 << 14);
							break;
						case 3:
							lock = (seed << 2) | (3 << 12);
							break;
						case 4:
							lock = (seed << 0) | (3 << 10);
							break;
						}


						AsDeviceMngr.getInstance().getOTG().lockTagMemory(
									Long.parseLong(lock_accessPass.getText()
											.toString(), 16),
									AsDeviceLib.convertStringToByteArray(epc),
									lock);
						

					}
					catch (Exception e)
					{

					}
				}
				else
				{
					Toast.makeText(LockActivity.this, "lock Tag successs",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	public void onCheckedChanged(RadioGroup group, int checkedId)
	{
		if (group == segment_TagMemory)
		{
			switch(checkedId)
			{
			case R.id.segment_kill:
				targetMemory = 0;
				break;
			case R.id.segment_acs:
				targetMemory = 1;
				break;
			case R.id.segment_epc:
				targetMemory = 2;
				break;
			case R.id.segment_tid:
				targetMemory = 3;
				break;
			case R.id.segment_user:
				targetMemory = 4;
				break;
			}   	

		}
		else if (group == segment_Action)
		{
			switch(checkedId)
			{
			case R.id.segment_unlock:
				seed = 0;
				break;
			case R.id.segment_punlock:
				seed = 1;
				break;
			case R.id.segment_lock:
				seed = 2;
				break;
			case R.id.segment_plock:
				seed = 3;
				break;		
			}

		}
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
	public void onTagMemoryReceived(int[] data)
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
		resultCode = 0x00;

		runOnUiThread(new Runnable() 
		{
			public void run()
			{	
				Toast.makeText(LockActivity.this, "Success",
						Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public void onFailureReceived(final int[] data)
	{	
		resultCode = (byte)(data[0] & 0xFF);

		runOnUiThread(new Runnable() 
		{
			public void run()
			{	
				Toast.makeText(LockActivity.this, "Error: Error Code = 0x" + AsDeviceLib.byte2string((byte)(data[0]&0xff)),
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
	public void onSessionReceived(int channel)
	{
		// TODO Auto-generated method stub

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
