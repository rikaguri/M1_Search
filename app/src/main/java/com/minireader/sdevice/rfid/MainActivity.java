package com.minireader.sdevice.rfid;
import java.util.ArrayList;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import com.asreader.common.AsDeviceConst;
import com.asreader.event.IOnAsDeviceRfidEvent;
import com.asreader.event.IOnOtgEvent;
import com.asreader.sdevice.AsDeviceLib;
import com.asreader.sdevice.AsDeviceMngr;
import com.asreader.sdevice.AsDeviceRfid;
import com.asreader.sdevice.dongle.custom.*;
import com.asreader.util.SensorTag;
import com.asreader.util.Utils;
import com.asreader.utility.EpcConverter;
import com.asreader.utility.Logger;

public class MainActivity extends AppCompatActivity implements IOnAsDeviceRfidEvent,IOnOtgEvent
{
	private final String TAG = "MainActivity";
	private final int df_NOT_INVALID_RSSI = 10;
	private final String df_NOT_INVALID_RFM  = "nRFM";
	private int encoding_type = EpcConverter.HEX_STRING;
	private int max_tag      = 0;//maximum number of tags to read
	private int max_time     = 0;//maximum elapsed time to read tags(sec)
	private int repeat_cycle = 0;//how many times reader performs inventory round


	private ListView epclist;
	private ListView epclist_empty;
	private TextView tvTagCount, tvDeviceStatus, tvBattery;

	private ArrayList<CellData>   tagCellList   = new ArrayList<CellData>();
	private ArrayList<String>     tagStringList = new ArrayList<String>();
	private ArrayList<CellData>   tagArrayEmpty = new ArrayList<CellData>();
	private ArrayList<TagData>    tagDataList   = new ArrayList<TagData>();


	private CustomTagAdapter tagAdaterEmpty;
	private CustomTagAdapter tagAdapter;
	private SharedPreferences prefs = null;
	private boolean bIsHomeButtonPress = true;

	private SharedPreferences mPrefs = null;

	private ScreenOnReceiver screenOnReceiver =null;

	ToggleButton setPower;

	Button option ,clearScreen, stopAutoRead, btn_ext_read, btn_ext_rfm, btn_battery;



	public class TagData
	{
		public int[] mData;
		public int m_nCount;
		public String m_strData;
	}

