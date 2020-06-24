package com.minireader.sdevice.rfid;

import android.os.Bundle;
import com.asreader.event.IOnAsDeviceRfidEvent;
import com.asreader.sdevice.AsDeviceLib;
import com.asreader.sdevice.AsDeviceMngr;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class KillActivity extends Activity implements IOnAsDeviceRfidEvent
{
	public Byte resultCode = 0x00;

	private Button back;
	private Button done;

	private TextView kill_targetTag, kill_password;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kill);
		AsDeviceMngr.getInstance().setDelegateRFID(this);
		kill_targetTag = (TextView) findViewById(R.id.kill_targetTag);
		kill_targetTag.setText(TagAccessActivity.nowTag);

		kill_password = (TextView) findViewById(R.id.kill_pass);

		back = (Button) findViewById(R.id.kill_navigation_back_button);
		back.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				moveTaskToBack(false);
				finish();
			}
		});

		done = (Button) findViewById(R.id.kill_done_btn);
		done.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (AsDeviceMngr.getInstance().getOTG().isOpen())
				{
					try
					{
						String epc = kill_targetTag.getText().toString();

						
							AsDeviceMngr.getInstance().getOTG().killTag(
									Long.parseLong(kill_password.getText()
											.toString(), 16),
									AsDeviceLib.convertStringToByteArray(epc));
						

					}
					catch (Exception e)
					{
						Toast.makeText(KillActivity.this, e.toString(),
								Toast.LENGTH_SHORT).show();
					}
				}
				else
				{
					Toast.makeText(KillActivity.this, "Kill Tag successs",
							Toast.LENGTH_SHORT).show();
				}
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
		resultCode = 0x00;

		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() 
		{
			public void run()
			{	
				Toast.makeText(KillActivity.this, "Success",
						Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public void onFailureReceived(final int[] data)
	{
		resultCode = (byte)(data[0] & 0xFF);

		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() 
		{
			public void run()
			{	
				Toast.makeText(KillActivity.this, "Error: Error Code = 0x" + AsDeviceLib.byte2string((byte)(data[0]&0xff)),
						Toast.LENGTH_LONG).show();
			}
		});
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
	public void onBatteryStateReceived(final int dest ,final int nCharging){
		// TODO Auto-generated method stub

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
