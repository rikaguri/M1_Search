package com.minireader.sdevice.rfid;

import android.os.Bundle;

import com.asreader.event.IOnAsDeviceRfidEvent;
import com.asreader.sdevice.AsDeviceMngr;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChannelActivity extends Activity implements IOnAsDeviceRfidEvent
{
	private Button back, done;
	private EditText text1;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channel);

		text1 = (EditText) findViewById(R.id.channel_editText);
		back = (Button) findViewById(R.id.stopconditions_navigation_back_button);
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

		done = (Button) findViewById(R.id.stop_done_btn);
		done.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (isNumber(text1.getText().toString()))
				{

					int nChannel = Integer.parseInt(text1.getText()
							.toString());

					Toast.makeText(ChannelActivity.this, "Set Channel",
							Toast.LENGTH_SHORT).show();

					AsDeviceMngr.getInstance().getOTG().setChannel(nChannel, 0);


				}
				else
				{
					Toast.makeText(ChannelActivity.this, "Error: Only integers allowed",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	public static boolean isNumber(String str)
	{
		boolean check = true;

		if (!str.isEmpty())
		{
			for (int i = 0; i < str.length(); i++)
			{
				if (!Character.isDigit(str.charAt(i)))
				{
					check = false;
					break;
				}
			}
			return check;
		}
		else
		{
			check = false;
			return check;
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		AsDeviceMngr.getInstance().setDelegateRFID(this);


		AsDeviceMngr.getInstance().getOTG().getChannel();	

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
		text1.setText(""+channel);

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
	public void onSuccessReceived(int[] data, final int commandCode)
	{
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() 
		{
			public void run()
			{	
				Toast.makeText(ChannelActivity.this, "Success",
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
				Toast.makeText(ChannelActivity.this, "Error: Error Code = " + data[0],
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
