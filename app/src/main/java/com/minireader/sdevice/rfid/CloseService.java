package com.minireader.sdevice.rfid;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.asreader.sdevice.AsDeviceMngr;
import com.asreader.utility.Logger;

public class CloseService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onTaskRemoved (Intent rootIntent){
        Logger.getInstance().addLogData(2,"service Close session");
        AsDeviceMngr.getInstance().getOTG().setRfidPowerOn(false);
        AsDeviceMngr.getInstance().getOTG().close();
        this.stopSelf();
    }

}
