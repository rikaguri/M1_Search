package com.minireader.sdevice.rfid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.asreader.common.AsDeviceConst;
import com.asreader.event.IOnAsDeviceRfidEvent;
import com.asreader.sdevice.AsDeviceMngr;
import com.asreader.util.Utils;

import java.util.ArrayList;


public class InfoActivity extends Activity implements IOnAsDeviceRfidEvent
{
	private Button back;
	private Button btn_refresh;

	private String strSWVersion;
	private String strHWVersion;
	private String strRFIDVersion;
	private TextView tvInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);

		back = (Button) findViewById(R.id.info_navigation_back_button);
		back.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				moveTaskToBack(false);
				finish();
			}
		});


		tvInfo = (TextView)findViewById(R.id.txt_info);


		btn_refresh = (Button) findViewById(R.id.btn_refresh);
		btn_refresh.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				tvInfo.setText("");
				AsDeviceMngr.getInstance().getOTG().getReaderInfo(0xb1);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				AsDeviceMngr.getInstance().getOTG().getReaderInfo(0x01);
			}
		});

	}




	@Override
	public void onRegionReceived(final int region) 
	{

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		strSWVersion = "\n APP: "+ Utils.getInstance().getAppVersion(this)+" \n SDK: "+ AsDeviceConst.strLibVersion;

		tvInfo.setText(strSWVersion+strHWVersion+strRFIDVersion);
		AsDeviceMngr.getInstance().setDelegateRFID(this);

		/* Request Reader's info */
		AsDeviceMngr.getInstance().getOTG().getReaderInfo(0xb1);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		/* Request RFID info */
		AsDeviceMngr.getInstance().getOTG().getReaderInfo(0x01);
	}


	@Override
	public void onReceivedRFIDModuleVersion(String s) {

		strRFIDVersion = "\n RFID : "+s;

		runOnUiThread(new Runnable()
		{
			public void run()
			{
				tvInfo.setText(strSWVersion+strHWVersion+strRFIDVersion);

			}
		});
	}

	@Override
	public void onReaderAboutInfo(String module, String hw, String fw, byte region) {

		/*Region =>  not Use */

		strHWVersion = "\n HW: "+hw+"\n FW: "+fw;

		runOnUiThread(new Runnable()
		{
			public void run()
			{
				tvInfo.setText(strSWVersion+strHWVersion+strRFIDVersion);
			}
		});

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
					
					AlertDialog.Builder alert = new AlertDialog.Builder(InfoActivity.this);
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
