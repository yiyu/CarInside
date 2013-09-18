package com.example.carinside.nav;
import com.example.carinside.R;
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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.android.speech.tts.TextToSpeech;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.mapapi.map.RouteOverlay;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPlanNode;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKRoute;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKStep;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;

import com.example.carinside.MainActivity;
import com.example.carinside.DemoApplication;
import com.example.carinside.share.ShareManager;
import com.example.carinside.voice.DeviceService;
public class MapManager {
	public static final String TAG = "MapManager";
	public static final int TYPE_SEARCH_LOCATION = 0x100;
	public static final String  TYPE_VALUE_LOCATIONITEMS = "location_items";
	
	public MainActivity 		mMain = null;
	public MyLocationMapView 	mMapView = null;
	public MapController 		mMapController = null;
	private MKMapViewListener 	mMapListener = null;
	private EmulateNav  		mEmulateNav = null;
	//about get location
	private LocationClient 		mLocClient = null;
	public static LocationData 	locData = null;
	public MyLocationListenner 	myLocListenner = null;
	private MyLocationOverlay 	myLocationOverlay = null;
	//��������ͼ��
	private PopupOverlay   		pop  = null;//��������ͼ�㣬����ڵ�ʱʹ��
	private TextView  			popupText = null;//����view
	private boolean 			isRequest = false;//�Ƿ��ֶ���������λ
	private boolean 			isFirstLoc = true;//�Ƿ��״ζ�λ
	//about search
	public static MKSearch 		mMKSearch = null;
	private RouteOverlay 		routeOverlay = null;
	private GeoPoint 			des = null;
	private MySearchListener 	mSearchListener = new MySearchListener();
	//about add Poi info markers
	
	public MapManager(MainActivity main) {
		mMain = main;
		mMapView =(MyLocationMapView)main.findViewById(R.id.bmapView);
		
	}
	
	
	public void init() {
		mMapController= mMapView.getController();
		mMapView.setBuiltInZoomControls(true);
		GeoPoint point =new GeoPoint((int)(39.915* 1E6),(int)(116.404* 1E6));
		mMapController.setCenter(point);//���õ�ͼ���ĵ�
		mMapController.setZoom(14);//���õ�ͼzoom����
		
		mMapController.setOverlooking(-44);
	    mMapListener = new MKMapViewListener() {
				@Override
				public void onMapMoveFinish() 
				{
				}
				
				@Override
				public void onClickMapPoi(MapPoi mapPoiInfo) {
					String title = "";
					if (mapPoiInfo != null){
						title = mapPoiInfo.strText;
						//Toast.makeText(MainActivity.this,title,Toast.LENGTH_SHORT).show();
						mMapController.animateTo(mapPoiInfo.geoPt);
					}
				}
				@Override
				public void onGetCurrentMap(Bitmap b) {

				}

				@Override
				public void onMapAnimationFinish() {
				}
	          
				@Override
				public void onMapLoadFinish() {
					/*Toast.makeText(MainActivity.this, 
							       "��ͼ�������",
							       Toast.LENGTH_SHORT).show();
					*/
				}
			};
			
			mMapView.regMapViewListener(DemoApplication.getInstance().getBMapManager(), mMapListener);
			DemoApplication.getInstance().initEngineManager(mMain);
	       
			//location init
			mLocClient  = new LocationClient(mMain);
			locData = new LocationData();
			myLocListenner = new MyLocationListenner();
			mLocClient.registerLocationListener(myLocListenner);
			LocationClientOption option = new LocationClientOption();
	        option.setOpenGps(true);//��gps
	        option.setCoorType("bd09ll");     //������������
	        option.setScanSpan(5000);
	        mLocClient.setLocOption(option);
	        mLocClient.start();
	        
	        myLocationOverlay = new MyLocationOverlay(mMapView);
	        myLocationOverlay.setData(locData);
	        mMapView.getOverlays().add(myLocationOverlay);
	        myLocationOverlay.enableCompass();
	        mMapView.refresh();
	        //System.out.println(locData.latitude+ ","+locData.longitude);
	        Log.i("locData", locData.latitude+ ","+locData.longitude);
	        
	        mMKSearch = new MKSearch();
	        mMKSearch.init( DemoApplication.getInstance().getBMapManager(), mSearchListener);
	      
	        mEmulateNav = new EmulateNav(this,locData);
	}
	
