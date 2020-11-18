package com.minireader.sdevice.rfid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.asreader.component.ListData;
import com.asreader.event.IOnAsDeviceRfidEvent;
import com.asreader.event.IOnOtgEvent;
import com.asreader.sdevice.AsDeviceMngr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import static java.lang.Math.abs;
import static java.nio.charset.StandardCharsets.UTF_8;

public class SearchActivity extends Activity implements IOnAsDeviceRfidEvent, SensorEventListener {

	/** データ格納用*/
	CsvReader csvReader = new CsvReader();
	ListData list = new ListData();

	/** 向き表示*/
	String targetTag;
	private ImageView imageView;
	//Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.drawable.arrow); //bmpにarrowを代入
	TextView dist;
	Matrix matrix;
	private int imageWidthCenter;
	private int imageHeightCenter;
	//private  Bitmap bmp2;

	/** 音 */
	private SoundPool mSoundPool;
	private int OK;
	private int near;

	/** 振動*/
	private Vibrator vibrator;
	private long[] pattern = {1000, 200, 700, 200, 400, 200 };

	/**タグのデータをcsvに格納するためのデータ*/
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

	/** POST通信用データ*/
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
	private SharedPreferences mPrefs = null;
	private MainActivity.ScreenOnReceiver screenOnReceiver = null;

	/** 物探し用のクラス*/
	public class MyTagData {
		public Integer time;
		public String name;
		public float rssi;
		public String x_vec;
		public String y_vec;
		public String z_vec;
	}

