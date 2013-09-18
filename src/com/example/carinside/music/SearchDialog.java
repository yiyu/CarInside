package com.example.carinside.music;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SearchDialog {
	Context mContext;
	Handler mHandler;
	Handler dialogHandler;
	
	Handler searchHandler;
	ArrayList<Boolean> selected;
	ArrayList<String> list_name;
	ArrayList<String> list_artist;
	ArrayList<String> list_url;
	
	
	String[] strs;
	
	String searchName;
	String searchArtist;
	boolean popup;
	
	ListView selectListView;
	SearchDialog(Context context,Handler handler) {
		mContext=context;
		mHandler=handler;
		searchHandler=new Handler(){
			public void handleMessage(Message msg){
				handleJSON(msg.getData().getString("JSON"));
			}
		};
		dialogHandler=new Handler(){
			public void handleMessage(Message msg){
				callDialog();
			}
		};
		selected=new ArrayList<Boolean>();
		list_name=new ArrayList<String>();
		list_artist=new ArrayList<String>();
		list_url=new ArrayList<String>();
	}
	
	public void searchByArtist(String artist,boolean _popup){
		popup=_popup;
		searchArtist=new String(artist);
		this.clearResults();
		Thread thread=new Thread(new Runnable(){
			@Override
			public void run() {
				URL url;
				HttpURLConnection conn;
				BufferedReader rd;
				String line;
				String result = "";
				try {
					Log.v("JSONrequestname",searchArtist);
					String curURL=String.format(
							"http://cq01-2011q4-rptest3-5.vm.baidu.com:8081/music?query=1:%s"
							, java.net.URLEncoder.encode(searchArtist));
					Log.v("JSONrequest",curURL);
					url = new URL(curURL);
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					while ((line = rd.readLine()) != null) {
						result += line;
					}
					rd.close();
				} catch (Exception e) {
				   e.printStackTrace();
				   result=null;
				}
				Message message=new Message();
				Bundle bundle=new Bundle();
				bundle.putString("JSON", result);
				
				message.setData(bundle);
				searchHandler.handleMessage(message);
			}		
		});
		thread.start();
	}
	public void searchDefault(){
		this.clearResults();
		Thread thread=new Thread(new Runnable(){
			@Override
			public void run() {
				HttpURLConnection conn;
				BufferedReader rd;
				String line;
				String result = "";
				try {
				//	Log.e("JSONrequestname",searchName);
					String curURL=String.format("http://cq01-2011q4-rptest3-5.vm.baidu.com:8081/music?query=3:1");
					//Log.e("JSONrequest",curURL);
					URL url = new URL(curURL);
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					while ((line = rd.readLine()) != null) {
						result += line;
					}
					rd.close();
				} catch (Exception e) {
				   e.printStackTrace();
				   result=null;
				}
				Message message=new Message();
				Bundle bundle=new Bundle();
				bundle.putString("JSON", result);
				
				message.setData(bundle);
				searchHandler.handleMessage(message);
			}		
		});
		thread.start();
	}
	
	public void searchByName(String name,boolean _popup){
		popup=_popup;
		searchName=new String(name);
		this.clearResults();
		Thread thread=new Thread(new Runnable(){
			@Override
			public void run() {
				HttpURLConnection conn;
				BufferedReader rd;
				String line;
				String result = "";
				try {
					Log.v("JSONrequestname",searchName);
					String curURL=String.format(
							"http://cq01-2011q4-rptest3-5.vm.baidu.com:8081/music?query=2:%s"
							, java.net.URLEncoder.encode(searchName));
					Log.v("JSONrequest",curURL);
					URL url = new URL(curURL);
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					while ((line = rd.readLine()) != null) {
						result += line;
					}
					rd.close();
				} catch (Exception e) {
				   e.printStackTrace();
				   result=null;
				}
				Message message=new Message();
				Bundle bundle=new Bundle();
				bundle.putString("JSON", result);
				
				message.setData(bundle);
				searchHandler.handleMessage(message);
			}		
		});
		thread.start();
	}
	public void handleJSON(String jsonString){
		if (jsonString==null) return;
		Log.v("JSONresult",jsonString);
		//单层json
		try {
			JSONObject jsonObj=new JSONObject(jsonString);
			JSONArray data=jsonObj.getJSONArray("data");
			for (int i=0;i<data.length();i++){
				JSONObject obj=data.getJSONObject(i);
				String artist=obj.getString("singer");
				String name=obj.getString("name");
				String url=obj.getString("link");
				addResult(artist,name,url);
			}
		} catch (Exception e) {
			//e.printStackTrace();  //At least one exception per call
		}
		//嵌套json
		try {
			JSONObject obj=new JSONObject(jsonString);
			String artist=obj.getString("singer");
			String name=obj.getString("name");
			String url=obj.getString("link");
			addResult(artist,name,url);
		} catch (Exception e) {
			//e.printStackTrace();  //At least one exception per call
		}
		Message message=new Message();
		Bundle bundle=new Bundle();
		message.setData(bundle);
		if (popup) {
			dialogHandler.sendMessage(message);
		}
		else{
			for (int i=0;i<selected.size();i++){
				selected.set(i, new Boolean(true));
			}
			mHandler.sendMessage(message);
		}
		
	}
	private void clearResults(){
		this.list_artist.clear();
		this.list_name.clear();
		this.list_url.clear();
		this.selected.clear();
	}
	
	private void addResult(String artist,String name,String url){
		this.list_artist.add(artist);
		this.list_name.add(name);
		this.list_url.add(url);
		this.selected.add(Boolean.valueOf(false));
	}
	
	private void callDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(
                mContext);
		builder.setTitle("请点击以选中音乐");
		final ArrayAdapter<String> adapter=new ArrayAdapter<String>(mContext,android.R.layout.select_dialog_singlechoice);
		
		int size=this.selected.size();
		strs=new String[size];
		for (int i=0;i<size;i++){
			strs[i]=String.format("%s", list_name.get(i)+";"+list_artist.get(i));
		}
		adapter.addAll(strs);

		builder.setAdapter(adapter, null);

		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	SparseBooleanArray checked = selectListView.getCheckedItemPositions();
        		for (int i = 0; i < checked.size(); i++) {
        			// Item position in adapter
        			int position = checked.keyAt(i);
        			// Add sport if it is checked i.e.) == TRUE!
        			selected.set(position, new Boolean(true));
        		}
            	Message message=new Message();
    			Bundle bundle=new Bundle();
    			message.setData(bundle);
    			mHandler.sendMessage(message);
            }
        });
		AlertDialog dialog =builder.create();
		selectListView=dialog.getListView();
		selectListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		selectListView.setItemsCanFocus(false);
		dialog.show();
		
		
	}
	
	public ArrayList<Boolean> getSelected(){
		return selected;
	}
	public ArrayList<String> getArtist(){
		return list_artist;
	}
	public ArrayList<String> getName(){
		return list_name;
	}
	public ArrayList<String> getURL(){
		return list_url;
	}
}
