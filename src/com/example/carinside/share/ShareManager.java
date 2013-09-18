package com.example.carinside.share;

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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.baidu.mapapi.map.LocationData;
import com.example.carinside.nav.MyPoi;

public class ShareManager {
	public static final int TYPE_SHAREHERE = 0x01;
	public static final String TYPE_VALUE_RESULT = "share_result";
	public static void RequestShareHere(final LocationData locData,final Handler handle){
		Thread thread  = new Thread(new Runnable(){
			@Override
			public void run() {
				ArrayList<MyPoi> poiList = new ArrayList<MyPoi> ();
				String urllll = "http://ducks-mission.wikaer.com/get_condition?radius=0.9";
				urllll = urllll + "&lat=" + locData.latitude + "&lng=" + locData.longitude;
				HttpURLConnection conn;
				BufferedReader rd;
				String line;
				String result = "";
				try {
					 HttpClient client = new DefaultHttpClient();
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
				double[] lats = new double[10];
				double[] lngs = new double[10];
				int[] types = new int[10];
				String[] contents = new String[10];
				int len = 0;
				try {
					JSONArray list = new JSONArray(result);
					len = list.length();
					for(int i = 0; i < len && i < 10; i ++) {
						JSONObject tmp = list.getJSONObject(i);
						lats[i] = tmp.getDouble("lat");
						lngs[i] = tmp.getDouble("lng");
						types[i] = tmp.getInt("ctype");
						contents[i] = tmp.getString("content");
					}
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Bundle b = new Bundle();
				b.putDoubleArray("lats", lats);
				b.putDoubleArray("lngs", lngs);
				b.putIntArray("types", types);
				b.putStringArray("cont", contents);
				if(len>10)
					b.putInt("len", 10);
				else
					b.putInt("len", len);
				Message msg = new Message();
				msg.what =  TYPE_SHAREHERE;
				msg.setData(b);
				handle.sendMessage(msg);
			}
		});
		thread.start();   
	}
	
	public static void sendShare(final int ctype, final String content, final LocationData loc,final Handler handle) {
		Thread th = new Thread(new Runnable(){
			public void run() {
				String urllll = "http://ducks-mission.wikaer.com/send_condition?";
				urllll = urllll + "&lat=" + loc.latitude + "&lng=" + loc.longitude;
				urllll = urllll + "&ctype=" + ctype + "&cont=" + content;
				String result = "";
				try {
					 HttpClient client = new DefaultHttpClient();
		               HttpGet get = new HttpGet(urllll);
		               HttpResponse response = client.execute(get);
		               HttpEntity entity = response.getEntity();
		               long length = entity.getContentLength();
		               InputStream is = entity.getContent();
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
				} catch (Exception e) 
				{
				   e.printStackTrace();
				   result=null;
				}
				int re = Integer.parseInt(result);
				Bundle b = new Bundle();
				b.putInt(TYPE_VALUE_RESULT, re);
				Message msg = new Message();
				msg.what = TYPE_SHAREHERE;
				msg.setData(b);
				handle.sendMessage(msg);
			}
			
		});
		th.start();
	}
}