	class ScreenOnReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_SCREEN_ON)) {
			}
			else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				buttonControl(4);
				// AsDeviceMngr.getInstance().getOTG().stopReadTags();
				AsDeviceMngr.getInstance().getOTG().setRfidPowerOn(false);
			}
		}
	}

	private void regScreeOffEvent()
	{
		if(screenOnReceiver == null)
		{
			screenOnReceiver = new ScreenOnReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction("android.intent.action.SCREEN_ON");
			filter.addAction("android.intent.action.SCREEN_OFF");
			registerReceiver(screenOnReceiver, filter);
		}
	}


	//Activityの初期化
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		option       = (Button) findViewById(R.id.option);
		clearScreen  = (Button) findViewById(R.id.btn_clear);
		stopAutoRead = (Button) findViewById(R.id.btn_stop);
		epclist        = (ListView) findViewById(R.id.tag_list);
		tvTagCount     = (TextView) findViewById(R.id.name);
		tvDeviceStatus = (TextView) findViewById(R.id.aboutvalue);
		tvBattery      = (TextView) findViewById(R.id.textView3);

		// Read Tag List View event
		tagAdapter     = new CustomTagAdapter(this,R.layout.cell_tag, tagCellList);

		epclist.setAdapter(tagAdapter);

		//Add Empty Tag Cell
		epclist_empty = (ListView) findViewById(R.id.tag_list_empty);
		tagAdaterEmpty = new CustomTagAdapter(this,
				R.layout.cell_tag_empty, tagArrayEmpty);
		for (int i = 0; i < 20; i++)
		{
			tagAdaterEmpty.add(new CellData());
		}
		epclist_empty.setAdapter(tagAdaterEmpty);
		epclist_empty.setEnabled(false);

		// RFM Button (Sensor Tag)
		btn_ext_rfm = (Button) findViewById(R.id.btn_ext_rfm);
		btn_ext_rfm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				buttonControl(2);
				AsDeviceMngr.getInstance().getOTG().startReadTagsRFM(0x03,max_tag,max_time,repeat_cycle);
			}
		});
		btn_ext_rfm.setEnabled(false);

		// Battery
		btn_battery  = (Button) findViewById(R.id.btn_battery);
		btn_battery.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AsDeviceMngr.getInstance().getOTG().getBattery();
			}
		});
		btn_battery.setEnabled(false);

		//Read Tag Button
		btn_ext_read = (Button) findViewById(R.id.btn_ext_read);
		btn_ext_read.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				buttonControl(1);
				boolean rssiOn = prefs.getBoolean("DISPLAY_RSSI", false);
				AsDeviceMngr.getInstance().getOTG().startReadTags(max_tag, max_time, repeat_cycle,rssiOn);
			}
		});
		btn_ext_read.setEnabled(false);


		// Stop button
		stopAutoRead = (Button) findViewById(R.id.btn_stop);
		stopAutoRead.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				buttonControl(0);
				AsDeviceMngr.getInstance().getOTG().stopReadTags();
			}
		});
		stopAutoRead.setEnabled(false);

		// Clear button
		clearScreen = (Button) findViewById(R.id.btn_clear);
		clearScreen.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ListClear();
			}
		});
		clearScreen.setEnabled(false);

		// More Button
		option = (Button) findViewById(R.id.option);
		option.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if (AsDeviceMngr.getInstance().getOTG().isOpen())
				{
					bIsHomeButtonPress = false;

					AsDeviceMngr.getInstance().getOTG().stopReadTags();
					Intent intent = new Intent(getBaseContext(),
							OptionActivity.class);
					intent.putExtra("TAG_LIST", tagStringList);
					startActivity(intent);
				}
				else
				{
					Intent intent = new Intent(getBaseContext(),
							OptionActivity.class);
					intent.putExtra("TAG_LIST", tagStringList);
					startActivity(intent);
					ShowToast("More not open");
				}
			}
		});


		setPower = (ToggleButton) findViewById(R.id.power_onoff);
		setPower.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(setPower.isChecked()){
					setPowerOn();
				}
				else{
					buttonControl(4);
					ShowToast("Power OFF");
					AsDeviceMngr.getInstance().getOTG().setRfidPowerOn(false);
				}
			}
		});


		initial();

		/* SDK , APP Info */
		TextView txtTitle  = (TextView) findViewById(R.id.titlebar_text);
		txtTitle.setText("A3XD");


		TextView txtSubTitle  = (TextView) findViewById(R.id.txt_sub_title);
		txtSubTitle.setText("APP: "+Utils.getInstance().getAppVersion(this)+" SDK: "+AsDeviceConst.strLibVersion);
		regScreeOffEvent();

		/*Enable save Log file  */
		/*Log folder dir : /RFIDLOG/log.csv*/
		Logger.getInstance().enableCSVLogFileName("A3XD",true);

		/* type : 0 RX 1: TX 2: Event */
		Logger.getInstance().addLogData(2,"Start Log : onCreate");


		//以下にBottomNavigationViewのコード
		BottomNavigationView bottom_navigation=(BottomNavigationView)findViewById(R.id.bottom);//下部分のナビゲーション作成
		BottomNavigationView.OnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){

		@Override
		public boolean onNavigationItemSelected(@NonNull MenuItem item){
			Fragment selectedFragment =null;
			switch (item.getItemId()) {
				case R.id.item1:
					selectedFragment = ItemOneFragment.newInstance();
					break;
				case R.id.item2:
					selectedFragment = ItemTwoFragment.newInstance();
					break;
				case R.id.item3:
					selectedFragment = ItemThreeFragment.newInstance();
					break;
			}
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			//transaction.replace();
			transaction.commit();
			return true;
		}
	});
	}



	/* Initial required declaration when using OTG and RFID events. */
	private void initial()
	{
		//for receive Broadcast event about OTG Plug and Unplug
		AsDeviceMngr.getInstance().setEnableBroadCastReceiver(this);

		//OTG Plug, UnPlug Event
		AsDeviceMngr.getInstance().setDelegateOTG(this);

		//RFID Receive Data Event
		AsDeviceMngr.getInstance().setDelegateRFID(this);

		// This service do close ust otg when destory activity
		Intent intent = new Intent(getApplicationContext(), CloseService.class);
		startService(intent);
	}

	public void setPowerOn(){

		mPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean beep = mPrefs.getBoolean("READER_BEEP", true);
		boolean vib  = mPrefs.getBoolean("READER_VIB", true);
		boolean led  = mPrefs.getBoolean("READER_LED", true);
		boolean illu  = mPrefs.getBoolean("READER_ILLU", true);
		boolean powerOnBeep  = mPrefs.getBoolean("READER_POWERONBEEP", true);

		buttonControl(3);
		ShowToast("Power ON");
		AsDeviceMngr.getInstance().getOTG().setPowerWithOption(true,beep,vib,led,illu,powerOnBeep,0);

	}

	public static Intent createIntent(Context context)
	{
		Intent i = new Intent(context, MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return i;
	}

	synchronized private void ListClear()
	{
		tagCellList.clear();
		tagStringList.clear();
		tagDataList.clear();
		tagAdapter.notifyDataSetChanged();
		tvTagCount.setText("0  tags");
	}

	@Override
	protected void onResume()//Activityが表示された時
	{
		super.onResume();
		AsDeviceMngr.getInstance().setDelegateOTG(this);
		AsDeviceMngr.getInstance().setDelegateRFID(this);

		bIsHomeButtonPress = true;

		this.max_tag      = prefs.getInt("MAX_TAG", 0);
		this.max_time     = prefs.getInt("MAX_TIME", 0);
		this.repeat_cycle = prefs.getInt("REPEAT_CYCLE", 0);

		int new_encoding_type  = prefs.getInt("ENCODING_TYPE", 0);

		if (this.encoding_type != new_encoding_type)
			this.encoding_type = new_encoding_type;

		//If OTG Reader device plugged, try to open communication port.
		if(checkOtgPluged())
		{
			if(!AsDeviceMngr.getInstance().getOTG().isOpen())
				AsDeviceMngr.getInstance().getOTG().open();
		}
		buttonsStatusUpdate();

	}

	// Get Home Button Event
	@Override
	protected void onUserLeaveHint()
	{
		if(bIsHomeButtonPress == true)
		{
			AsDeviceMngr.getInstance().getOTG().setRfidPowerOn(false);
			buttonsStatusUpdate();
			buttonControl(4);
		}
		super.onUserLeaveHint();
	}


	//Back Button Event
	@Override
	public void onBackPressed()//戻るボタンが押されたら
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(" Alert  ");
		builder.setMessage("Do you want to exit the program?");
		builder.setPositiveButton("Exit", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface arg0, int arg1)
			{
				setPower.setChecked(false);
				AsDeviceMngr.getInstance().getOTG().setRfidPowerOn(false);

				/* Log folder Update */
				Logger.getInstance().setUpdateFileList(getApplication());

				if(screenOnReceiver != null)
					unregisterReceiver(screenOnReceiver);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				AsDeviceMngr.getInstance().getOTG().close();
				buttonsStatusUpdate();

				moveTaskToBack(true);

				finish();

				android.os.Process.killProcess(android.os.Process.myPid());
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		builder.show();
	}


	@Override
	protected void onStop()
	{
		super.onStop();
		overridePendingTransition(R.anim.slide_out_left1,
				R.anim.slide_out_left1);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	public void setRequestedOrientation(int requestedOrientation)
	{
		super.setRequestedOrientation(requestedOrientation);
	}

	// Destroy - clearCache
	@Override
	protected void onDestroy()
	{
		if(AsDeviceMngr.getInstance().getOTG().isOpen())
			AsDeviceMngr.getInstance().getOTG().close();

		if (isFinishing())
			AsDeviceMngr.getInstance().setDisableBroadCastReceiver();

		super.onDestroy();
	}

	@Override
	public void onSuccessReceived(int[] data, int code)
	{

	}

	@Override
	public void onReaderInfoReceived(int nOnTime,int nOffTime,int nSensTiem,int nLBTLevel,int nFhEnable,int nLbtEnable,int nCWEnable,int nPwer,int nMinPower, int nMaxPower)
	{

	}

	@Override
	public void onReceivedLeakageData(int i, int i1, int i2, int i3, int i4, int i5, int i6) {

	}

	@Override
	public void onFailureReceived(final int[] data)
	{
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				String test_msg = AsDeviceLib.int2str(data);

				Toast.makeText(MainActivity.this, "Error code  :  " + test_msg,
						Toast.LENGTH_SHORT).show();

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
		buttonsStatusUpdate();
	}


	@Override
	public void onBatteryStateReceived(final int dest ,final int nCharging)
	{
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				//A30D Not use nCharging value
				tvBattery.setText(""+dest + "%");
				Log.i(TAG, dest+ " %");
			}
		});
	}



	private boolean checkOtgPluged()
	{
		boolean reVal = false;
		ImageView img = (ImageView) findViewById(R.id.img_log);

		//OTG Plugged
		if (AsDeviceMngr.getInstance().getOTG().isPlugged())
		{
			img.setImageResource(R.drawable.log_otg);
			img.setVisibility(View.VISIBLE);
			tvDeviceStatus.setText("Connected");
			reVal = true;
		}
		//OTG Unplugged
		else
		{
			tvDeviceStatus.setText(" Disconnected");
			img.setVisibility(View.INVISIBLE);
		}
		return reVal;
	}


	@Override
	public void onPlugged(final boolean plug)//接続されたら
	{
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				checkOtgPluged();
				if (plug)
				{
					ShowToast("OTG Connected!");
					boolean isOpen = AsDeviceMngr.getInstance().getOTG().open();
					if(isOpen){
						setPowerOn();
					}
				}
				else
					ShowToast("OTG Disconnected!");

				buttonsStatusUpdate();
			}
		});
	}

	@Override
	public void onRegionReceived(int region)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onSelectParamReceived(int[] selParam)
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
	public void onTagMemoryLongReceived(int[] dest)
	{
		// TODO Auto-generated method stub

	}



	@Override
	public void onSessionReceived(int session)
	{
		// TODO Auto-generated method stub

	}



	synchronized private void ListRefresh(final int[] tagData, final float fRSSI, final String temp)
	{
		Utils.getInstance().playSound(this);
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				String strTag = ""+EpcConverter.toString(encoding_type, tagData);

				Log.i("ListRefresh > ", temp + "");

				boolean newTagReceived = true;
				boolean newItemReceived = true;
				int index;

				for (index = 0; index < tagDataList.size(); index++)
				{
					TagData cData = tagDataList.get(index);
					if(cData.m_strData.equals(strTag))
					{
						newTagReceived = false;
						break;
					}
				}

				if(encoding_type == EpcConverter.EAN13)
				{
					if(newTagReceived)
					{
						for (index = 0; index < tagCellList.size(); index++)
						{
							if (tagCellList.get(index).getName().equals(strTag))
							{
								newItemReceived = false;
								break;
							}
						}
					}
					else
					{
						newItemReceived = false;
					}
				}

				if(newTagReceived)
				{
					TagData da   = new TagData();
					da.mData     = tagData;
					da.m_strData = strTag;
					da.m_nCount  = 1;
					tagDataList.add(da);
				}

				//New Tag
				if ( (encoding_type != EpcConverter.EAN13 && newTagReceived)
						|| (encoding_type == EpcConverter.EAN13 && newTagReceived && newItemReceived) )
				{

					final CellData ttag = new CellData();
					ttag.setName(strTag);


					if (fRSSI<0 && temp.equals(df_NOT_INVALID_RFM)){
						ttag.setRssi(""+fRSSI);
						ttag.setRFM("");
					}
					if (fRSSI>0 && !temp.equals(df_NOT_INVALID_RFM)){
						ttag.setRssi("");
						ttag.setRFM(""+temp+"°C");
					}

					tagCellList.add(ttag);
					tagStringList.add(strTag);

					tagAdapter.notifyDataSetChanged();
					tvTagCount.setText(tagCellList.size() + " tags");

				}

				//Update Tag
				else if( (encoding_type != EpcConverter.EAN13 && !newTagReceived) ||
						(encoding_type == EpcConverter.EAN13 && newTagReceived && !newItemReceived) )
				{
					final int indexLock = index;

					if(tagCellList.size() > 0)
					{
						TagData cData = tagDataList.get(index);
						cData.m_nCount = cData.m_nCount+1;
						tagDataList.set(index, cData);
						tagCellList.get(indexLock).setName(strTag);


						if (fRSSI<0 && temp.equals(df_NOT_INVALID_RFM)){
							tagCellList.get(indexLock).setRssi(""+fRSSI);
							tagCellList.get(indexLock).setRFM("");
						}
						if (fRSSI>0 && !temp.equals(df_NOT_INVALID_RFM)){
							tagCellList.get(indexLock).setRssi("");
							tagCellList.get(indexLock).setRFM(""+temp+"°C");
						}

						tagCellList.get(indexLock).setValue(""+cData.m_nCount);
						tagAdapter.notifyDataSetChanged();
					}

				}

			}
		});
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



	private void buttonsStatusUpdate()
	{

		runOnUiThread(new Runnable()
		{
			public void run()
			{
				//Plug & Open
				if((AsDeviceMngr.getInstance().getOTG().isOpen())&&(AsDeviceMngr.getInstance().getOTG().isPlugged()))
				{

					setPower.setEnabled(true);
				}

				//Only Plug
				else if(AsDeviceMngr.getInstance().getOTG().isPlugged())
				{
					btn_ext_read.setEnabled(false);
					btn_ext_rfm.setEnabled(false);
					btn_battery.setEnabled(false);
					clearScreen.setEnabled(false);
					stopAutoRead.setEnabled(false);
					option.setEnabled(false);
					setPower.setEnabled(false);
				}
				//Unplugged
				else
				{
					btn_ext_read.setEnabled(false);
					btn_ext_rfm.setEnabled(false);
					btn_battery.setEnabled(false);
					clearScreen.setEnabled(false);
					stopAutoRead.setEnabled(false);
					option.setEnabled(false);
					setPower.setEnabled(false);
					setPower.setChecked(false);
				}
			}
		});
	}


	private void buttonControl(final int read )
	{

		runOnUiThread(new Runnable()
		{
			public void run()
			{
				switch (read){
					case 0:       // stop
						btn_ext_read.setEnabled(true);
						btn_ext_rfm.setEnabled(true);
						stopAutoRead.setEnabled(false);
						clearScreen.setEnabled(true);
						break;

					case 1:       // RFID read
						btn_ext_read.setEnabled(false);
						btn_ext_rfm.setEnabled(false);
						stopAutoRead.setEnabled(true);
						clearScreen.setEnabled(true);
						break;

					case 2:       // RFM read
						btn_ext_read.setEnabled(false);
						btn_ext_rfm.setEnabled(false);
						stopAutoRead.setEnabled(true);
						clearScreen.setEnabled(true);
						break;

					case 3:       // power on
						btn_ext_read.setEnabled(true);
						btn_ext_rfm.setEnabled(true);
						btn_battery.setEnabled(true);
						stopAutoRead.setEnabled(true);
						clearScreen.setEnabled(true);
						option.setEnabled(true);
						setPower.setChecked(true);
						setItemClickListener(true);

						break;

					case 4:       // power off
						btn_ext_read.setEnabled(false);
						btn_ext_rfm.setEnabled(false);
						btn_battery.setEnabled(false);
						stopAutoRead.setEnabled(false);
						clearScreen.setEnabled(false);
						option.setEnabled(false);
						setPower.setChecked(false);
						setItemClickListener(false);
						break;

				}

			}
		});


	}

	public void setItemClickListener(boolean power){
		if(power){
			epclist.setOnItemClickListener(new OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
				{
					AsDeviceMngr.getInstance().getOTG().stopReadTags();
					if (encoding_type == EpcConverter.HEX_STRING)
					{
						bIsHomeButtonPress = false;
						TagData da = tagDataList.get(arg2);
						String strData =  EpcConverter.toString(encoding_type, da.mData);

						Intent intent = new Intent(getBaseContext(),
								TagAccessActivity.class);
						intent.putExtra("tagitem",strData);
						startActivity(intent);
						Log.i(TAG,"TEST LOG");

					}
					else
					{
						Toast.makeText(MainActivity.this,
								"Not possible when encoding type is not HEX.",
								Toast.LENGTH_SHORT).show();
					}
				}
			});
		}else {
			epclist.setOnItemClickListener(null);
		}

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
		ListRefresh(dest,df_NOT_INVALID_RSSI,df_NOT_INVALID_RFM);
	}


	@Override
	public void onTagWithTidReceived(int[] pcEpc, int[] tid) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onTagWithRssiReceived(int[] pcEpc, int rssi) {
		// TODO Auto-generated method stub
		ListRefresh(pcEpc,rssi,df_NOT_INVALID_RFM);
	}


	@Override
	public void onPcEpcSensorDataReceived(final int[] pcEpc,final int[] sensorData) {  // RFM
		if(SensorTag.getInstance().parseSensorData(sensorData))
		{
			//Success parse sensor data
			/* Manus S3 Type sensor */
			String sensorValues  = String.format("%.2f",SensorTag.getInstance().sensorTemp);
			String Cfq = String.format("%.3f",SensorTag.getInstance().sensorCfq);

			Log.i(TAG,"[Sensor Val]  Cop: "+SensorTag.getInstance().sensorCop
					+" Cfq : "+Cfq
					+ "CT  : "+SensorTag.getInstance().sensorCt
					+" temp: "+sensorValues);

			ListRefresh(pcEpc, df_NOT_INVALID_RSSI, sensorValues);
		}
		else
		{
			//fail parse sensor data
			ListRefresh(pcEpc, df_NOT_INVALID_RSSI, "Unknown SensorTAG");
		}
	}


}


