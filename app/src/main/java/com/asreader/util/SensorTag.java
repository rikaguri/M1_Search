package com.asreader.util;

import android.util.Log;

/**
 * Created by sps on 2018. 10. 24..
 */

public class SensorTag {

    private static final String TAG = "SensorTag";
    public  static int sensorCop = 0;
    public  static double sensorCfq = 0;
    public  static int sensorCt = 0;
    public  static double sensorTemp = 0;

    static com.asreader.util.SensorTag instance = null;

    private void SensroTag(){}

    public static com.asreader.util.SensorTag getInstance()
    {
        if(instance == null)
            instance = new com.asreader.util.SensorTag();
        return  instance;
    }


    public boolean parseSensorData(int[] data)
    {

        byte[] btSensor = new byte[data.length];
        for(int i=0; i<data.length; i++)
            btSensor[i] = (byte)(data[i]&0xFF);

        getSensorData(btSensor);

        return  true;
    }


    private boolean getSensorData(byte[] btSensor)
    {
        sensorCop       =  (btSensor[0] <<8)|btSensor[1]&0xFF;
        int frequency_h =  (btSensor[2] <<8)|(btSensor[3]&0xFF);
        int frequency_l =  (btSensor[4]<<8)|(btSensor[5]&0xFF);

        sensorCfq = frequency_h + (frequency_l/1000.0);
        sensorCt  = btSensor[6];

        boolean success = true;
        switch (sensorCt) {
            //Magnus S3 type Sensor TAG
            case 0x03:
                sensorTemp = RFMMagnusS3Temp(btSensor, 11);
                break;

            default:
                success = false;
                break;
        }
        return success;
    }


    private double RFMMagnusS3Temp(byte[] ba, int typeIdx)
    {
        double result = 0;
        int offset = 0;
        int temp = 0;

        double tempCode_ = 0;

        double code1_ = 0;
        double code2_ = 0;

        double temp1_ = 0;
        double temp2_ = 0;

        offset = typeIdx;
        tempCode_ = ba[offset + 0] << 8 | ba[offset + 1]&0xFF;

        temp = ba[offset + 4]&0xFF;
        temp = (temp << 4) + ((ba[offset + 5] >> 4) & 0x0F);
        code1_ = temp;

        temp = ba[offset + 5] & 0x0F;
        temp = (temp << 7) + ((ba[offset + 6] >> 1) & 0x7F);
        temp1_ = temp;

        temp = ba[offset + 6] & 0x01;
        temp = (temp << 8) + ba[offset + 7];
        temp = (temp << 3) + ((ba[offset + 8] >> 5) & 0x07);
        code2_ = temp;

        temp = ba[offset + 8] & 0x1F;
        temp = (temp << 6) + ((ba[offset + 9] >> 2) & 0x3F);
        temp2_ = temp;

        result = ((temp2_ - temp1_) / (code2_ - code1_) * (tempCode_ - code1_) + temp1_ - 800) / 10;
        return result;
    }
}