	public void Pause(){
        mMapView.onPause();
        DemoApplication.getInstance().getBMapManager().start();
	}
	
	public void Resume(){
        mMapView.onResume();
        DemoApplication.getInstance().getBMapManager().start();
	}
	
	public void Destroy() {
    	mMapView.destroy();
    	DemoApplication.getInstance().getBMapManager().destroy();
    }
	
	public void onSaveInstanceState(Bundle outState) {
    	mMapView.onSaveInstanceState(outState);
    }
	
	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
    	mMapView.onRestoreInstanceState(savedInstanceState);
    }
		
	public void requestDestination(String strCity,String strKey,Handler handle)
	{
		mSearchListener.setHandler(handle);
		mMKSearch.poiSearchInCity(strCity, strKey);
	}
	public LocationData getLocation()
	{
		return mEmulateNav.getLocation();
	}
	
	public void setDriveRoute(GeoPoint des) 
	{

		MKPlanNode start = new MKPlanNode();  
		//start.pt = new GeoPoint((int) (39.915 * 1E6), (int) (116.404 * 1E6));
		start.pt = new GeoPoint((int)(locData.latitude*1E6), (int)(locData.longitude*1E6));
		MKPlanNode end = new MKPlanNode();  
		//end.pt = new GeoPoint(40057031, 116307852);// ���üݳ�·���������ԣ�ʱ�����ȡ��������ٻ�������  
		end.pt = des;
		mMKSearch.setDrivingPolicy(MKSearch.ECAR_TIME_FIRST);  
		mMKSearch.drivingSearch(null, start, null, end);
		mEmulateNav.stopRoute();
		//startRoute();
		//mThread.start();
	 }
	 
	public void RequestShareHere(LocationData loc)
	{
		ShareManager.RequestShareHere(loc,mEmulateNav.poiHandler);
	}
	
	 public class MyLocationListenner implements BDLocationListener {
	    	
	        @Override
	        public void onReceiveLocation(BDLocation location) {
	            if (location == null)
	                return ;
	            
	            locData.latitude = location.getLatitude();
	            locData.longitude = location.getLongitude();
	            //�������ʾ��λ����Ȧ����accuracy��ֵΪ0����
	            locData.accuracy = location.getRadius();
	            locData.direction = location.getDerect();
	            //���¶�λ����
	            myLocationOverlay.setData(locData);
	            //����ͼ������ִ��ˢ�º���Ч
	            mMapView.refresh();
	            //���ֶ�����������״ζ�λʱ���ƶ�����λ��
	            if (isRequest || isFirstLoc){
	            	//�ƶ���ͼ����λ��
	                mMapController.animateTo(new GeoPoint((int)(locData.latitude* 1e6), (int)(locData.longitude *  1e6)));
	                isRequest = false;
	            }
	            //�״ζ�λ���
	            isFirstLoc = false;
	        }
	        
	        public void onReceivePoi(BDLocation poiLocation) {
	            if (poiLocation == null){
	                return ;
	            }
	        }
	    }

	  public class locationOverlay extends MyLocationOverlay{

	  		public locationOverlay(MapView mapView) {
	  			super(mapView);
	  			// TODO Auto-generated constructor stub
	  		}
	  		@Override
	  		protected boolean dispatchTap() {
	  			// TODO Auto-generated method stub
	  			//�������¼�,��������
	  			popupText.setBackgroundResource(R.drawable.popup);
				popupText.setText("�ҵ�λ��");
				pop.showPopup(BMapUtil.getBitmapFromView(popupText),
						new GeoPoint((int)(locData.latitude*1e6), (int)(locData.longitude*1e6)),
						8);
	  			return true;
	  		}
	  		
	  	}
	 
	  public class MySearchListener implements MKSearchListener {  
		  private Handler mHandler = null;
	       
		   public void setHandler(Handler handler)
		   {
			   mHandler = handler;
		   }
			@Override
			public void onGetAddrResult(MKAddrInfo arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onGetDrivingRouteResult(MKDrivingRouteResult res,
					int error) {
				// TODO Auto-generated method stub
				if (error == MKEvent.ERROR_ROUTE_ADDR){
					//�������е�ַ
//					ArrayList<MKPoiInfo> stPois = res.getAddrResult().mStartPoiList;
//					ArrayList<MKPoiInfo> enPois = res.getAddrResult().mEndPoiList;
//					ArrayList<MKCityListInfo> stCities = res.getAddrResult().mStartCityList;
//					ArrayList<MKCityListInfo> enCities = res.getAddrResult().mEndCityList;
					return;
				}
				// ����ſɲο�MKEvent�еĶ���
				if (error != 0 || res == null) {
					Toast.makeText(mMain, "��Ǹ��δ�ҵ����", Toast.LENGTH_SHORT).show();
					return;
				}
				//if(MainActivity.isNav)
			
				//searchType = 0;
			    routeOverlay = new RouteOverlay(mMain, mMapView);
			    // �˴���չʾһ��������Ϊʾ��
			    routeOverlay.setData(res.getPlan(0).getRoute(0));
			    //�������ͼ��
			    mMapView.getOverlays().clear();
			    //���·��ͼ��
			    mMapView.getOverlays().add(routeOverlay);
			    //ִ��ˢ��ʹ��Ч
			    mMapView.refresh();
			    // ʹ��zoomToSpan()���ŵ�ͼ��ʹ·������ȫ��ʾ�ڵ�ͼ��
			    mMapView.getController().zoomToSpan(routeOverlay.getLatSpanE6(), routeOverlay.getLonSpanE6());
			    //�ƶ���ͼ�����
			    mMapView.getController().animateTo(res.getStart().pt);
			    //��·�����ݱ����ȫ�ֱ���
			    //simulate(route);
			    mEmulateNav.startRoute(res.getPlan(0).getRoute(0));
			}
			
			@Override
			public void onGetPoiDetailSearchResult(int arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onGetPoiResult(MKPoiResult res, int type, int error) 
			{
				// TODO Auto-generated method stub
				if( error == MKEvent.ERROR_RESULT_NOT_FOUND){
					//Toast.makeText(MyMapActivity.this, "��Ǹ��δ�ҵ����",Toast.LENGTH_LONG).show();  
					return ;  
		        }  
		        else if (error != 0 || res == null) {
		        	//Toast.makeText(MyMapActivity.this, "����������..", Toast.LENGTH_LONG).show();
		        }

				 ArrayList<MyPoi>  	mLocationPT = new ArrayList<MyPoi>();
				
				for(MKPoiInfo info : res.getAllPoi())
				{ 
					if(info.pt != null)
					{
						GeoPoint pt = info.pt;
						String str = info.name;
						mLocationPT.add(new MyPoi(pt,0,str,""));	
						Log.e(TAG,str);
					}
				}  
				
				if(mHandler != null)
				{
					Bundle bundle = new Bundle();
					bundle.putParcelableArrayList(TYPE_VALUE_LOCATIONITEMS,mLocationPT);
					Message msg = mHandler.obtainMessage();
					msg.what = TYPE_SEARCH_LOCATION;
					msg.setData(bundle);
					msg.sendToTarget();
					mHandler = null;
				}
			}
			@Override
			public void onGetShareUrlResult(MKShareUrlResult arg0, int arg1,
					int arg2) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onGetTransitRouteResult(MKTransitRouteResult arg0,
					int arg1) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onGetBusDetailResult(MKBusLineResult arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onGetWalkingRouteResult(MKWalkingRouteResult arg0,
					int arg1) {
				// TODO Auto-generated method stub
				
			} 
	}
}