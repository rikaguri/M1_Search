package com.minireader.sdevice.rfid;


import android.os.Bundle;

import com.asreader.event.IOnAsDeviceRfidEvent;
import com.asreader.rfid.AsDeviceRfidFhLbtParam;
import com.asreader.sdevice.AsDeviceMngr;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.TextView;
import android.widget.Toast;

public class OnOffTimeActivity extends Activity implements IOnAsDeviceRfidEvent
{
	Button back, done;
	EditText edit_text1, edit_text2;
	AsDeviceRfidFhLbtParam param;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_on_off_time);
		String value = getIntent().getStringExtra("value");
		
		
		edit_text1 = (EditText) findViewById(R.id.onofftimetext);
		edit_text2 = (EditText) findViewById(R.id.onofftimetext2);
		
		if((value!=null)&&(value.length()>0))
		{
			edit_text1.setText(value.substring(0, value.indexOf("/")));
			edit_text2.setText(value.substring(value.indexOf("/") + 1));
			
		}
		else
			Toast.makeText(getApplicationContext(), "Not found RFID's data(On/Off Time)", Toast.LENGTH_SHORT).show();
		
		this.param = PopSettingActivity.param;

		back = (Button) findViewById(R.id.onofftime_navigation_back_button);
		back.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				moveTaskToBack(false);
				finish();
			}
		});

		done = (Button) findViewById(R.id.onoff_done_btn);
		done.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				param.readtime = Integer.parseInt(edit_text1.getText()
						.toString());
				param.idletime = Integer.parseInt(edit_text2.getText()
						.toString());



				AsDeviceMngr.getInstance().getOTG().setFhLbtParam(
							param.readtime,
							param.idletime,
							param.sensetime,
							param.lbtlevel,
							param.fhmode,
							param.lbtmode,
							param.cwmode);
				

				Toast.makeText(OnOffTimeActivity.this, "Success",
						Toast.LENGTH_SHORT).show();
			}
		});
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
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() 
		{
			public void run()
			{	
				Toast.makeText(OnOffTimeActivity.this, "Success",
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
				Toast.makeText(OnOffTimeActivity.this, "Error: Error Code = " + data[0],
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
