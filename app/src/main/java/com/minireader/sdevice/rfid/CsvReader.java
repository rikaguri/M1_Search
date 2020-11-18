package com.minireader.sdevice.rfid;

import android.content.Context;
import android.content.res.AssetManager;

import com.asreader.component.ListData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {
	List<ListData> objects = new ArrayList<>();
	public void reader(Context context){
		AssetManager assetManager = context.getResources().getAssets();
		try {
			// CSVファイルの読み込み
			InputStream inputStream = assetManager.open("virtual_data.csv");
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferReader = new BufferedReader(inputStreamReader);
			String line = "";
			while ((line = bufferReader.readLine()) != null) {

				//カンマ区切りで１つづつ配列に入れる
				ListData data = new ListData();
				String[] RowData = line.split(",");

				//CSVの左([0]番目)から順番にセット
				data.setTag1(RowData[0]);
				data.setTag2(RowData[1]);
				data.setTheta(RowData[2]);
				data.setDist(RowData[3]);

				objects.add(data);
			}
			bufferReader.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

}
