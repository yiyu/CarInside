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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.baidu.android.speech.tts.TextToSpeech;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.search.MKRoute;
import com.baidu.mapapi.search.MKStep;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.example.carinside.MainActivity;
import com.example.carinside.R;
import com.example.carinside.share.ShareManager;

public class EmulateNav  implements Runnable {

	public TextToSpeech mTTS = null;
	private MapManager mMgr = null;
	private int mRouteStep = 0;
	private boolean mStart = false;
	private MKRoute mRoute = null;
	public Object obj = new Object();
	public static LocationData testLocData = null;
	ItemizedOverlay mOverlay = null;
	ItemizedOverlay mCarOverlay = null;
	
	public Handler poiHandler = new Handler() {
		public void handleMessage(Message msg){
			Bundle pois = msg.getData();
			double []lats = pois.getDoubleArray("lats");
			double []lngs = pois.getDoubleArray("lngs");
			int len = pois.getInt("len");
			int[] types = pois.getIntArray("types");
			String[] conts = pois.getStringArray("cont");
			mOverlay.removeAll();
			for(int i = 0; i < len; i ++) {
				GeoPoint p = new GeoPoint ((int)(lats[i]*1E6),(int)(lngs[i]*1E6));
				switch(types[i]) {
				case 0:
					OverlayItem item = new OverlayItem(p,"music","");
					item.setMarker(mMgr.mMain.getResources().getDrawable(R.drawable.musicshare));
					mOverlay.addItem(item);
					if (mTTS == null) {
		                mTTS = new TextToSpeech(mMgr.mMain);
		                //mTTS.setOnUtteranceProgressListener();
		            }
					//mTTS.speak("附近有音乐分享", TextToSpeech.QUEUE_FLUSH, null);
					break;
				case 1:
					OverlayItem item1 = new OverlayItem(p,"accident","");
					item1.setMarker(mMgr.mMain.getResources().getDrawable(R.drawable.accident));
					mOverlay.addItem(item1);
					if (mTTS == null) {
		                mTTS = new TextToSpeech(mMgr.mMain);
		                //mTTS.setOnUtteranceProgressListener();
		            }
					mTTS.speak("附近有交通事故", TextToSpeech.QUEUE_ADD, null);
					break;
				case 2:
					OverlayItem item2 = new OverlayItem(p,"jam","");
					item2.setMarker(mMgr.mMain.getResources().getDrawable(R.drawable.jam));
					mOverlay.addItem(item2);
					if (mTTS == null) {
		                mTTS = new TextToSpeech(mMgr.mMain);
		                //mTTS.setOnUtteranceProgressListener();
		            }
					mTTS.speak("附近堵车", TextToSpeech.QUEUE_ADD, null);
					break;
				default:
					OverlayItem item3 = new OverlayItem(p,"cop","");
					item3.setMarker(mMgr.mMain.getResources().getDrawable(R.drawable.police));
					mOverlay.addItem(item3);
					if (mTTS == null) {
		                mTTS = new TextToSpeech(mMgr.mMain);
		                //mTTS.setOnUtteranceProgressListener();
		            }
					mTTS.speak("附近有交警", TextToSpeech.QUEUE_ADD, null);
					break;
				}
			}
			mMgr.mMapView.getOverlays().remove(mOverlay);
	        mMgr.mMapView.getOverlays().add(mOverlay);
	        mMgr.mMapView.refresh();
			
		}
	};
	
	public EmulateNav(MapManager mgr,LocationData locData)
	{
		mMgr = mgr;
	    mOverlay = new ItemizedOverlay(mgr.mMain.getResources().getDrawable(R.drawable.icon_marka), mgr.mMapView);
	    testLocData = new LocationData();
        testLocData.latitude = locData.latitude;
        testLocData.longitude = locData.longitude;
		new Thread(this).start();
	}
	@Override
	public void run() {
		while(true)
		{
			  synchronized(obj) {
					if(!mStart)
					{
						try{
						obj.wait();
						}catch(InterruptedException e){};
					}
			  }
			// TODO Auto-generated method stub
			if(!MainActivity.mStart)
				MainActivity.mStart = true;
			mCarOverlay = new ItemizedOverlay(mMgr.mMain.getResources().getDrawable(R.drawable.icon_marka), mMgr.mMapView);
			int steps = mRoute.getNumSteps();
			mMgr.mMapController.setZoom(18);
			if(!MainActivity.isShareIng)
			for( ; mRouteStep < steps; mRouteStep ++) {
		
					if(!mStart) 
						break;
					MKStep step = mRoute.getStep(mRouteStep);
					mMgr.mMapController.animateTo(step.getPoint());
					OverlayItem item = new OverlayItem(step.getPoint(),"car","");
					item.setMarker(mMgr.mMain.getResources().getDrawable(R.drawable.car));
					LocationData loc = new LocationData();
					loc.latitude = (double)(step.getPoint().getLatitudeE6())/1E6;
					loc.longitude = (double)(step.getPoint().getLongitudeE6())/1E6;
					testLocData.latitude = (double)(step.getPoint().getLatitudeE6())/1E6;
					testLocData.longitude = (double)(step.getPoint().getLongitudeE6())/1E6;
					mCarOverlay.removeAll();
					mCarOverlay.addItem(item);
					mMgr.mMapView.getOverlays().remove(mCarOverlay);
					mMgr.mMapView.getOverlays().add(mCarOverlay);
					mMgr.mMapView.refresh();
					ShareManager.RequestShareHere(loc,poiHandler);
				
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			mStart = false;
		}
	}
	

	
	public void  startRoute(MKRoute route)
	{
		mRoute =  route;
		synchronized(obj) {
			mRouteStep = 0;
		    mStart = true;
			obj.notify();
		  }
	}
	public void stopRoute()
	{
		synchronized(obj) {
			if(mStart)
				obj.notify();
		    mStart = false;
		    
		  }
	
	}
	
	public LocationData getLocation()
	{
		return testLocData;
	}
}
