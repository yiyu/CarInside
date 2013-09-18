package com.example.carinside.voice;

import java.util.List;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.app.Service;
import android.media.AudioManager;
import android.content.ComponentName;

import android.os.Message;

public class DeviceService  extends Service implements Runnable  {
	private static String TAG = "DeviceReceiver";
	
	private  static boolean mVoice = false;
	private final IBinder mBinder = new LocalBinder();

	private DeviceReceiver mReceiver;
    private AudioManager 	 mAudioManager = null;
    private   ComponentName  mbCN  = null;
    private static Handler mMainHandler = null;
    public static int VOICE_CMD_MEDIABUTTON = 0x1000;
    public static int VOICE_CMD_UP = 1;
    public static int VOICE_CMD_DOWN = 2;
    public static int VOICE_CMD_PREV = 3;
    public static int VOICE_CMD_NEXT = 4;
    public static String VOICE_CMD_TYPE = "DeviceService.voicd_cmd_type";
    public static String VOICE_ACTION_STATUS = "DeviceService.action_status";
    
  
	@Override
	public void run() {

	}
	public void setHandler(Handler handler)
	{ 
		mMainHandler = handler;

	}
	
	@Override
	public void onCreate() {
	  	mReceiver = new DeviceReceiver();
		IntentFilter filter = new IntentFilter("android.intent.action.MEDIA_BUTTON");
		this.registerReceiver(mReceiver, filter);
		mAudioManager =(AudioManager)getSystemService(this.getApplicationContext().AUDIO_SERVICE);	 
	    mbCN = new ComponentName(getPackageName(),DeviceReceiver.class.getName());
	  	mAudioManager.registerMediaButtonEventReceiver(mbCN);
	
		new Thread(this).start();
		super.onCreate();
	}
	
	public static class DeviceReceiver extends BroadcastReceiver {

		public DeviceReceiver(){}
		@Override
		public void onReceive(Context context, Intent intent) {
		
		// ���Action
				String intentAction = intent.getAction();
				// ���KeyEvent����
				KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
				// ��ð����ֽ���
				int keyCode = keyEvent.getKeyCode();
				// ���� / �ɿ� ��ť
				int keyAction = keyEvent.getAction();
				// ����¼���ʱ��
				long downtime = keyEvent.getEventTime();
				//Log.i(TAG, "Action ---->" + intentAction + "  KeyEvent----->"+ keyEvent.toString());
				
				int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
				
				if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction) && KeyEvent.ACTION_DOWN == keyAction  ) 
		        {
					
					// ��ȡ������ keyCode
					int iAction= 0;
					StringBuilder sb = new StringBuilder();
					// ��Щ���ǿ��ܵİ����� �� ��ӡ�����û����µļ�
					if (KeyEvent.KEYCODE_MEDIA_NEXT == keyCode) {
						sb.append("KEYCODE_MEDIA_NEXT");
						iAction= DeviceService.VOICE_CMD_UP;
					}
					// ˵���������ǰ���MEDIA_BUTTON�м䰴ťʱ��ʵ�ʳ������� KEYCODE_HEADSETHOOK ������
					// KEYCODE_MEDIA_PLAY_PAUSE
					if (KeyEvent.KEYCODE_MEDIA_PLAY == keyCode ) {
						sb.append("KEYCODE_MEDIA_PLAY");
							mVoice = true;
					}
					
					if (KeyEvent.KEYCODE_MEDIA_STOP == keyCode ) {
							mVoice = false;
							sb.append("KEYCODE_MEDIA_STOP");
					}
				
					if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == keyCode) {
						sb.append("KEYCODE_MEDIA_PREVIOUS");
						iAction =DeviceService.VOICE_CMD_PREV;
					}
					
					if (KeyEvent.KEYCODE_MEDIA_FAST_FORWARD == keyCode) {
						sb.append("KEYCODE_MEDIA_FAST_FORWARD");
						iAction =DeviceService.VOICE_CMD_NEXT;
					}
					
					Message msg = mMainHandler.obtainMessage();
					msg.what = VOICE_CMD_MEDIABUTTON;
					Bundle bundle = new Bundle();
					bundle.putBoolean(DeviceService.VOICE_ACTION_STATUS, mVoice);
					bundle.putInt(DeviceService.VOICE_CMD_TYPE, iAction);
					msg.setData(bundle);
					//mMainHandler.sendMessage(msg);
					msg.sendToTarget();
					Log.e(TAG,"KEYACTION:" + sb);
					//Log.i(TAG, "Action ---->" + intentAction + "  KeyEvent----->"+ keyEvent.toString());
			
					
				}
		}
	}
	
	public class LocalBinder extends Binder {		
		public DeviceService getService() 
		{
		// ����Activity��������Service����������Activity��Ϳɵ���Service���һЩ���÷����͹�������
			return DeviceService.this;		
		}
	}
		
	@Override	
	public IBinder onBind(Intent intent) {
		return mBinder;
	}	
	@Override
	
	public void onDestroy() {
		this.unregisterReceiver(mReceiver);//ȡ��ע���CommandReceiver
		super.onDestroy();
	}
	
}
