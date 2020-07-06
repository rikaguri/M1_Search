package com.minireader.sdevice.rfid;

import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.asreader.component.ListData;
import com.asreader.event.IOnAsDeviceRfidEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;

public class SearchFragment extends Fragment implements IOnAsDeviceRfidEvent, SensorEventListener {

	/*SensorManager manager;
	Sensor aSensor;  // 加速度センサ
	Sensor gSensor; // ジャイロセンサ
	Sensor mSensor; //地磁気センサ

	*//**データ格納用*//*
	CsvReader csvReader = new CsvReader();
	ListData list = new ListData();

	*//** 向き表示*//*
	String targetTag;
	private ImageView imageView;
	//Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.drawable.arrow); //bmpにarrowを代入
	TextView dist;
	Matrix matrix;
	private int imageWidthCenter;
	private int imageHeightCenter;
	//private  Bitmap bmp2;

	*//**音 *//*
	private SoundPool mSoundPool;
	private int OK;
	private int near;

	*//**振動*//*
	private Vibrator vibrator;
	private long pattern[] = {1000, 200, 700, 200, 400, 200 };

	//移動方向推定
	//姿勢推定
	float filterCoefficient = 0.9f; //ローパスフィルタ係数
	private float[] accelValue = new float[3];  //加速度センサ
	private float[] gravity = new float[3];    //加速度ローパスフィルタ
	private float[] accel = new float[4];   //加速度ハイパスフィルタ
	private float[] worldAccel = new float[3];  //世界座標軸の加速度
	private float[] exWorldAccel1 = new float[3];   //一つ前の世界座標軸加速度
	private float[] gyroValue = new float[3];      //ジャイロセンサ
	private float[] magneticValue = new float[3];   //地磁気センサ
	private float[] magnetic = new float[3];    //地磁気ローパスフィルタ
	private float[] oriantation = new float[3]; //方位角（degree）
	private static final int MATRIX_SIZE = 16;
	private float[] accelerometerReading = new float[3];
	private float[] magnetometerReading = new float[3];
	private final float[] rotationMatrix = new float[9];

	private final float[] orientationAngles = new float[3];
	//ノルム計算
	private float[] norm = new float[3];    //最新3つのノルム
	private boolean max_flag1 = true;
	private boolean max_flag2 = false;
	private float peak = 0; //ピーク値
	private float[] peak1 = new float[2];   //一つ目のピーク値成分
	private float[] peak2 = new float[3];   //二つ目のピーク値成分
	private float[] mMagnetometerReading = new float[3];
	private float[] mOrientationAngles = new float[3];


	*//** ログタブ用 *//*
	private Button mBtnLogClear;    //ログ消去
	private ListView mListLog;      //ログリスト
	private ArrayList<HashMap<String, String>> mAarryLog;   //ログ格納配列
	private BaseAdapter mAdapterLog;    //ListViewと一緒に使うと画像、文字のリスト表示ができる

	*//** ファイルアップロード用*//*
	private UploadTask task;
	String uploadTXT = "";
	Timer timer;


	*//**
	 * センサー
	 *//*
	//@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	public void onSensorChanged(SensorEvent event) {
		// 時刻記録
		Calendar time = Calendar.getInstance();
		int hour = time.get(Calendar.HOUR_OF_DAY);
		int minute = time.get(Calendar.MINUTE);
		int second = time.get(Calendar.SECOND);
		int ms = time.get(Calendar.MILLISECOND);

		String nowtime = hour +":"+ minute +":"+ second +"."+ ms;
		timestamp.setText(nowtime);

		int i = 0;


		// 加速度センサの値が変更
		switch (event.sensor.getType()){
			case Sensor.TYPE_ACCELEROMETER:
				//加速度センサ
				System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
				accelValue = event.values.clone();
				//ハイパスフィルタとローパスフィルタにかけて重力加速度を取り除く
				for(i = 0; i <= 2; i++) {
					//ローパスフィルター(ノイズを取り除く)
					//現在の値 = 係数 * ひとつ前の値 + (1 - 係数) * センサの値
					gravity[i] = gravity[i] * filterCoefficient + event.values[i] * (1 - filterCoefficient);
					//ハイパスフィルター(重力加速度を取り除く)
					accel[i] = event.values[i] - gravity[i];
				}
				break;

			case Sensor.TYPE_GYROSCOPE:
				//ジャイロセンサ
				gyroValue = event.values.clone();
				break;

			case Sensor.TYPE_MAGNETIC_FIELD:
				//地磁気センサ
				System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.length);
				magneticValue = event.values.clone();
				//ローパスフィルタにかける（ノイズ除去）
				for(i = 0; i <= 2; i++){
					//現在の値 = 係数 * ひとつ前の値 + (1 - 係数) * センサの値
					magnetic[i] = magnetic[i] * filterCoefficient + event.values[i] * (1 - filterCoefficient);
				}
				break;
			default:
				return;
		}

		if(magnetic != null && accel != null){
			float[] R = new float[MATRIX_SIZE]; //回転行列
			float[] I = new float[MATRIX_SIZE]; //伏角行列（水平面からのズレの角度）
			float[] rR = new float[MATRIX_SIZE];    //ワールド軸
			float[] oriRad = new float[3];  //方位角（デバイスの向き）
			float[] rotationMatrix = new float[9];
			//重力、磁力を地球基準の直行座標系の地場に変換するための伏角行列と回転行列を計算
			SensorManager.getRotationMatrix(R, I, gravity, magnetic);
			//回転行列を異なる座標系での回転量に変換
			SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, rR);
			//デバイスの向き（方位角）を取得
			SensorManager.getOrientation(rR, oriRad);
			oriantation = rad2deg(oriRad);

			//android公式でのやり方
			SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, mMagnetometerReading);
			SensorManager.getOrientation(rotationMatrix, mOrientationAngles);


			//回転行列の逆行列を求める
//                float[] invertR = new float[16];
//                Matrix.invertM(invertR, 0, R, 0);
			//加速度に回転行列をかける
			float[] acc4 = new float[4];
			android.opengl.Matrix.multiplyMV(acc4, 0, R, 0, accel, 0);
			//世界軸の加速度を取得
			for(i = 0; i < 3; i++){
				exWorldAccel1[i] = worldAccel[i];
				worldAccel[i] = acc4[i];
			}
		}

		//ノルム計算
		for(i = 0; i < 3; i++){
			if(i != 2){
				norm[i] = norm[i+1];
			}
			else{
				norm[i] = (float)Math.sqrt(Math.pow(worldAccel[0],2) + Math.pow(worldAccel[1],2));
			}
		}

		//極大値検索
		if(norm[0] < norm[1] && norm[1] > norm[2]){

		}
		if(max_flag1 == true){

		}

		//ステップ検出
		if(exWorldAccel1[2] < 0.8f && worldAccel[2] > 0.8f){
			if(step_flag == true){
				step = step + 1;
				step_flag = false;
				//移動方向の算出をする
			}
		}
		else if(worldAccel[2] < -0.5f){
			if(step_flag == false){
				step_flag = true;
			}
		}


//            // SDカードに記録
//            String gFILE = "/sdcard/world.csv";
//            try {
//                FileOutputStream fileOutputStream = new FileOutputStream(gFILE, true);
//	            OutputStreamWriter outputStreamWriter = null;
//	            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//		            outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
//	            }
//	            BufferedWriter bw = new BufferedWriter(outputStreamWriter);
//                String write_int = nowtime + "," +
//		                worldAccel[0] + "," +
//		                worldAccel[1] + "," +
//		                worldAccel[2] + "," +
//		                oriantation[0] + "\n";
//                bw.write(write_int);
//                bw.close();
//            } catch (UnsupportedEncodingException k){
//                k.printStackTrace();
//            } catch (FileNotFoundException k){
//                k.printStackTrace();
//            } catch (IOException k){
//                k.printStackTrace();
//            }

		// テキスト表示
		ax_TextView.setText(String.valueOf(worldAccel[0]));
		ay_TextView.setText(String.valueOf(worldAccel[1]));
		az_TextView.setText(String.valueOf(worldAccel[2]));
		ori_TextView.setText(String.valueOf(oriantation[0]));
		step_TextView.setText(String.valueOf(step));
//            gy_TextView.setText(String.valueOf(magneticValues[1]));
//            gz_TextView.setText(String.valueOf(magneticValues[2]));
//            accely_TextView.setText(String.valueOf(difference));
//            degreez_TextView.setText(String.valueOf(degree));

	}

	private float[] rad2deg(float[] vec){
		//入力された値をradianからdegreeに変換
		int VEC_SIZE = vec.length;
		float[] retvec = new float[VEC_SIZE];
		for(int i = 0; i < VEC_SIZE; i++){
			retvec[i] = vec[i] / (float)Math.PI*180;
		}
		return retvec;
	}

	// Compute the three orientation angles based on the most recent readings from
	// the device's accelerometer and magnetometer.
	public void updateOrientationAngles() {
		// Update rotation matrix, which is needed to update orientation angles.
		SensorManager.getRotationMatrix(rotationMatrix, null,
				accelerometerReading, mMagnetometerReading);

		// "mRotationMatrix" now has up-to-date information.

		SensorManager.getOrientation(rotationMatrix, mOrientationAngles);

		// "mOrientationAngles" now has up-to-date information.
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}*/

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.fragment_search,container,false);
		return v;
	}

	 //Viewが生成し終わった時に呼ばれるメソッド
	 @Override
	 public void onViewCreated(View view,Bundle savedInstanceState){

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

	@Override
	public void onSensorChanged(SensorEvent event) {

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
