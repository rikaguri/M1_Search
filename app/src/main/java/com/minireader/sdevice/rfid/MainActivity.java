package com.minireader.sdevice.rfid;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.util.ArrayList;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import com.asreader.sdevice.dongle.custom.*;
import com.asreader.util.SensorTag;
import com.asreader.util.Utils;
import com.asreader.utility.EpcConverter;
import com.asreader.utility.Logger;

import static java.lang.Math.abs;
import static java.nio.charset.StandardCharsets.UTF_8;

public class MainActivity extends Activity implements IOnAsDeviceRfidEvent,IOnOtgEvent, SensorEventListener {

	private final String TAG = "MainActivity";
	private final int df_NOT_INVALID_RSSI = 10;
	private final String df_NOT_INVALID_RFM = "nRFM";
	private int encoding_type = EpcConverter.HEX_STRING;
	private int max_tag = 4000;//maximum number of tags to read
	private int max_time = 10;//maximum elapsed time to read tags(sec) 経過時間
	private int repeat_cycle = 100;//how many times reader performs inventory round


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

	Button option, clearScreen, stopAutoRead, btn_ext_read, btn_ext_rfm, btn_battery,btn_upload;

	//タグのデータをcsvに格納するためのデータ
	public ArrayList<MainActivity.MyTagData> myTagDataArrayList = new ArrayList<>();
	private String finCalData="";
	private String finSaveData="";
	private String tempData="";
	private String tempRSSI="";
	private Integer pastTime = 0;//時間保存用
	private BufferedWriter bw;
	private Integer countTagReadRssi =0;
	private ArrayList<Integer> tempIDList = new ArrayList<>();//一時保存用
	private ArrayList<MainActivity.MyTagData> finDataList = new ArrayList<>();//書き込み前保存用

	//POST通信用データ
	private  UploadTask task;
	private  UploadTask task2;
	//String url=;//PHPがPOSTで受け取ったwordを入れて作成するHTMLページ


	//方向推定用
	final static float PI = (float)Math.PI;

	private SensorManager sensorManager;
	Sensor aSensor;  // 加速度センサ TYPE_LINEAR_ACCELERATION
	Sensor grSensor; //重力センサ TYPE_GRAVITY
	Sensor gSensor; // ジャイロセンサ TYPE_GYROSCOPE
	Sensor mSensor; //地磁気センサ TYPE_MAGNETIC_FIELD
	Sensor oSensor; //方位センサ TYPE_ORIENTATION
	private TextView azimuth, moveTextx, moveTexty, moveTextz;

	//移動方向推定
	private float[] accelValue = new float[3];  //加速度
	private float[] gravity = new float[3];    //重力
	private float[] north = new float[3];       //N軸
	private float[] east = new float[3];        //E軸
	//GNE軸への射影成分
	private float[] gneAccel = new float[3];
	private float oriantation = 0; //方位角（degree）
	private float orientationRad = 0;   //方位角（rad）
	//移動方向推定
	private long lastAccelTime = 0;
	private float[] speed = new float[3];   //速度
	private float[] difference = new float[3];  //変位
	private boolean diffFlag = true;

	private Button button;
	boolean check = false;

	public class TagData {
		public int[] mData;
		public int m_nCount;
		public String m_strData;
		//public String m_Time;
	}

	//物探し用のクラス
	public class MyTagData {
		public Integer time;
		public String name;
		public float rssi;
		public String x_vec;
		public String y_vec;
		public String z_vec;
	}

