package com.minireader.sdevice.rfid;

import java.util.ArrayList;

import com.asreader.event.IOnAsDeviceRfidEvent;
import com.asreader.sdevice.AsDeviceMngr;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class SessionActivity extends Activity implements IOnAsDeviceRfidEvent
{
	private Button back;
	private Button done;
	private ListView outList;
	private ArrayAdapter<String> adapter;
	private ArrayList<String> array_list;
	private int mSession = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_session);

		array_list = new ArrayList<String>();

		for (int i = 0; i < 4; i++)
		{
			array_list.add("S"+Integer.toString(i));
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
				mSession = arg2;		
			}
		});	

		adapter.notifyDataSetChanged();

		done = (Button) findViewById(R.id.power_done_btn);
		done.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{		

				AsDeviceMngr.getInstance().getOTG().setSession(mSession);
				
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("SESSION", mSession);
				editor.commit();
				
				
				
			}
		});
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();

		AsDeviceMngr.getInstance().setDelegateRFID(this);

		AsDeviceMngr.getInstance().getOTG().getSession();
		


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
				Toast.makeText(SessionActivity.this, "Sucess.",
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
				Toast.makeText(SessionActivity.this, "Error: Error Code = " + data[0],
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
		//	System.out.println("session = " + session);
		runOnUiThread(new Runnable() 
		{
			public void run()
			{	

				outList.setItemChecked(session, true);
			}
		});
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