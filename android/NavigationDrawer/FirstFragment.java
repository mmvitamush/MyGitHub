package com.example.navigationdrawertest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.example.navigationdrawertest.RssLoadingAsyncTask.RssLoadingAsyncTaskListener;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class FirstFragment extends Fragment {
	private RssLoadingAsyncTaskListener mRssLoadingAsyncTaskListener = new RssLoadingAsyncTaskListener() {
		@Override
		public void onStartTask() {
			
		}
		
		@Override
		public void onEndTask(List<RssItem> items){
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
				= new SimpleAdapter(getActivity(), data, R.layout.row, 
	                new String[]{"line", "lineno","t_date","celsius","humidity"},
	                new int[]{R.id.line, R.id.lineno,R.id.t_date,R.id.celsius,R.id.humidity}
	        );
			ListView lv = (ListView)getActivity().findViewById(R.id.listView1);
			lv.setAdapter(sa);
		}
		
	};
	
	@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	        View v = inflater.inflate(R.layout.fragment_first, container, false);
	        Button button=(Button)v.findViewById(R.id.bt_greeting);
	        button.setOnClickListener(new View.OnClickListener(){
	        	@Override
	        	public void onClick(View view){
	        		if (isConnected()){
	    				RssLoadingAsyncTask  task = new RssLoadingAsyncTask(mRssLoadingAsyncTaskListener);
	    				task.execute();
	    			} else {
	    				Toast.makeText(getActivity(), "Failed", Toast.LENGTH_LONG).show();
	    			}
	        	}
	        });
	        
	        return v;
	}
	
	
	//ネットワーク接続状況の確認
    private boolean isConnected() {
        boolean connected = false;
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

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

    	return sdf.format(date);
    }
}
