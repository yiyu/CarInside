package com.example.carinside.nav;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.example.carinside.R;
import com.example.carinside.voice.VoiceRecognize;

public class SearchLocation {
    //private String 		test = "{status: \"Success\",total: 229,pointList: [{name: \"北京大地花园酒店\",cityName: \"北京市\",location: {lng: 116.30606594159,lat: 39.982760910623},address: \"北京市海淀区海淀大街35号（近海淀桥南侧，近中关村图书大厦）\",district: null}]}";
	//ArrayList<String> 		candicates = null;
	
	public  void getDestination(final String strAddr,Handler handle) {
		Thread thread  = new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String addr = strAddr;
				//geocoding(t);
				//UrlTask task = new UrlTask(this, progressBar);
				String urllll = "http://api.map.baidu.com/telematics/v3/geocoding?cityName=北京&output=json&ak=D9ae5f93cdd5a4151ee216f300394724&keyWord=";
				urllll += addr;
				HttpURLConnection conn;
				BufferedReader rd;
				String line;
				String result = "";
				try {
					 HttpClient client = new DefaultHttpClient();
		               // params[0]代表连接的url
		               HttpGet get = new HttpGet(urllll);
		               HttpResponse response = client.execute(get);
		               HttpEntity entity = response.getEntity();
		               long length = entity.getContentLength();
		               InputStream is = entity.getContent();
		               String s = null;
		               if(is != null) {
		                   ByteArrayOutputStream baos = new ByteArrayOutputStream();
		                   byte[] buf = new byte[128];
		                   int ch = -1;
		                   int count = 0;
		                   while((ch = is.read(buf)) != -1) {
		                      baos.write(buf, 0, ch);
		                      count += ch;
		                      Thread.sleep(100);
		                   }
		                   result = new String(baos.toByteArray());
					}
					//rd.close();
				} catch (Exception e) {
				   e.printStackTrace();
				   result=null;
				}
				GeoPoint destination = null;
				try {
	        		JSONObject data = new JSONObject(result);
					JSONObject res = (JSONObject) data.get("results");
					JSONObject loc = (JSONObject) res.get("location");
					//candicates.clear();
					//for(int i = 0; i < resList.length(); i ++) {
						int lng = (int)(loc.getDouble("lng")*1E6);
						int lat = (int)(loc.getDouble("lat")*1E6);
						destination = new GeoPoint(lat, lng);
					//}
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Intent dataa = new Intent();
				dataa.putExtra("lat", destination.getLatitudeE6());
				dataa.putExtra("lng", destination.getLongitudeE6());
			}
			
		});
		thread.start();
	}
	
	 class UrlTask extends AsyncTask<String, Integer, String> {
	        // 可变长的输入参数，与AsyncTask.exucute()对应
		 	ProgressBar pdialog;
	        public UrlTask(Context context,ProgressBar progressBar){
//		            pdialog = progressBar;   
	            
//		            pdialog.setMax(100);
	            //pdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//		            pdialog.setVisibility(ProgressBar.VISIBLE);
	        }
	        @Override
	        protected String doInBackground(String... params) {

	            try{

	               HttpClient client = new DefaultHttpClient();
	               // params[0]代表连接的url
	               HttpGet get = new HttpGet(params[0]);
	               HttpResponse response = client.execute(get);
	               HttpEntity entity = response.getEntity();
	               long length = entity.getContentLength();
	               InputStream is = entity.getContent();
	               String s = null;
	               if(is != null) {
	                   ByteArrayOutputStream baos = new ByteArrayOutputStream();

	                   byte[] buf = new byte[128];

	                   int ch = -1;

	                   int count = 0;

	                   while((ch = is.read(buf)) != -1) {

	                      baos.write(buf, 0, ch);

	                      count += ch;

	                      if(length > 0) {
	                          // 如果知道响应的长度，调用publishProgress（）更新进度
	                          publishProgress((int) ((count / (float) length) * 100));
	                      }
	                      // 让线程休眠100ms
	                      Thread.sleep(100);
	                   }
	                   s = new String(baos.toByteArray());              }
	               // 返回结果
	               return s;
	            } catch(Exception e) {
	            }
	            return null;
	        }

	        @Override
	        protected void onCancelled() {
	            super.onCancelled();
	        }

	        @Override
	        protected void onPostExecute(String result) {
	            // 返回HTML页面的内容
	            //textView.setText(result);
	        	try {
					/*JSONObject data = new JSONObject(result);
					JSONArray resList = (JSONArray) data.get("results");
					candicates.clear();
					for(int i = 0; i < resList.length(); i ++) {
						JSONObject addr = (JSONObject) resList.get(i);
						candicates.add(addr.getString("name"));
						Log.i("candicates", addr.getString("name"));
					}
					setAdapter();*/
	        		JSONObject data = new JSONObject(result);
					JSONArray resList = (JSONArray) data.get("results");
					//ArrayList<String> candicates.clear();
					for(int i = 0; i < resList.length(); i ++) {
						JSONObject loc = (JSONObject) resList.get(i);
						int lng = (int)(loc.getDouble("lng")*1E6);
						int lat = (int)(loc.getDouble("lat")*1E6);
						GeoPoint destination = new GeoPoint(lat, lng); 
						//Log.i("candicates", addr.getString("name"));
					}
					
				} catch (JSONException e) {
					//Log.d("search positions", );
					e.printStackTrace();
				}
	            pdialog.setVisibility(ProgressBar.INVISIBLE);
	        }

	        @Override
	        protected void onPreExecute() {
	            // 任务启动，可以在这里显示一个对话框，这里简单处理
	          //  message.setText(R.string.task_started);
	        }

	        @Override
	        protected void onProgressUpdate(Integer... values) {
	            // 更新进度
	              System.out.println(""+values[0]);
	              //message.setText(""+values[0]);
	              pdialog.setProgress(values[0]);
	        }

	     }

}