	//センサー値の変化があったときのevent
	@Override
	public void onSensorChanged(SensorEvent event) {
		int i = 0;

		switch (event.sensor.getType()){

			case Sensor.TYPE_LINEAR_ACCELERATION:   //重力を除く加速度m/s^2

				accelValue = event.values.clone();

				if(abs(gravity[2]) < 0.5){
					//スマホが垂直な時は方向算出しない
					//diffFlag = false;
					//azimuth.setText("gravity[2] < 0.5");
					//リセット
					lastAccelTime = 0;
					for (i = 0; i < 3; i++){
						speed[i] = 0;
						difference[i] = 0;
					}
				}


				//if(check && abs(accelValue[0]) > 0.05 || abs(accelValue[1]) > 0.05 || abs(accelValue[2]) > 0.05) {//もしかしたらいらないかも
				if(check){//上がいらない場合は、こっちに切り替える

					//moveTextx.setText("accelValue[0]:" + String.valueOf(accelValue[0]));
					//moveTexty.setText("accelValue[1]:" + String.valueOf(accelValue[1]));
					//moveTextz.setText("accelValue[2]:" + String.valueOf(accelValue[2]));

					//各軸の加速度を2重積分
					long dt = event.timestamp - lastAccelTime;    //dt(nanosec)
					if (lastAccelTime > 0) {
						//GNE軸に変換
						gneAccel[0] = east[0] * accelValue[0] + east[1] * accelValue[1] + east[2] * accelValue[2];
						gneAccel[1] = north[0] * accelValue[0] + north[1] * accelValue[1] + north[2] * accelValue[2];
						gneAccel[2] = gravity[0] * accelValue[0] + gravity[1] * accelValue[1] + gravity[2] * accelValue[2];

						for (i = 0; i < 3; i++) {
							if (abs(gneAccel[i]) > 0.5) {
								//速度（cm/s）
								speed[i] += gneAccel[i] * dt / 10000000.0;
								//距離（cm）
								difference[i] += speed[i] * dt / 1000000000.0;
							}
						}
					}
					lastAccelTime = event.timestamp;
				}
				break;


			case Sensor.TYPE_ORIENTATION:
				oriantation = event.values[0];
				//azimuth.setText("方位:" + String.valueOf(oriantation));
				break;


			case Sensor.TYPE_GRAVITY:   //重力を取得
				gravity = event.values.clone();
				gravity = unit(gravity);    //正規化
				break;


			default:
				return;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
		//Log.d("o","ルートディレクトリ？"+);
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
				boolean rssiOn = prefs.getBoolean("DISPLAY_RSSI", true);
				check=true;
				AsDeviceMngr.getInstance().getOTG().startReadTags(max_tag, max_time, repeat_cycle,rssiOn);

			}
		});
		btn_ext_read.setEnabled(false);

		// Stop button
		stopAutoRead = (Button) findViewById(R.id.btn_stop);
		stopAutoRead.setOnClickListener(new View.OnClickListener()
		{
			@RequiresApi(api = Build.VERSION_CODES.KITKAT)
			@Override
			public void onClick(View v)
			{
				buttonControl(0);
				check=false;
				AsDeviceMngr.getInstance().getOTG().stopReadTags();
			}
		});
		stopAutoRead.setEnabled(false);

		//upload button
		btn_upload=(Button)findViewById(R.id.upload);
		btn_upload.setOnClickListener(new View.OnClickListener() {
			@RequiresApi(api = Build.VERSION_CODES.KITKAT)
			@Override
			public void onClick(View v) {
				createCSVData();//ここでcsvのデータ形式にする

				//ファイルの読み書きの話
				File path = getExternalFilesDir(null);
				String testfile ="calc.csv";
				File file = new File(path, testfile);
				if(file.exists()){
					file.delete();
					ShowToast("Delete");
				}

				try {
					FileOutputStream outputStream = new FileOutputStream(file, true);
					OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, UTF_8);
					//outputStream.write(finData.getBytes());
					//outputStream.close();
					bw = new BufferedWriter(outputStreamWriter);
					finCalData = finCalData+"\n";
					bw.write(finCalData);
					bw.flush();
					bw.close();
					ShowToast("STOP"+ finCalData);
				}catch(Exception e){
					e.printStackTrace();
					ShowToast("NG"+ finCalData);
				}

				File path2 = getExternalFilesDir(null);
				String testfile2 ="save.csv";
				File file2 = new File(path2, testfile2);
				if(file2.exists()){
					file2.delete();
					ShowToast("Delete");
				}
				try {
					FileOutputStream outputStream2 = new FileOutputStream(file2, true);
					OutputStreamWriter outputStreamWriter2 = new OutputStreamWriter(outputStream2, UTF_8);
					//outputStream.write(finData.getBytes());
					//outputStream.close();
					BufferedWriter bw = new BufferedWriter(outputStreamWriter2);
					finSaveData = finSaveData+"\n";
					bw.write(finSaveData);
					bw.flush();
					bw.close();
					ShowToast("STOP"+ finSaveData);
				}catch(Exception e){
					e.printStackTrace();
					ShowToast("NG"+ finSaveData);
				}


				/*task = new UploadTask();
				task.setListener(createListener());
				task.execute(finCalData);*/
				task2 =new UploadTask();
				task2.setListener(createListener());
				task2.execute(finSaveData);


				finCalData ="";
				finSaveData="";
				countTagReadRssi = 0;
			}
		});


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

		//方位推定用
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// 標準角加速度を登録
		// 引数を変えると違う種類のセンサ値を取得
		aSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		grSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		oSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

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
		boolean beep = mPrefs.getBoolean("READER_BEEP", false);
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
		countTagReadRssi = 0;
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

		// registerListener：監視を開始
		// SENSOR_DELAY_UIを変更すると更新頻度が変わる SENSOR_DELAY_FASTEST:0ms,
		// SENSOR_DELAY_GAME:20ms, SENSOR_DELAY_UI:60ms, SENSOR_DELAY_NORMAL:200ms
		sensorManager.registerListener(this, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, grSensor, SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, gSensor, SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, oSensor, SensorManager.SENSOR_DELAY_NORMAL);

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
		// Listenerを解除
		sensorManager.unregisterListener(this);
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
	synchronized private void ListRefresh(final int[] tagData, final float fRSSI, final String temp,final int time)
	{
		Utils.getInstance().playSound(this);
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				//得られたデータをString型に変換
				String strTag = ""+EpcConverter.toString(encoding_type, tagData);
				//Toastで見てみる
				ShowToast(String.valueOf(fRSSI));

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

	public void createCSVData(){//CSVのデータにするときに使う関数、ここで同スキャン別スキャン判定等々を行う

		int index;

		/*for(index = 0 ;index<finDataList.size();index++){
			//同じスキャンの時
			if(finDataList.get(index).time - finDataList.get(index-1).time < 100){
				tempData=tempData+finDataList.get(index).name;
				tempIDList.add(tempData);
				finDataList.get(index).x_vec="";
				finDataList.get(index).y_vec="";
				finDataList.get(index).z_vec="";
			}
			else if(){//別スキャンの時かつ前の別スキャンと違うスキャンが読み込まれてた時


			}
		}*/
		//以下取ってきたものを全てcsv形式に直すもの
		for(index=0;index<finDataList.size();index++){
			if(finDataList.get(index).time-pastTime<100) {
				finSaveData = finSaveData + "\n" +","+ String.valueOf(finDataList.get(index).time) + "," + finDataList.get(index).name + "," + finDataList.get(index).rssi + "," + finDataList.get(index).x_vec + "," + finDataList.get(index).y_vec + "," + finDataList.get(index).z_vec;
				pastTime=finDataList.get(index).time;
			}
			else{
				finSaveData= finSaveData+ "\n"+"Betu"+","+ String.valueOf(finDataList.get(index).time)+","+finDataList.get(index).name+","+finDataList.get(index).rssi+","+finDataList.get(index).x_vec+","+finDataList.get(index).y_vec+","+finDataList.get(index).z_vec;
				pastTime=finDataList.get(index).time;
			}
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
		//int型にする
		int time = (int) System.currentTimeMillis();

		ListRefresh(dest,df_NOT_INVALID_RSSI,df_NOT_INVALID_RFM,time);
	}



	//tidが読み読み取れた時に発生するイベント
	@Override
	public void onTagWithTidReceived(int[] pcEpc, int[] tid) {
		// TODO Auto-generated method stub
		//現在時刻を取得
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		//int型にする
		int time = (int) System.currentTimeMillis();
	}



/*	//rssiがついたタグが読み取れた時発生するイベント
	@Override
	public void onTagWithRssiReceived(int[] pcEpc, int rssi) {
		// TODO Auto-generated method stub

		countTagReadRssi++;

		//現在時刻を取得
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		//int型にする
		int time = (int) System.currentTimeMillis();//currentTimeMIllis()はlong型で時刻を出しているので
		//引数のpcEPCをStringにする
		String strTag = ""+EpcConverter.toString(encoding_type, pcEpc);

		if ( countTagReadRssi<=2 || time-pastTime >= 100) {//別スキャンになった時 && 初スキャン,2回目スキャン
			//デバッグ用
			//finData = finData + "ScanStart" + "\n" + strTag + "\n" + rssi + "\n" + time + "\n";
			if (countTagReadRssi == 1) {//1回目のスキャン(空データなので何もしない)
				//何もしない
				lastAccelTime = 0;
				int i = 0;
				for (i = 0; i < 3; i++) {
					speed[i] = 0;
					difference[i] = 0;
				}
			}
			else if (countTagReadRssi >= 2) {//countTagが2以上でfinに値が入っている時
				if (countTagReadRssi != 2) {//2以外の時の処理->countが2の時に初めてデータが取れるのでtempにそのまま書き込みたい&方向を正規化したいのでこっちに分岐をいれる
					if (!finCalData.isEmpty() && !finSaveData.isEmpty()) {//finにデータが入っている時
						difference = unit(difference);//方向を正規化
						//今までのデータをfindataにいれる
						finCalData = finCalData + "\n" + tempData + "\n" + tempRSSI;
						finSaveData = finSaveData + "\n" + tempData + "\n" + String.valueOf(difference[0]) + "," + String.valueOf(difference[1]) + "," + String.valueOf(difference[2]);
					} else {//finにデータが入っていない時-> finと足さずにいれる(改行されちゃうので)
						difference = unit(difference);//方向を正規化
						finCalData = tempData + "\n" + tempRSSI;
						finSaveData = tempData + "\n" + String.valueOf(difference[0]) + "," + String.valueOf(difference[1]) + "," + String.valueOf(difference[2]);
					}
				}

				//新しく書き込み
				tempData = strTag;
				tempRSSI = String.valueOf(rssi);

				//方向リセット
				lastAccelTime = 0;
				int i = 0;
				for (i = 0; i < 3; i++) {
					speed[i] = 0;
					difference[i] = 0;
				}

				if(countTagReadRssi!=2) {
					//新しい方向の基準を設定
					//N軸，E軸の設定
					orientationRad = (float) Math.toRadians(oriantation);    //degree[°]をradianへ 方位をラジアンに
					float oriNorth = (float) Math.PI / 2 + orientationRad;   //π/2+θ　方位を90°回転
					float oriEast = orientationRad;                         //θ 方位を入れる
					//N軸
					north[0] = (float) Math.cos(oriNorth);  //oriNorthをcosに射影したものを代入(x座標)
					north[1] = (float) Math.sin(oriNorth);  //oriNorthをsinに射影したものを代入(y座標)
					north[2] = -1.0f * (north[0] * gravity[0] + north[1] * gravity[1]) / gravity[2]; //これ何してるのかわかんないけど多分gravity?を頑張っている
					north = unit(north);    //正規化
					//E軸 北から-90°してる
					east[0] = (float) Math.cos(oriEast);//oriEastをcosに射影したものを代入(x座標)
					east[1] = (float) Math.sin(oriEast);
					east[2] = -1.0f * (east[0] * gravity[0] + east[1] * gravity[1]) / gravity[2];
					east = unit(east);  //正規化
			*//*else{
				finCalData= tempData + "\n" + tempRSSI;
				finSaveData = tempData + "\n" + String.valueOf(difference[0]) + "," + String.valueOf(difference[1]) + "," + String.valueOf(difference[2]);
			}*//*
				}

			}
		}
			//finData = finData + "ScanStart" + "\n" + strTag + "\n" + rssi + "\n" + time + "\n";

		else{//連続している時
			 //前のデータセットに付け足し
			tempData=tempData+","+strTag;
			tempRSSI=tempRSSI+","+String.valueOf(rssi);
			//以下デバッグ用
			//ShowToast(String.valueOf(tempDataList));
			//finData = finData+"\n"+strTag+"\n"+rssi+"\n"+time +"\n";

			//初期化
				lastAccelTime = 0;
				int i = 0;
				for (i = 0; i < 3; i++) {
					speed[i] = 0;
					difference[i] = 0;
				}
		}

		pastTime = time;
		//listRefreshにデータを送る
		ListRefresh(pcEpc,rssi,df_NOT_INVALID_RFM,time);
	}*/

	//rssiがついたタグが読み取れた時発生するイベント
	//別スキャンの時だけ計測・記録を行う
	@Override
	public void onTagWithRssiReceived(int[] pcEpc, int rssi) {
		// TODO Auto-generated method stub

		countTagReadRssi++;
		check=false;

		//現在時刻を取得
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		//int型にする
		int time = (int) System.currentTimeMillis();//currentTimeMIllis()はlong型で時刻を出しているので
		//引数のpcEPCをStringにする
		String strTag = ""+EpcConverter.toString(encoding_type, pcEpc);

		//時刻・ID・RSSI・xベクトル・yベクトル・zベクトルを記録
		MyTagData mydata = new MyTagData();
		mydata.time= time;
		mydata.rssi= rssi;
		mydata.name= strTag;
		difference=unit(difference);//正規化
		mydata.x_vec= String.valueOf(difference[0]);
		mydata.y_vec= String.valueOf(difference[1]);
		mydata.z_vec= String.valueOf(difference[2]);
		finDataList.add(mydata);

		check=true;

		//初期化
		lastAccelTime = 0;
		int i = 0;
		for (i = 0; i < 3; i++) {
			speed[i] = 0;
			difference[i] = 0;
		}

		//新しい方向の基準を設定
		//N軸，E軸の設定
		orientationRad = (float) Math.toRadians(oriantation);    //degree[°]をradianへ 方位をラジアンに
		float oriNorth = (float) Math.PI / 2 + orientationRad;   //π/2+θ　方位を90°回転
		float oriEast = orientationRad;                         //θ 方位を入れる
		//N軸
		north[0] = (float) Math.cos(oriNorth);  //oriNorthをcosに射影したものを代入(x座標)
		north[1] = (float) Math.sin(oriNorth);  //oriNorthをsinに射影したものを代入(y座標)
		north[2] = -1.0f * (north[0] * gravity[0] + north[1] * gravity[1]) / gravity[2]; //これ何してるのかわかんないけど多分gravity?を頑張っている
		north = unit(north);    //正規化
		//E軸 北から-90°してる
		east[0] = (float) Math.cos(oriEast);//oriEastをcosに射影したものを代入(x座標)
		east[1] = (float) Math.sin(oriEast);
		east[2] = -1.0f * (east[0] * gravity[0] + east[1] * gravity[1]) / gravity[2];
		east = unit(east);  //正規化


		//finCalData = finCalData + "\n" + time + "\n" + strTag; //デバッグ用

		/*if ( countTagReadRssi<=2 || time-pastTime >= 100) {//別スキャンになった時 && 初スキャン,2回目スキャン
			//finData = finData + "ScanStart" + "\n" + strTag + "\n" + rssi + "\n" + time + "\n";　//デバッグ用
			if (countTagReadRssi == 1) {//1回目のスキャン(空データなので何もしない)
				//何もしない
				//値を全てリセット
				lastAccelTime = 0;
				int i = 0;
				for (i = 0; i < 3; i++) {
					speed[i] = 0;
					difference[i] = 0;
				}
			}
			else if (countTagReadRssi >= 2) {//countTagが2以上でfinに値が入っている時
				if (countTagReadRssi != 2) {//2以外の時の処理->countが2の時に初めてデータが取れるのでtempにそのまま書き込みたい&方向を正規化したいのでこっちに分岐をいれる
					if (!finCalData.isEmpty() && !finSaveData.isEmpty()) {//finにデータが入っている時
						//difference = unit(difference);//方向を正規化
						//今までのデータをfindataにいれる
						finCalData = finCalData + "\n" + time +"," +  tempData + "," + tempRSSI +"\n"+ "betu";
						//finSaveData = finSaveData + "\n" + tempData + "\n" + String.valueOf(difference[0]) + "," + String.valueOf(difference[1]) + "," + String.valueOf(difference[2]);
					} else {//finにデータが入っていない時-> finと足さずにいれる(改行されちゃうので)
						//difference = unit(difference);//方向を正規化
						finCalData =  tempData + "\n" + tempRSSI;
						//finSaveData =  tempData + "\n" + String.valueOf(difference[0]) + "," + String.valueOf(difference[1]) + "," + String.valueOf(difference[2]);
					}
				}

				//新しく書き込み
				tempData = strTag;
				tempRSSI = String.valueOf(rssi);

			*//*else{
				finCalData= tempData + "\n" + tempRSSI;
				finSaveData = tempData + "\n" + String.valueOf(difference[0]) + "," + String.valueOf(difference[1]) + "," + String.valueOf(difference[2]);
			}*//*
			}

			//方向リセット
			lastAccelTime = 0;
			int i = 0;
			for (i = 0; i < 3; i++) {
				speed[i] = 0;
				difference[i] = 0;
			}

			//新しい方向の基準を設定
			//N軸，E軸の設定
			orientationRad = (float) Math.toRadians(oriantation);    //degree[°]をradianへ 方位をラジアンに
			float oriNorth = (float) Math.PI / 2 + orientationRad;   //π/2+θ　方位を90°回転
			float oriEast = orientationRad;                         //θ 方位を入れる
			//N軸
			north[0] = (float) Math.cos(oriNorth);  //oriNorthをcosに射影したものを代入(x座標)
			north[1] = (float) Math.sin(oriNorth);  //oriNorthをsinに射影したものを代入(y座標)
			north[2] = -1.0f * (north[0] * gravity[0] + north[1] * gravity[1]) / gravity[2]; //これ何してるのかわかんないけど多分gravity?を頑張っている
			north = unit(north);    //正規化
			//E軸 北から-90°してる
			east[0] = (float) Math.cos(oriEast);//oriEastをcosに射影したものを代入(x座標)
			east[1] = (float) Math.sin(oriEast);
			east[2] = -1.0f * (east[0] * gravity[0] + east[1] * gravity[1]) / gravity[2];
			east = unit(east);  //正規化

			check=true;
		}
		//finData = finData + "ScanStart" + "\n" + strTag + "\n" + rssi + "\n" + time + "\n";

		else{//連続している時
			//前のデータセットに付け足し
			tempData=tempData+","+strTag;
			tempRSSI=tempRSSI+","+String.valueOf(rssi);
			//以下デバッグ用
			//ShowToast(String.valueOf(tempDataList));
			//finData = finData+"\n"+strTag+"\n"+rssi+"\n"+time +"\n";

			//初期化
			lastAccelTime = 0;
			int i = 0;
			for (i = 0; i < 3; i++) {
				speed[i] = 0;
				difference[i] = 0;
			}

			//新しい方向の基準を設定
			//N軸，E軸の設定
			orientationRad = (float) Math.toRadians(oriantation);    //degree[°]をradianへ 方位をラジアンに
			float oriNorth = (float) Math.PI / 2 + orientationRad;   //π/2+θ　方位を90°回転
			float oriEast = orientationRad;                         //θ 方位を入れる
			//N軸
			north[0] = (float) Math.cos(oriNorth);  //oriNorthをcosに射影したものを代入(x座標)
			north[1] = (float) Math.sin(oriNorth);  //oriNorthをsinに射影したものを代入(y座標)
			north[2] = -1.0f * (north[0] * gravity[0] + north[1] * gravity[1]) / gravity[2]; //これ何してるのかわかんないけど多分gravity?を頑張っている
			north = unit(north);    //正規化
			//E軸 北から-90°してる
			east[0] = (float) Math.cos(oriEast);//oriEastをcosに射影したものを代入(x座標)
			east[1] = (float) Math.sin(oriEast);
			east[2] = -1.0f * (east[0] * gravity[0] + east[1] * gravity[1]) / gravity[2];
			east = unit(east);  //正規化

			check=true;
		}
		pastTime = time;*/


		//listRefreshにデータを送る
		ListRefresh(pcEpc,rssi,df_NOT_INVALID_RFM,time);
	}


	//RSSIがないものを読み取った時に時に発生するイベント
	@Override
	public void onPcEpcSensorDataReceived(final int[] pcEpc,final int[] sensorData) {  // RFM
		//現在時刻を取得
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		//int型にする
		int time = (int) System.currentTimeMillis();

		//ShowToast(strtmp);

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

	//方向推定の時に必要な関数
	private float[] unit(float[] vec){ //入力されたベクトルを正規化
		float[] unitVec = new float[vec.length];
		float scalar = (float)Math.sqrt(Math.pow(vec[0],2) + Math.pow(vec[1],2) + Math.pow(vec[2],2));
		for(int i = 0; i < 3; i++){
			unitVec[i] = vec[i] / scalar;
		}
		return  unitVec;
	}

	private float[] rad2deg(float[] vec){ //入力された値をradianからdegreeに変換する関数
		int VEC_SIZE = vec.length;
		float[] retvec = new float[VEC_SIZE];
		for(int i = 0; i < VEC_SIZE; i++){
			retvec[i] = vec[i] / (float)Math.PI*180;
		}
		return retvec;
	}

	private UploadTask.Listener createListener() {
		return new UploadTask.Listener() {
			@Override
			public void onSuccess(String result) {
				ShowToast(result);
			}
		};
	}
}



