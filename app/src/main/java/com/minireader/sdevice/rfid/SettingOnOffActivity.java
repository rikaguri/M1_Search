package com.minireader.sdevice.rfid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.asreader.event.IOnAsDeviceRfidEvent;
import com.asreader.rfid.AsDeviceRfidFhLbtParam;
import com.asreader.sdevice.AsDeviceMngr;
import com.asreader.utility.EpcConverter;

public class SettingOnOffActivity extends Activity implements IOnAsDeviceRfidEvent
{
	private Button back;
	private SharedPreferences mPrefs = null;
	private ToggleButton swBeep;
	private ToggleButton swVib;
	private ToggleButton swLed;
	private ToggleButton swIllu;
	private ToggleButton swPowerOnBeep;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_onoff);


		mPrefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		back = (Button) findViewById(R.id.onoff_setting_navigation_back_button);
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



		swBeep = (ToggleButton) findViewById(R.id.sw_beep);
		swBeep.setChecked(mPrefs.getBoolean("READER_BEEP", true));
		swBeep.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				changeSetting();
			}
		});


		swVib = (ToggleButton) findViewById(R.id.sw_vibration);
		swVib.setChecked(mPrefs.getBoolean("READER_VIB", true));
		swVib.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				changeSetting();
			}
		});


		swLed = (ToggleButton) findViewById(R.id.sw_led);
		swLed.setChecked(mPrefs.getBoolean("READER_LED", true));
		swLed.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				changeSetting();
			}
		});


		swIllu = (ToggleButton) findViewById(R.id.sw_illumination);
		swIllu.setChecked(mPrefs.getBoolean("READER_ILLU", true));
		swIllu.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				changeSetting();
			}
		});


		swPowerOnBeep = (ToggleButton) findViewById(R.id.sw_power_on_beep);
		swPowerOnBeep.setChecked(mPrefs.getBoolean("READER_POWERONBEEP", true));
		swPowerOnBeep.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				changeSetting();
			}
		});
	}


	private void changeSetting()
	{
		boolean beep 		= false;
		boolean vib  		= false;
		boolean led  		= false;
		boolean illu 		= false;
		boolean powerOnBeep = false;

		/* not use mode */
		int mode = 0;


		if(swBeep.isChecked())
			beep = true;

		if(swVib.isChecked())
			vib = true;

		if(swLed.isChecked())
			led = true;

		if(swIllu.isChecked())
			illu = true;

		if(swPowerOnBeep.isChecked())
			powerOnBeep = true;


		AsDeviceMngr.getInstance().getOTG().setReaderOnOffSettings(beep,vib,led,illu);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingOnOffActivity.this);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("READER_BEEP", beep);
		editor.putBoolean("READER_VIB", vib);
		editor.putBoolean("READER_LED", led);
		editor.putBoolean("READER_ILLU", illu);
		editor.putBoolean("READER_POWERONBEEP", powerOnBeep);
		editor.commit();


	}


	@Override
	protected void onResume()
	{
		super.onResume();
		AsDeviceMngr.getInstance().setDelegateRFID(this);



	}


	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}



	@Override
	public void onReaderInfoReceived(final int nOnTime,final int nOffTime,final int nSensTiem,final int nLBTLevel,final int nFhEnable,final int nLbtEnable,final int nCWEnable,final int nPower,final int nMinPower, final int nMaxPower)
	{

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

	int nCh,nOff;
	@Override
	public void onChannelReceived(int channel, int channelOffset)
	{

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
	public void onSuccessReceived(final int[] data, final int commandCode)
	{
		ShowToast("Success");	
	}

	@Override
	public void onFailureReceived(final int[] data)
	{

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


	int nStateUpdate;
	@Override
	public void didUpdateRegistry(int state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReceiveAntimode(final int nMode, int nStart, int nMax, int nMin, int nCounter) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onTxPowerLevelReceived(int nPower,int nMinPower, int nMaxPower) {
		

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
