package com.minireader.sdevice.rfid;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import static java.lang.Math.abs;

public class CalibrationActivity extends Activity implements SensorEventListener {
	private SensorManager sensorManager;
	Sensor aSensor;  // 加速度センサ TYPE_LINEAR_ACCELERATION
	Sensor grSensor; //重力センサ TYPE_GRAVITY
	Sensor gSensor; // ジャイロセンサ TYPE_GYROSCOPE
	Sensor mSensor; //地磁気センサ TYPE_MAGNETIC_FIELD
	Sensor oSensor; //方位センサ TYPE_ORIENTATION
	private TextView azimuth;

	TextView CalibrationComment;
	private Button Enter,finish;
	private int clickCounter;

	boolean check;
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

	//キャリブレーション結果をいれる変数
	public float[][] calibrate = new float[3][3];



	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibration);
		clickCounter=0;
		//センサーリスナー登録
		//方位推定用
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// 標準角加速度を登録
		// 引数を変えると違う種類のセンサ値を取得
		aSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		grSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		oSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		CalibrationComment=(TextView)findViewById(R.id.CalibrateText);

		azimuth = (TextView) findViewById(R.id.azimuth);//方位角

		//キャリブレーション終了時のボタン
		finish = (Button) findViewById(R.id.finish);
		finish.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//次のactivityに移行
				Intent intent= new Intent(getApplication(),SearchActivity.class);
				intent.putExtra("CalibrationData",calibrate);
				startActivity(intent);
			}
		});

		//キャリブレーション開始と終了ボタン
		Enter = (Button) findViewById(R.id.Check);
		//開始
		CalibrationComment.setText("東に10cm動かしてください");
		Enter.setText("Start");
		Enter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//何回クリックされたのか
				clickCounter++;
				switch (clickCounter){
					case 1:
					case 3:
					case 5:
						Enter.setText("Stop");
						CalibrationComment.setText("動かし終わったらボタンを押してください");
						check = true;
						break;
					case 2:
						//東に動かして終了、計測を終了して配列にいれる
						check = false;
						//記録
						calibrate[0][0]=difference[0];
						calibrate[0][1]=difference[1];
						calibrate[0][2]=difference[2];
						//次のやつ
						Enter.setText("Start");
						CalibrationComment.setText("北に10cm動かしてください");
						break;
					case 4:
						check=false;
						//記録
						calibrate[1][0]=difference[0];
						calibrate[1][1]=difference[1];
						calibrate[1][2]=difference[2];
						//次のやつ
						Enter.setText("Start");
						CalibrationComment.setText("上に10cm動かしてください");
						break;
					case 6:
						check=false;
						Enter.setVisibility(View.INVISIBLE);
						//記録
						calibrate[2][0]=difference[0];
						calibrate[2][1]=difference[1];
						calibrate[2][2]=difference[2];
						//キャリブレーション終了
						finish.setVisibility(View.VISIBLE);
						break;


					default:
						throw new IllegalStateException("Unexpected value: " + clickCounter);
				}
			}
		});


	}

	// Activityが表示された時
	@Override
	protected void onResume() {
		super.onResume();
		// registerListener：監視を開始
		// SENSOR_DELAY_UIを変更すると更新頻度が変わる SENSOR_DELAY_FASTEST:0ms,
		// SENSOR_DELAY_GAME:20ms, SENSOR_DELAY_UI:60ms, SENSOR_DELAY_NORMAL:200ms
		sensorManager.registerListener(this, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, grSensor, SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, gSensor, SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, oSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	// 解除するコード
	@Override
	protected void onPause() {
		super.onPause();
		// Listenerを解除
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		int i = 0;

		switch (event.sensor.getType()){

			case Sensor.TYPE_LINEAR_ACCELERATION:   //重力を除く加速度m/s^2

				accelValue = event.values.clone();

				if(abs(gravity[2]) < 0.5){
					//スマホが垂直な時は方向算出しない
					//diffFlag = false;
					azimuth.setText("gravity[2] < 0.5");
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
				azimuth.setText("方位:" + String.valueOf(oriantation));
				break;


			case Sensor.TYPE_GRAVITY:   //重力を取得
				gravity = event.values.clone();
				//gravity = unit(gravity);    //正規化
				break;




			default:
				return;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
