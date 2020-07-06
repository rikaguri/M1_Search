package com.minireader.sdevice.rfid;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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

public class MainActivity extends AppCompatActivity implements IOnAsDeviceRfidEvent,IOnOtgEvent {

	private final String TAG = "MainActivity";
	private final int df_NOT_INVALID_RSSI = 10;
	private final String df_NOT_INVALID_RFM = "nRFM";
	private int encoding_type = EpcConverter.HEX_STRING;
	private int max_tag = 0;//maximum number of tags to read
	private int max_time = 0;//maximum elapsed time to read tags(sec)
	private int repeat_cycle = 0;//how many times reader performs inventory round


	private ListView epclist;
	private ListView epclist_empty;
	private TextView tvTagCount, tvDeviceStatus, tvBattery;

	private ArrayList<CellData> tagCellList = new ArrayList<>();
	private ArrayList<String> tagStringList = new ArrayList<>();
	private ArrayList<CellData> tagArrayEmpty = new ArrayList<>();
	private ArrayList<MainActivity.TagData> tagDataList = new ArrayList<>();


	private CustomTagAdapter tagAdaterEmpty;
	private CustomTagAdapter tagAdapter;
	private SharedPreferences prefs = null;
	private boolean bIsHomeButtonPress = true;

	private SharedPreferences mPrefs = null;

	private MainActivity.ScreenOnReceiver screenOnReceiver = null;

	ToggleButton setPower;

	Button option, clearScreen, stopAutoRead, btn_ext_read, btn_ext_rfm, btn_battery;

	//タグのデータをcsvに格納するためのデータ
	public ArrayList<MainActivity.MyTagData> myTagDataArrayList = new ArrayList<>();
	//csvファイルのためのもの
	FileWriter fileWriter;
	PrintWriter printWriter;
	private Integer pastTime = null;//時間保存用


	public class TagData {
		public int[] mData;
		public int m_nCount;
		public String m_strData;
		//public String m_Time;
	}

