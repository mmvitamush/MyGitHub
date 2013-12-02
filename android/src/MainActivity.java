package com.example.jsongetapp;
import java.util.List;

import com.example.jsongetapp.RssLoadingAsyncTask.RssLoadingAsyncTaskListener;


import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
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
				Toast toast = Toast.makeText(MainActivity.this, "connected", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER,0,0);
				toast.show();
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
}