	/** 入力された値をradianからdegreeに変換*/
	private float[] rad2deg(float[] vec){
		int VEC_SIZE = vec.length;
		float[] retvec = new float[VEC_SIZE];
		for(int i = 0; i < VEC_SIZE; i++){
			retvec[i] = vec[i] / (float)Math.PI*180;
		}
		return retvec;
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		// センサーマネージャの取得
		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		// 標準角加速度を登録
		// 引数を変えると違う種類のセンサ値を取得
		aSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		/**データ格納用*/
		csvReader.reader(getApplicationContext());

		/** 向き表示用*/
		targetTag = "0800004d";
		imageView=findViewById(R.id.image_view);
		dist = findViewById(R.id.dist);
		Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),R.drawable.arrow);
		imageWidthCenter = bitmap1.getWidth()/2 ;//横幅の中心の座標を求める
		imageHeightCenter = bitmap1.getHeight()/2;//縦幅の中心の座標を求める

		matrix = new Matrix();//Matrixインスタンス作成

		imageView.setImageBitmap(bitmap1);


		/**音*/
		mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
		//OK = mSoundPool.load(getApplicationContext(),R.raw.ok,0);
		//near = mSoundPool.load(getApplicationContext(),R.raw.near,0);

		/**振動*/
		vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);

		initial();

	}

	/* Initial required declaration when using OTG and RFID events. */
	private void initial()
	{
		//for receive Broadcast event about OTG Plug and Unplug
		AsDeviceMngr.getInstance().setEnableBroadCastReceiver(this);

		//OTG Plug, UnPlug Event
		AsDeviceMngr.getInstance().setDelegateOTG((IOnOtgEvent) this);

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

		//buttonControl(3);
		//ShowToast("Power ON");
		AsDeviceMngr.getInstance().getOTG().setPowerWithOption(true,beep,vib,led,illu,powerOnBeep,0);

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
	public void onReceivedRFIDModuleVersion(String s) {

	}

	@Override
	public void onReaderAboutInfo(String s, String s1, String s2, byte b) {

	}

	@Override
	public void onResetReceived() {

	}

	@Override
	public void onSuccessReceived(int[] ints, int i) {

	}

	@Override
	public void onFailureReceived(int[] ints) {

	}

	@Override
	public void onReaderInfoReceived(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9) {

	}

	@Override
	public void onReceivedLeakageData(int i, int i1, int i2, int i3, int i4, int i5, int i6) {

	}

	@Override
	public void onBatteryStateReceived(int i, int i1) {

	}

	@Override
	public void onReceiveAntimode(int i, int i1, int i2, int i3, int i4) {

	}

	@Override
	public void didSetOptiFreqHPTable(int i) {

	}

	@Override
	public void didSetSmartMode(int i) {

	}

	@Override
	public void onReceiveSmartMode(int i) {

	}

	@Override
	public void didUpdateRegistry(int i) {

	}

	@Override
	public void didReceiveRegion(int i) {

	}

	@Override
	public void onRegionReceived(int i) {

	}

	@Override
	public void onSessionReceived(int i) {

	}

	@Override
	public void onChannelReceived(int i, int i1) {

	}

	@Override
	public void onTxPowerLevelReceived(int i, int i1, int i2) {

	}

	@Override
	public void onFhLbtReceived(int i, int i1, int i2, int i3, int i4, int i5, int i6) {

	}

	@Override
	public void onTagReceived(int[] ints) {

	}

	@Override
	public void onSelectParamReceived(int[] ints) {

	}

	@Override
	public void onTagWithTidReceived(int[] ints, int[] ints1) {

	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	@Override
	public void onTagWithRssiReceived(int[] ints, int i) {

	}

	@Override
	public void onQueryParamReceived(int[] ints) {

	}

	@Override
	public void onTagMemoryReceived(int[] ints) {

	}

	@Override
	public void onTagMemoryLongReceived(int[] ints) {

	}

	@Override
	public void onPcEpcSensorDataReceived(int[] ints, int[] ints1) {

	}

	/** 物探しユーザー表示用*/
	synchronized public void Search(){
		//		//Log.d(TAG, "onTriggerChaned(" + trigger + ")");
//		final Handler mainHandler = new Handler(Looper.getMainLooper());
//		//読み取り音をスマホからさせる
//		mSoundPool.play(OK,1.0f, 1.0f,0,0,1.0F);
//
		/** タグを読み取ったら疑似距離と疑似方向を取ってきて目的タグの方向に矢印を描写するプログラム */
		//もしターゲットタグが読み取れたら、バイブと音
//		rssiMax = 0;
//		updateOrientationAngles();
//		//Log.d(TAG, String.valueOf(csvReader.objects.size()));

		/** 電波強度がMAXのものをget */
//		for (int i = 0; i < one_scan.size() - 1; i++) {
//			if (Float.valueOf((one_scan.get(i)).get("RSSI").toString()) > Float.valueOf((one_scan.get(rssiMax)).get("RSSI").toString())) {
//				rssiMax = i;
//			}
//			if ((((one_scan.get(i)).get("ID")).toString()).equals(targetTag)) {//目的のタグが読み取れたら
//				Log.d(TAG, (((one_scan.get(i)).get("ID")).toString()));
//				mainHandler.post(new Runnable() {
//					@Override
//					public void run() {
//						imageView.setVisibility(View.GONE);
//						dist.setText("近くにあります!!!");
//						dist.setTextSize(50.0f);
//						//dist.setGravity(Gravity.CENTER);
//					}
//				});
//				if (Float.valueOf(((one_scan.get(i)).get("RSSI")).toString()) > -65) {//近かったら長めのバイブ
//					vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
//					mSoundPool.play(near, 2.0F, 2.0F, 0, 0, 1.0F);
//				} else {//遠かったら短めのバイブ
//					vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
//					mSoundPool.play(near, 1.0F, 1.0F, 0, 0, 1.0F);
//				}
//			}
//			break;
//		}
//		//目的のタグが読み取れなかった
//		for (int i = 0; i < csvReader.objects.size(); i++) {//CSVを探して目的のタグと今いるタグの方向と距離を取ってくる
//			if (((one_scan.get(rssiMax).get("ID").toString().equals(csvReader.objects.get(i).getTag1())) && targetTag.equals(csvReader.objects.get(i).getTag2()))) {
//				//matrix.setRotate(Float.valueOf(csvReader.objects.get(i).theta) - oriantation[0], imageWidthCenter, imageHeightCenter);
//				Log.d(TAG, "tag1は" + csvReader.objects.get(i).getTag1() +"tag2は"+ csvReader.objects.get(i).getTag2());
//				imageView.setVisibility(View.VISIBLE);
//				//imageView.setRotation(90);
//				imageView.setRotation(Float.parseFloat(csvReader.objects.get(i).getTheta()) - oriantation[0]);
//				Log.d(TAG, "取って来た方向データ"+csvReader.objects.get(i).getTheta());
//				Log.d(TAG, "向いている方向"+ oriantation[0]);
//				//imageView.setRotation(Float.parseFloat(csvReader.objects.get(i).getTheta())*-1);
//				dist.setTextSize(100.0f);
//				dist.setText(csvReader.objects.get(i).getDist() + "cm");
//			}
//			else if( one_scan.get(rssiMax).get("ID").toString().equals(csvReader.objects.get(i).getTag2()) && targetTag.equals(csvReader.objects.get(i).getTag1())){//
//				// matrix.setRotate(Float.valueOf(csvReader.objects.get(i).theta) - oriantation[0], imageWidthCenter, imageHeightCenter);
//				Log.d(TAG, "tag1は" + csvReader.objects.get(i).getTag1() +"tag2は"+ csvReader.objects.get(i).getTag2());
//				imageView.setVisibility(View.VISIBLE);
//				//imageView.setRotation(90);
//				imageView.setRotation(Float.parseFloat(csvReader.objects.get(i).getTheta())*(-1) - oriantation[0]);
//				Log.d(TAG, "取って来た方向データ"+"-"+csvReader.objects.get(i).getTheta());
//				Log.d(TAG, "向いている方向"+ oriantation[0]);
//				//imageView.setRotation(Float.parseFloat(csvReader.objects.get(i).getTheta())*-1);
//				dist.setTextSize(100.0f);
//				dist.setText(csvReader.objects.get(i).getDist() + "cm");
//			}
//		}
		//一回で読み込めたスキャンリストを初期化
		//one_scan = new ArrayList<Map<String, Object>>();

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
						//ENG軸に変換
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
	//方向推定の時に必要な関数
	private float[] unit(float[] vec){ //入力されたベクトルを正規化
		float[] unitVec = new float[vec.length];
		float scalar = (float)Math.sqrt(Math.pow(vec[0],2) + Math.pow(vec[1],2) + Math.pow(vec[2],2));
		for(int i = 0; i < 3; i++){
			unitVec[i] = vec[i] / scalar;
		}
		return  unitVec;
	}


	private UploadTask.Listener createListener() {
		return new UploadTask.Listener() {
			@Override
			public void onSuccess(String result) {
				ShowToast(result);
			}
		};
	}

	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	private void CSVcreator(String output, String data){
		//ファイルの読み書きの話
		File path = getExternalFilesDir(null);
		File file = new File(path, output);
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
			data = data+"\n";
			bw.write(data);
			bw.flush();
			bw.close();
			ShowToast("STOP"+ data);
		}catch(Exception e){
			e.printStackTrace();
			ShowToast("NG"+ data);
		}
	}

	/** コメント　*/
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
}
