package com.example.carinside;
import com.example.carinside.nav.MapManager;
import com.example.carinside.nav.MyPoi;
import com.example.carinside.share.ShareManager;
import com.example.carinside.voice.*;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.platform.comapi.basestruct.GeoPoint;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class AddrSearchActivity extends Activity {
	private static final String 	TAG = "AddrSearchActivity";
	public static final String		TYPE_RETURN_LOCATION = "location_poi";
	private AutoCompleteTextView 	mTextView = null;
	private ProgressBar 			mProgressBar = null;
	private Button 					mGo = null;
	private VoiceRecognize 			mVR = null;
	private ImageView 				getVoice = null;
    private ArrayList<MyPoi>  		mLocationItems = null;
    private ArrayAdapter<String> 	mLocationArrayAdapter;
    private String 					mCity = "深圳";
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			if(msg.what == MapManager.TYPE_SEARCH_LOCATION)
			 {
				mLocationArrayAdapter.clear();
				Bundle bundle = msg.getData();
				mLocationItems = bundle.getParcelableArrayList(MapManager.TYPE_VALUE_LOCATIONITEMS);
				for (int i=0;i<mLocationItems.size();i++){
					MyPoi poi = mLocationItems.get(i);
					mLocationArrayAdapter.add(poi.getName());
				}
				Log.e(TAG,"Search Result");
			 }
		}
		
	};	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addr_search);
		mTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
		mTextView.setText("茂业百货");
		mGo = (Button) findViewById(R.id.getaddr);
		mLocationArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		
        ListView locationListView = (ListView) findViewById(R.id.locationList);
        locationListView.setAdapter(mLocationArrayAdapter);
        locationListView.setOnItemClickListener(mLocationClickListener);
		mGo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				DemoApplication.getInstance().getMapManager().requestDestination(mCity,mTextView.getText().toString(),mHandler);		
			}	
		});
	    
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.addr_search, menu);
		return true;
	}
	
	private OnItemClickListener mLocationClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            
            String info = ((TextView) v).getText().toString();
           
            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            MyPoi p1 = mLocationItems.get(arg2) ;
            intent.putExtra(TYPE_RETURN_LOCATION, p1);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

}
