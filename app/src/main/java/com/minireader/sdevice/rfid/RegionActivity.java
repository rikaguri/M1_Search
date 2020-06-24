package com.minireader.sdevice.rfid;

import java.util.ArrayList;

import com.asreader.component.SegmentedRadioGroup;
import com.asreader.event.IOnAsDeviceRfidEvent;
import com.asreader.sdevice.AsDeviceMngr;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class RegionActivity extends Activity implements IOnAsDeviceRfidEvent
{
	private Button back;
	private Button done;

	private int nSelectedRegion = -1;

	final int REGION_KOREA_OLD	    =	0x01;
	final int REGION_USA_OLD 		=	0x02;
	final int REGION_EUROPE_OLD		=	0x04;
	final int REGION_JAPAN_OLD		=	0x08;
	final int REGION_CHINA2_OLD		=	0x10;
	final int REGION_CHINA1_OLD		=	0x16;
	final int REGION_USA2_OLD		=	0x32;

	final int REGION_KOREA			=	0x11;
	final int REGION_USA 			=	0x21;
	final int REGION_USA2	 		=	0x22;
	final int REGION_EUROPE			=	0x31;
	final int REGION_JAPAN			=	0x41;
	final int REGION_CHINA1			=	0x51;
	final int REGION_CHINA2			=	0x52;
	final int REGION_BRAZIL1        =   0x61;
	final int REGION_BRAZIL2        =   0x62;
	final int REGION_AU_HK			=	0x71;



	private ListView outList;
	private ArrayAdapter<String> adapter;
	private ArrayList<String> array_list;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_region);
		array_list = new ArrayList<String>();





		back = (Button) findViewById(R.id.admin_navigation_back_button);
		back.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				moveTaskToBack(false);
				finish();
			}
		});

		array_list.add("REGION_KOREA");	
		array_list.add("REGION_USA");
		array_list.add("REGION_USA2");
		array_list.add("REGION_EUROPE");
		array_list.add("REGION_JAPAN");
		array_list.add("REGION_CHINA1");
		array_list.add("REGION_CHINA2");
		array_list.add("REGION_BRAZIL1");
		array_list.add("REGION_BRAZIL2");
		array_list.add("REGION_AU_HK");

		adapter = new ArrayAdapter<String>(this, R.layout.cell_setting_check,array_list);


		outList = (ListView) findViewById(R.id.list_region);
		outList.setAdapter(adapter);
		outList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		outList.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3)
			{
				nSelectedRegion = arg2;
			}
		});

		outList.setItemChecked(nSelectedRegion, true);
		adapter.notifyDataSetChanged();
		
		done = (Button) findViewById(R.id.btn_setRegion);
		done.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Log.i("Region","Done"+ nSelectedRegion);
				if(nSelectedRegion == -1)
				{
					Toast.makeText(getApplicationContext(), "Not selected Region ",Toast.LENGTH_SHORT).show();
					return;
				}
				
				int nRegion = 0;
				
			
				
				switch(nSelectedRegion)
				{
					case 0:
						nRegion = 0x11;
						
						break;
						
					case 1:
						nRegion = 0x21;
						break;
						
					case 2:
						nRegion = 0x22;
						break;
					case 3:
						nRegion = 0x31;
						break;
					case 4:
						nRegion = 0x41;
						break;
					case 5:
						nRegion = 0x51;
						break;
					case 6:
						nRegion = 0x52;
						break;
					case 7:
						nRegion = 0x61;
						break;
					case 8:
						nRegion = 0x62;
						break;
					case 9:
						nRegion = 0x71;
						break;

						
				}

				AsDeviceMngr.getInstance().getOTG().setRegion(nRegion);
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("REGION", nRegion);
				editor.commit();
				

			}
		});

	}



	String strBand = "None";
	@Override
	public void onRegionReceived(final int region) 
	{
		//		- US FCC    (0x21)
		//		- US Narrow (0x22)
		//		- Europe (0x31) 
		//		- Japan (0x41)

		switch (region)
		{
		case REGION_KOREA_OLD:
		case REGION_KOREA:
			strBand = "KOREA";
			break;
		case REGION_USA_OLD:
			strBand = "US_OLD";
			break;
		case REGION_USA:
			strBand = "US1";
			break;
		case REGION_USA2:
			strBand = "US2";
			break;
		case REGION_EUROPE_OLD:
			strBand = "EU_OLD";
			break;
		case REGION_EUROPE:
			strBand = "EU";
			break;
		case REGION_JAPAN_OLD:
			strBand = "APAN_OLD";
			break;
		case REGION_JAPAN:
			strBand = "JAPAN";
			break;
		case REGION_CHINA2_OLD:
		case REGION_CHINA1_OLD:
		case REGION_CHINA1:
		case REGION_CHINA2:
			strBand = "CHINA";
			break;
		case REGION_AU_HK:
			strBand = "ASIA";
			break;
		default:
			strBand = "Unkown";
			break;
		}
		runOnUiThread(new Runnable()
		{
			public void run()
			{ 
				TextView txtregion = (TextView) findViewById(R.id.txt_region);

				txtregion.setText("Region : "+strBand);

			}
		});


	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		AsDeviceMngr.getInstance().setDelegateRFID(this);
		nSelectedRegion = -1;
		AsDeviceMngr.getInstance().getOTG().getRegion();
		AsDeviceMngr.getInstance().getOTG().getRegion();

	}


	@Override
	public void onReceivedRFIDModuleVersion(String s) {

	}

	@Override
	public void onReaderAboutInfo(String s, String s1, String s2, byte b) {

	}

	@Override
	public void onResetReceived() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSuccessReceived(int[] data, int commandCode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFailureReceived(int[] errCode) {
		// TODO Auto-generated method stub

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
	public void onReceiveAntimode(int nMode, int nStart, int nMax, int nMin, int nCounter) {
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
	public void didUpdateRegistry(final int state) {
		runOnUiThread(new Runnable()
		{
			public void run()
			{ 
				if(state == 0)
				{

				}
				else
				{
					Toast.makeText(getApplicationContext(), "Fail Update Registry", Toast.LENGTH_SHORT).show();
				}
			}
		});

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
	public void onChannelReceived(int channel, int channelOffset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFhLbtReceived(int nOnTime,int nOffTime,int nSensTime,int nLBTLevel,int nFhEnable,int nLbtEnable,int nCWEnable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTxPowerLevelReceived(int nPower,int nMinPower, int nMaxPower) {
		// TODO Auto-generated method stub

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
	public void didReceiveRegion(final int state) 
	{	
		Log.i("Region", " Region   " +state);
		runOnUiThread(new Runnable()
		{
			public void run()
			{ 
				if(state == 0)
				{
					AsDeviceMngr.getInstance().getOTG().updateRegistry();	
					Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();

					AsDeviceMngr.getInstance().getOTG().updateRegistry();

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					AsDeviceMngr.getInstance().getOTG().getRegion();
					AsDeviceMngr.getInstance().getOTG().getRegion();
					
					AlertDialog.Builder alert = new AlertDialog.Builder(RegionActivity.this);
					alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();     //닫기
						}
					});
					alert.setMessage("Region is changed \n The RFID Reader must be reset(detach -> reconnect)");
					alert.show();	

				}
				else
				{
					Toast.makeText(getApplicationContext(), "Fail Set Region", Toast.LENGTH_SHORT).show();
				}
			}
		});
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
