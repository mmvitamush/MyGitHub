package com.example.navigationdrawertest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.example.navigationdrawertest.R.drawable;
import com.example.navigationdrawertest.RssLoadingAsyncTask.RssLoadingAsyncTaskListener;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Layout.Alignment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class TableFragment extends Fragment {
	private RssLoadingAsyncTaskListener mRssLoadingAsyncTaskListener = new RssLoadingAsyncTaskListener() {
		@Override
		public void onStartTask() {
			
		}
		
		@Override
		public void onEndTask(List<RssItem> items){
			for (RssItem d : items){
				createTableLayout(d);
			}
			
		}
		
	};
	
	@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	        View v = inflater.inflate(R.layout.fragment_table, container, false);
	        Button button=(Button)v.findViewById(R.id.bt_table_fragment);
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
	
	//受信ﾃﾞｰﾀからTableを動的に作成しセットする
	private void createTableLayout(RssItem item) {
			TableLayout tableLayout = (TableLayout) getActivity().findViewById(R.id.TableLayout1);
			TableRow tableRow1 = new TableRow(getActivity());
			tableRow1.setBackgroundColor(Color.rgb(224,224,224));
			tableLayout.addView(tableRow1);
			tableRow1.addView(createTextView(String.valueOf(item.getLine()) ));
			tableRow1.addView(createTextView(String.valueOf(item.getLineno()) ));
			tableRow1.addView(createTextView(changeDatetime(item.getT_date()*1000) ));
			tableRow1.addView(createTextView(String.valueOf(item.getCelsius()) ));
			tableRow1.addView(createTextView(String.valueOf(item.getHumidity()) ));
	}
	
	//TextViewを動的生成
	private TextView createTextView(String str){
		TextView tv = new TextView(getActivity());
		tv.setText(str);
		tv.setTextSize(24);
		tv.setPadding(15, 15, 15, 15);
		tv.setGravity(Gravity.RIGHT);
		tv.setBackgroundResource(R.drawable.cell_shape);
		
		return tv;
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
