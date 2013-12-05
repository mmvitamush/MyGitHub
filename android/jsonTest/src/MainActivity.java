package com.example.jsongetapp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.Calendar;

import java.text.SimpleDateFormat;

import com.example.jsongetapp.RssLoadingAsyncTask.RssLoadingAsyncTaskListener;


import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.ListView;
import android.view.Gravity;
import android.view.View.OnClickListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;

public class MainActivity extends Activity {
	private RssLoadingAsyncTaskListener mRssLoadingAsyncTaskListener = new RssLoadingAsyncTaskListener() {
		@Override
		public void onStartTask() {
			
		}
		
		@Override
		public void onEndTask(List<RssItem> items){
			Toast.makeText(MainActivity.this, "RssItem受信完了", Toast.LENGTH_LONG).show();
			ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
			for (RssItem d : items){
				HashMap<String,String> map = new HashMap<String,String>();
				map.put("line",String.valueOf(d.getLine()));
				map.put("lineno",String.valueOf(d.getLineno()));
				map.put("t_date",changeDatetime(d.getT_date()*1000));
				map.put("celsius",String.valueOf(d.getCelsius()));
				map.put("humidity",String.valueOf(d.getHumidity()));
				data.add(map);
			}
			 SimpleAdapter sa
				= new SimpleAdapter(MainActivity.this, data, R.layout.row, 
	                new String[]{"line", "lineno","t_date","celsius","humidity"},
	                new int[]{R.id.line, R.id.lineno,R.id.t_date,R.id.celsius,R.id.humidity}
	        );
			ListView lv = (ListView)findViewById(R.id.listView1);
			lv.setAdapter(sa);
		}
		
	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button button=(Button)findViewById(R.id.bt_greeting);
        button.setOnClickListener(new ButtonClickListener());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	class ButtonClickListener implements OnClickListener {
		public void onClick(View v) {
   			if (isConnected()){
				RssLoadingAsyncTask  task = new RssLoadingAsyncTask(mRssLoadingAsyncTaskListener);
				task.execute();
			} else {
				Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_LONG).show();
			}
		}
	}

	//ネットワーク接続状況の確認
    private boolean isConnected() {
        boolean connected = false;
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // ネットワークの接続状況を取得
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // ネットワークと接続済み
            connected = true;
        }

        return connected;
    }
    
    private String changeDatetime(long utime){
    	Date date = new Date(utime);
    	SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
    	/*Calendar cal = Calendar.getInstance();
    	cal.setTimeInMillis(utime);
    	int year = cal.get(Calendar.YEAR);
    	int month = cal.get(Calendar.MONTH);
    	int day = cal.get(Calendar.DAY_OF_MONTH);
    	int hour = cal.get(Calendar.HOUR_OF_DAY);
    	int min = cal.get(Calendar.MINUTE);
    	int sec = cal.get(Calendar.SECOND);
    	*/
    	return sdf.format(date);
    }
}

