package com.minireader.sdevice.rfid;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.asreader.event.IOnAsDeviceRfidEvent;

public class SearchActivity extends AppCompatActivity implements IOnAsDeviceRfidEvent {

	 @Override
	 protected void onCreate(Bundle savedInstanceState){
	 	super.onCreate(savedInstanceState);
		 setContentView(R.layout.fragment_search);
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
}
