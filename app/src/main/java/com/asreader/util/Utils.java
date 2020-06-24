package com.asreader.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.asreader.utility.Logger;
import com.minireader.sdevice.rfid.R;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaPlayer;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

public class Utils {

	private static final Utils mInstance     = new Utils();
	private Logger tagWriter  = new Logger();
	private static String strFilePath = "";
	public static Utils getInstance()
	{
		
		return mInstance;
	}


	public String getAppVersion(Activity act)
	{
		PackageInfo pInfo = null;
		try
		{
			pInfo = act.getPackageManager().getPackageInfo(
					act.getPackageName(), 0);
		}
		catch (NameNotFoundException e1)
		{
			e1.printStackTrace();
		}
		final String version = pInfo.versionName;		
		return version;
	}

	public String getIpAdress(WifiManager wm)
	{
		String ipAddress = "";
		if(wm!=null)
		{
			WifiInfo wifiInfo = wm.getConnectionInfo();
			if(wifiInfo!=null)
			{
				int serverIp = wifiInfo.getIpAddress();
				ipAddress = String.format(
						"%d.%d.%d.%d",
						(serverIp & 0xff),
						(serverIp >> 8 & 0xff),
						(serverIp >> 16 & 0xff),
						(serverIp >> 24 & 0xff));
			}
		}
		return ipAddress;
	}
	
	
	public void showToast(final Activity act,final String strMessag)
	{
		Toast.makeText(act.getApplicationContext(), strMessag, Toast.LENGTH_SHORT).show();
	}
	
	
	MediaPlayer mp = null;
	public void playSound(final Activity act)
	{
		if(mp == null)
			mp = MediaPlayer.create(act.getApplicationContext(), R.raw.read);
		
		new Thread(new Runnable() 
		{
			@Override
			public void run() 
			{

				mp.start();
				try
				{
					Thread.sleep(10);
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	
	
	
	
}
