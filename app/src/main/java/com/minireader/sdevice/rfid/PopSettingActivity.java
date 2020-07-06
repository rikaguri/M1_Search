package com.minireader.sdevice.rfid;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import com.asreader.event.IOnAsDeviceRfidEvent;
import com.asreader.event.IOnHandlerMessage;
import com.asreader.rfid.AsDeviceRfidFhLbtParam;
import com.asreader.sdevice.AsDeviceMngr;
import com.asreader.utility.EpcConverter;
import com.asreader.utility.WeakRefHandler;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class PopSettingActivity extends Activity implements IOnAsDeviceRfidEvent
{
	private Button back,btnUpdate;
	private TextView tvEncodingType;
	private TextView tvOutputPower;
	private TextView tvOnOffTime;
	private TextView tvAntiMode;
	private TextView tvChannel;
	private TextView tvStopCondition;
	static int tx_minpower, tx_maxpower, powerLevel;
	static AsDeviceRfidFhLbtParam param;
	private int mTimerError = 0;
	private boolean mGetInfo = false;
	private SharedPreferences mPrefs = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		RelativeLayout lay_OnOff   = (RelativeLayout) findViewById(R.id.on_off_time_layout);
		RelativeLayout lay_Channle = (RelativeLayout) findViewById(R.id.channel_layout);

		mPrefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		back = (Button) findViewById(R.id.popsetting_navigation_back_button);
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
		btnUpdate =  (Button) findViewById(R.id.btn_update);
		btnUpdate.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AsDeviceMngr.getInstance().getOTG().updateRegistry();	
			}
		});

		param = new AsDeviceRfidFhLbtParam();

		tvAntiMode   = (TextView) findViewById(R.id.txt_Anti);
		tvChannel    = (TextView) findViewById(R.id.txt_channel);
		tvOutputPower = (TextView) findViewById(R.id.output_power);

		final RelativeLayout channelLayout 
		= (RelativeLayout) findViewById(R.id.channel_layout);	
		channelLayout.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getBaseContext(),
						ChannelActivity.class);
				startActivity(intent);
			}   
		});


		final RelativeLayout outputPowerLayout 
		= (RelativeLayout) findViewById(R.id.output_power_layout);	
		outputPowerLayout.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getBaseContext(),
						OutPutPowerActivity.class);
				startActivity(intent);
			}   
		});

		tvOnOffTime = (TextView) findViewById(R.id.on_off_time);	
		final RelativeLayout onOffLayout = (RelativeLayout) findViewById(R.id.on_off_time_layout);
		onOffLayout.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getBaseContext(),
						OnOffTimeActivity.class);
				intent.putExtra("value", tvOnOffTime.getText());
				startActivity(intent);
			}
		});

		tvStopCondition = (TextView) findViewById(R.id.stop_contidion);	
		final RelativeLayout stopConditionLayout 
		= (RelativeLayout) findViewById(R.id.stop_contidion_layout);
		stopConditionLayout.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getBaseContext(),
						StopCondisionsActivity.class);
				startActivity(intent);
			}
		});

		tvEncodingType = (TextView) findViewById(R.id.encoding_type);	
		final RelativeLayout encodingLayout = (RelativeLayout) findViewById(R.id.encoding_type_layout);
		encodingLayout.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getBaseContext(),
						EncodingTypeActivity.class);
				startActivity(intent);
			}
		});

		//tvSession = (TextView) findViewById(R.id.session);	
		final RelativeLayout sessionLayout = (RelativeLayout) findViewById(R.id.session_layout);
		sessionLayout.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getBaseContext(),
						SessionActivity.class);
				startActivity(intent);
			}
		});
		
		
		
		
		final RelativeLayout AntiLayout = (RelativeLayout) findViewById(R.id.anti_layout);
		AntiLayout.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getBaseContext(),AntiModeActivity.class);
				intent.putExtra("value", tvAntiMode.getText());
				startActivity(intent);
			}
		});
		
		
		ToggleButton rssiSet = (ToggleButton) findViewById(R.id.appsetting_rssi);
		rssiSet.setChecked(mPrefs.getBoolean("DISPLAY_RSSI", false));
		rssiSet.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				boolean rssi = false;
				ToggleButton rssiSet = (ToggleButton) findViewById(R.id.appsetting_rssi);
				rssi = rssiSet.isChecked();
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(PopSettingActivity.this);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean("DISPLAY_RSSI", rssi);
				editor.commit();
			}
		});

		AsDeviceMngr.getInstance().getOTG().getReaderInfo(0xB0);

	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		tvOutputPower.setText("");
		tvOnOffTime.setText("");
		tvAntiMode.setText("");
		tvChannel.setText("");

		AsDeviceMngr.getInstance().setDelegateRFID(this);
		tvStopCondition.setText(mPrefs.getInt("MAX_TAG", 0) + "/"
				+ mPrefs.getInt("MAX_TIME", 0) + "/"
				+ mPrefs.getInt("REPEAT_CYCLE", 0));

		tvEncodingType.setText(EpcConverter.toTypeString(mPrefs.getInt("ENCODING_TYPE", 0)));

		mGetInfo = false;
		mTimerError = 0;
		AsDeviceMngr.getInstance().getOTG().getReaderInfo(0xB0);
	}


	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}



	@Override
	public void onReaderInfoReceived(final int nOnTime,final int nOffTime,final int nSensTiem,final int nLBTLevel,final int nFhEnable,final int nLbtEnable,final int nCWEnable,final int nPower,final int nMinPower, final int nMaxPower)
	{
		mGetInfo = true;
		param.readtime  = nOnTime;
		param.idletime  = nOffTime;
		param.sensetime = nSensTiem;
		param.lbtlevel  = nLBTLevel;
		param.fhmode    = nFhEnable;
		param.lbtmode   = nLbtEnable;
		param.cwmode    = nCWEnable;

		powerLevel  = nPower;
		tx_minpower = nMinPower;
		tx_maxpower = nMaxPower;
		
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				tvOutputPower.setText(""+nPower/10.0);		
				tvOnOffTime.setText(nOnTime + "/"+nOffTime);		    
			}
		});


		AsDeviceMngr.getInstance().getOTG().getAnticollisionMode();


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
		nCh = channel;
		nOff = channelOffset;
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				tvChannel.setText("ch"+nCh+"off:"+nOff);
			}
		});
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
		nStateUpdate = state;
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				if(nStateUpdate == 0)
				{
					Toast.makeText(PopSettingActivity.this, "Success Data Store into Registry",
							Toast.LENGTH_SHORT).show();
				}
				else
				{
					Toast.makeText(PopSettingActivity.this, "Fail Store into Registry",
							Toast.LENGTH_SHORT).show();
				}

			}
		});
	}

	@Override
	public void onReceiveAntimode(final int nMode, int nStart, int nMax, int nMin, int nCounter) {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				tvAntiMode.setText(""+nMode);
			}
		});
		AsDeviceMngr.getInstance().getOTG().getChannel();
	}





	@Override
	public void onTxPowerLevelReceived(int nPower,int nMinPower, int nMaxPower) {
		
		powerLevel  = nPower;
		tx_minpower = nMinPower;
		tx_maxpower = nMaxPower;

		runOnUiThread(new Runnable()
		{
			public void run()
			{
				float fPower = (float)(powerLevel/10.0);
				tvOutputPower.setText(""+fPower);
			}
		});
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
