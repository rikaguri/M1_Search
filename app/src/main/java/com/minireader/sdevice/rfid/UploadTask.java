package com.minireader.sdevice.rfid;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/*
 * AsyncTask<型1, 型2,型3>
 *
 *   型1 … Activityからスレッド処理へ渡したい変数の型
 *          ※ Activityから呼び出すexecute()の引数の型
 *          ※ doInBackground()の引数の型
 *
 *   型2 … 進捗度合を表示する時に利用したい型
 *          ※ onProgressUpdate()の引数の型
 *
 *   型3 … バックグラウンド処理完了時に受け取る型
 *          ※ doInBackground()の戻り値の型
 *          ※ onPostExecute()の引数の型
 *
 *   ※ それぞれ不要な場合は、Voidを設定すれば良い
 */

public class UploadTask extends AsyncTask<String,Void,String> {

	private Listener listener;

	//非同期処理
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	@Override
	protected String doInBackground(String... params) {
		// 使用するサーバーのURLに合わせる
		String urlSt = "http://192.168.10.102/test/makeGraph/testlog";

		HttpURLConnection httpConn = null;
		String result = null;

		StringBuilder word = new StringBuilder();
		for(int i=0;i<params.length;i++){
			word.append(params[i]);
		}

		try{
			// URL設定
			URL url = new URL(urlSt);

			// HttpURLConnection
			httpConn = (HttpURLConnection) url.openConnection();

			// request POST
			httpConn.setRequestMethod("POST");

			// no Redirects
			httpConn.setInstanceFollowRedirects(false);

			// データを書き込む
			httpConn.setDoOutput(true);

			// 時間制限
			httpConn.setReadTimeout(10000);
			httpConn.setConnectTimeout(20000);

			// 接続
			httpConn.connect();

			try(// POSTデータ送信処理
			    OutputStream outStream = httpConn.getOutputStream()) {
				outStream.write( word.toString().getBytes(StandardCharsets.UTF_8));
				outStream.flush();
				Log.d("debug","flush");
			} catch (IOException e) {
				// POST送信エラー
				e.printStackTrace();
				result = "POST送信エラー";
			}

			final int status = httpConn.getResponseCode();
			if (status == HttpURLConnection.HTTP_OK) {
				// レスポンスを受け取る処理等
				result="HTTP_OK";
			}
			else{
				result="status="+String.valueOf(status);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (httpConn != null) {
				httpConn.disconnect();
			}
		}
		return result;
	}

	// 非同期処理が終了後、結果をメインスレッドに返す
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);

		if (listener != null) {
			listener.onSuccess(result);
		}
	}

	void setListener(Listener listener) {
		this.listener = listener;
	}

	interface Listener {
		void onSuccess(String result);
	}
}
