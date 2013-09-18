package com.example.carinside;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.baidu.android.speech.tts.TextToSpeech;
import com.baidu.mapapi.map.LocationData;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.example.carinside.nav.*;
import com.example.carinside.share.ShareManager;
import com.example.carinside.voice.VoiceRecognize;

public class ShareActivity extends Activity {
	private final int TYPE_MUSIC = 0;
	private final int TYPE_ACCIDENT = 1;
	private final int TYPE_JAM = 2;
	private final int TYPE_COP = 3;

	Button jam = null;
	Button weChat = null;
	Button music = null;
	Button weibo = null;
	Button cop = null;
	Button accident = null;
	private int ctype;
	private String content;
	
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			if(msg.what == ShareManager.TYPE_SHAREHERE)
			 {
				Bundle bundle = msg.getData();
				int iType = bundle.getInt(ShareManager.TYPE_VALUE_RESULT);
				setRe(iType);
			 }
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);
		//road condition button
		accident = (Button)findViewById(R.id.accident);
		OnClickListener acListener = new OnClickListener() {
			public void onClick(View c) {
				String content = "test_accident";
				//sendShare(TYPE_ACCIDENT, content, MapManager.locData);
				ShareManager.sendShare(TYPE_ACCIDENT, content, DemoApplication.getInstance().getMapManager().getLocation(),mHandler);
				//setRe();
			}
		};
		accident.setOnClickListener(acListener);
		
		jam = (Button)findViewById(R.id.jam);
		OnClickListener rcListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				String content = "test_jam";
				//sendShare(TYPE_JAM, content, MapManager.locData);
				ShareManager.sendShare(TYPE_JAM, content,DemoApplication.getInstance().getMapManager().getLocation(),mHandler);
				//setRe();
			}
		};
		jam.setOnClickListener(rcListener);
		
		cop = (Button)findViewById(R.id.cop);
		OnClickListener copListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				String content = "test_cop";
				//sendShare(TYPE_COP, content, MapManager.locData);
				ShareManager.sendShare(TYPE_COP, content, DemoApplication.getInstance().getMapManager().getLocation(),mHandler);
				//setRe();
			}
		};
		cop.setOnClickListener(copListener);
		
		//wechat button
		weChat = (Button)findViewById(R.id.weChat);
		OnClickListener weChatListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: set ctype, what message to send
			}
		};
		weChat.setOnClickListener(weChatListener);
		
		//weibo button
		weibo = (Button)findViewById(R.id.weibo);
		OnClickListener weiboListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: set ctype, what message to send
			}
		};
		weibo.setOnClickListener(weiboListener);
		
		//music button

		music = (Button)findViewById(R.id.music);
		OnClickListener musicListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				String content = "test_music";
				//sendShare(TYPE_MUSIC, content, MapManager.locData);
				ShareManager.sendShare(TYPE_MUSIC, content, DemoApplication.getInstance().getMapManager().getLocation(),mHandler);
				//setRe();
			}
		};
		music.setOnClickListener(musicListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.share, menu);
		return true;
	}
	
	private void setRe(int re) {
		Intent dataa = new Intent();
		dataa.putExtra("response", re);
		setResult(3, dataa);
		MainActivity.isShareIng = false;
		finish();
	}
	
}