	//物探し用のクラス
	public class MyTagData {
		public String data;
		public int rssi;
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


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		option       = (Button) findViewById(R.id.option);
		//option.setVisibility(View.INVISIBLE);
		clearScreen  = (Button) findViewById(R.id.btn_clear);
		//clearScreen.setVisibility(View.INVISIBLE);
		stopAutoRead = (Button) findViewById(R.id.btn_stop);
		//stopAutoRead.setVisibility(View.INVISIBLE);
		epclist        = (ListView) findViewById(R.id.tag_list);
		//epclist.setVisibility(View.INVISIBLE);
		tvTagCount     = (TextView) findViewById(R.id.name);
		//tvTagCount.setVisibility(View.INVISIBLE);
		tvDeviceStatus = (TextView) findViewById(R.id.aboutvalue);
		//tvDeviceStatus.setVisibility(View.INVISIBLE);
		tvBattery      = (TextView) findViewById(R.id.textView3);
		//tvBattery.setVisibility(View.INVISIBLE);

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
		btn_ext_rfm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				buttonControl(2);
				AsDeviceMngr.getInstance().getOTG().startReadTagsRFM(0x03,max_tag,max_time,repeat_cycle);
			}
		});
		btn_ext_rfm.setEnabled(false);

		// Battery
		btn_battery  = (Button) findViewById(R.id.btn_battery);
		btn_battery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AsDeviceMngr.getInstance().getOTG().getBattery();
			}
		});
		btn_battery.setEnabled(false);

		//Read Tag Button
		btn_ext_read = (Button) findViewById(R.id.btn_ext_read);
		btn_ext_read.setOnClickListener(new View.OnClickListener()
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
		stopAutoRead.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				buttonControl(0);

				//csvに書き込み
				for(int i =0;i<myTagDataArrayList.size();i++){
					printWriter.print("");//すでに改行されているRSSIのデータ
					printWriter.println();//改行
					printWriter.print("");//すでに改行されているRSSIのデータ
					printWriter.println();//改行

				}
				//閉じる
				printWriter.close();
				AsDeviceMngr.getInstance().getOTG().stopReadTags();
			}
		});

		stopAutoRead.setEnabled(false);

		// Clear button
		clearScreen = (Button) findViewById(R.id.btn_clear);
		clearScreen.setOnClickListener(new View.OnClickListener()
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
		option.setOnClickListener(new View.OnClickListener()
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
		setPower.setOnClickListener(new View.OnClickListener()
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


		//CSVに保存するための出力ファイルの作成
		try {
			fileWriter =fileWriter = new FileWriter("test.csv",false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		printWriter = new PrintWriter(new BufferedWriter(fileWriter));



		initial();

		/* SDK , APP Info */
		TextView txtTitle  = (TextView) findViewById(R.id.titlebar_text);
		txtTitle.setText("A3XD");


		TextView txtSubTitle  = (TextView) findViewById(R.id.txt_sub_title);
		txtSubTitle.setText("APP: "+ Utils.getInstance().getAppVersion(this)+" SDK: "+ AsDeviceConst.strLibVersion);
		regScreeOffEvent();

		/*Enable save Log file  */
		/*Log folder dir : /RFIDLOG/log.csv*/
		Logger.getInstance().enableCSVLogFileName("A3XD",true);

		/* type : 0 RX 1: TX 2: Event */
		Logger.getInstance().addLogData(2,"Start Log : onCreate");
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

	//ListDataの値をクリアする関数
	//排他制御
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


	//Another activity comes into foreground
	@Override
	protected void onStop()
	{
		super.onStop();
		overridePendingTransition(R.anim.slide_out_left1,
				R.anim.slide_out_left1);
	}

	//The activity is no longer visible
	@Override
	protected void onPause()
	{
		super.onPause();
	}

	//画面の向きを動的に生成する
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

	//リーダーのイベント
	@Override
	public void onReaderAboutInfo(String s, String s1, String s2, byte b) {

	}

	//リセットを受け取った時のイベント
	@Override
	public void onResetReceived()
	{
		buttonsStatusUpdate();
	}


	//バッテリーのステータスを受け取った時のイベント
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

	//タグが読み取れた時にリストが更新されるところ
	//排他制御している
	synchronized private void ListRefresh(final int[] tagData, final float fRSSI, final String temp,final Integer time)
	{
		Utils.getInstance().playSound(this);
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				//得られたデータをString型に変換
				String strTag = ""+EpcConverter.toString(encoding_type, tagData);
				//strTagに何が入っているかをToastで見てみる

				Log.i("ListRefresh > ", temp + "");

				boolean newTagReceived = true;//新しいタグが得られたかどうかのフラグ
				boolean newItemReceived = true;//新しいアイテムが得られたかどうかのフラグ
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

					final CellData ttag = new CellData();//CellData型の変数を準備
					ttag.setName(strTag);//変数に名前をつける


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

				//ここから物探しの時の処理を行う
				//物探しの時に使用する変数


				if(pastTime == null){//そもそも前のスキャンがなかった時
					//そのタグをlistに追加


				}
				//前のスキャンとの時間差が100ms以下だった場合
				else if(pastTime-time <= 100){

				}

				//別スキャンの場合
				else{

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
			epclist.setOnItemClickListener(new AdapterView.OnItemClickListener()
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


	//タグが読み取れた時(RSSIがない時)に発生するイベント
	@Override
	public void onTagReceived(int[] dest) {
		// TODO Auto-generated method stub
		//現在時刻を取得
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		//ミリ秒部分だけ出力
		SimpleDateFormat sdf = new SimpleDateFormat("SSS");
		//string型にする
		String strtmp = sdf.format(timestamp);
		//int型にする
		Integer time = Integer.parseInt(strtmp);
		ListRefresh(dest,df_NOT_INVALID_RSSI,df_NOT_INVALID_RFM,time);
	}


	@Override
	public void onTagWithTidReceived(int[] pcEpc, int[] tid) {
		// TODO Auto-generated method stub

	}


	//rssiがついたタグが読み取れた時発生するイベント
	@Override
	public void onTagWithRssiReceived(int[] pcEpc, int rssi) {
		// TODO Auto-generated method stub
		//現在時刻を取得
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		//ミリ秒部分だけ出力
		SimpleDateFormat sdf = new SimpleDateFormat("SSS");
		//string型にする
		String strtmp = sdf.format(timestamp);
		//int型にする
		Integer time = Integer.parseInt(strtmp);

		//List<String> list = Arrays.asList(pcEpc);

		//listRefreshにデータを送る
		ListRefresh(pcEpc,rssi,df_NOT_INVALID_RFM,time);
	}


	//RSSIがないものを読み取った時に時に発生するイベント
	@Override
	public void onPcEpcSensorDataReceived(final int[] pcEpc,final int[] sensorData) {  // RFM
		//現在時刻を取得
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		//ミリ秒部分だけ出力
		SimpleDateFormat sdf = new SimpleDateFormat("SSS");
		//string型にする
		String strtmp = sdf.format(timestamp);
		//int型にする
		Integer time = Integer.parseInt(strtmp);

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

			ListRefresh(pcEpc, df_NOT_INVALID_RSSI, sensorValues,time);
		}
		else
		{
			//fail parse sensor data
			ListRefresh(pcEpc, df_NOT_INVALID_RSSI, "Unknown SensorTAG",time);
		}
	}



	/*//Activityの初期化
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Fragmentを作成
		MainFragment fragment = new MainFragment();
		// Fragmentの追加や削除といった変更を行う際は、Transactionを利用します
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		// 新しく追加を行うのでaddを使用します
		// 他にも、よく使う操作で、replace removeといったメソッドがあります
		// メソッドの1つ目の引数は対象のViewGroupのID、2つ目の引数は追加するfragment
		transaction.add(R.id.container, fragment);
		// 最後にcommitを使用することで変更を反映します
		transaction.commit();


		//以下にBottomNavigationViewのコード
		BottomNavigationView bottom_navigation = (BottomNavigationView) findViewById(R.id.bottom);//下部分のナビゲーション作成
		BottomNavigationView.OnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem item) {
				Fragment selectedFragment = null;
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
	}*/
}



