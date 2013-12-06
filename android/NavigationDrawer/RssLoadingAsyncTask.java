package com.example.navigationdrawertest;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;

//import android.app.Activity;
import android.os.AsyncTask;
import android.net.http.AndroidHttpClient;

public class RssLoadingAsyncTask extends AsyncTask<String, Void, List<RssItem>> {
			RssLoadingAsyncTaskListener mRssLoadingAsyncTaskListener;
		
		    public interface RssLoadingAsyncTaskListener {
		        public void onStartTask();
		
		        public void onEndTask(List<RssItem> item);
		    }
		    
		    public RssLoadingAsyncTask(RssLoadingAsyncTaskListener listener){
		        mRssLoadingAsyncTaskListener = listener;
		    }
	
			@Override
		    protected List<RssItem> doInBackground(String... urls) {
		        List<RssItem> rssItemList = null;
		        if (isCancelled()) {
		            // キャンセルされた場合は終了
		        } else {
		
		        }
		        String url = "http://www.vita-factory.com/api/getchart";
		        rssItemList = getRssItems(url);
	
		        return rssItemList;
		    }
			
			@Override
		    protected void onPostExecute(List<RssItem> result) {
		        mRssLoadingAsyncTaskListener.onEndTask(result);
		    }
			
			private List<RssItem> getRssItems(String url) {
		        List<RssItem> items = null;
		        InputStream inputStream;
		        // HTTPクライアントのインスタンス取得
		        AndroidHttpClient httpClinet =
		                AndroidHttpClient.newInstance("Android");
		        // パラメータの設定
		        HttpConnectionParams.setConnectionTimeout(
		                httpClinet.getParams(), 10000);
		        HttpConnectionParams.setSoTimeout(httpClinet.getParams(), 10000);
		        // HttpGetのリクエスト生成
		        HttpPost request = new HttpPost(url);
		        List<NameValuePair> params = new ArrayList<NameValuePair>();
		        //POSTﾊﾟﾗﾒｰﾀの設定
		        params.add(new BasicNameValuePair("start","2013/01/01 00:00:00"));
		        params.add(new BasicNameValuePair("end","2013/12/31 23:59:59"));
		        try{
		        	request.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
		        } catch (UnsupportedEncodingException e){
		        	e.printStackTrace();
		        }
		        
		        try {
		            HttpResponse response;
		            // HttpGetリクエストの実行
		            response = httpClinet.execute(request);
		            // レスポンスの取得
		            if (response.getStatusLine().getStatusCode()
		                    == HttpStatus.SC_OK) {
		                // コンテンツの取得
		                final HttpEntity entity = response.getEntity();
		                inputStream = entity.getContent();
		                InputStreamReader objReader = new InputStreamReader(inputStream);  
		                BufferedReader objBuf = new BufferedReader(objReader);
		                StringBuilder objJson = new StringBuilder(); 
		                String line;
		                while((line = objBuf.readLine()) != null){
		                       objJson.append(line);
		                 }
		                
		                Gson gson = new Gson();
		                Type collectionType = new TypeToken<Collection<RssItem>>(){}.getType();
		                items = gson.fromJson(objJson.toString(),collectionType);
		               

		                if (inputStream != null) {
		                    //items = parseXmlContent(inputStream);
		                    inputStream.close();
		                }
		            } else {
		                // HttpStatusがエラーで返ってきた
		            }
		            // 切断
		            httpClinet.close();
		        } catch (IOException e) {
		            // HttpGetの失敗
		            e.printStackTrace();
		        }

		        return items;

		    }
			
}
