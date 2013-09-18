package com.example.carinside.voice;

import java.util.ArrayList;

import android.content.ComponentName;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.baidu.android.speech.RecognitionListener;
import com.baidu.android.speech.SpeechConfig;
import com.baidu.android.speech.SpeechRecognizer;
import com.capricorn.VoiceMenu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import com.example.carinside.MainActivity;
import com.example.carinside.voice.DeviceService.*;
public class VoiceRecognize {
	private String TAG = "VoiceRecognize";
	private String RESULTS_KEY = SpeechRecognizer.EXTRA_RESULTS_RECOGNITION;
    private String PARTIAL_RESULTS_KEY = SpeechRecognizer.EXTRA_RESULTS_RECOGNITION;
    private String PARTIAL_SCORES_KEY = SpeechRecognizer.EXTRA_CONFIDENCE_SCORES;
	public static int VOICE_TYPE_NAVIGATE = 0x01;
	public static int VOICE_TYPE_MUSIC = 0x02;
	public static int VOICE_TYPE_SHARE = 0x03;
	public static int VOICE_TYPE_RECEIVE_SHARE = 0x04;
	
	public static String CMD_NAV_DEST = "dest";
	public static String CMD_SHARE_CONTENT = "content";
	public static String CMD_SHARE_TYPE = "type";
	
	public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final int CMD_SHARE_TYPE_VALUE = 1;
    public static final int CMD_SHARE_TYPE_JAM = 2;
    public static final int CMD_SHARE_TYPE_MUSIC = 3;
    
    public static final String TOAST = "toast";
    
	private SpeechRecognizer mSpeechRecognizer = null;
    private AudioManager mAudioManager = null;
    private   ComponentName  mbCN  = null;
    MyRecognitionListener mMyRecognitionListener =null;
	private MainActivity mActivity = null;
	
	private BluetoothChatService mChatService = null;
	private DeviceService mService = null;
	boolean mBound = false;
	
	private int mSessionState = 0;
	private ArrayList<String> mSessionArray = null;
	
	
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(final Message msg)
		{
			 if (msg.what == DeviceService.VOICE_CMD_MEDIABUTTON) 
			 {
				Bundle bundle = msg.getData();
				boolean bVoice = bundle.getBoolean(DeviceService.VOICE_ACTION_STATUS);
				int iAction = bundle.getInt(DeviceService.VOICE_CMD_TYPE);
				switch(iAction)
				{
				case BluetoothChatService.VOICE_CMD_PLAY:
					startVoice1();
					break;
				case BluetoothChatService.VOICE_CMD_PREV:
					if(mSessionState == VOICE_TYPE_MUSIC )
						mActivity.musicView.next();
					break;
				case BluetoothChatService.VOICE_CMD_NEXT:
					if(mSessionState == VOICE_TYPE_MUSIC )
						mActivity.musicView.prev();
					break;
				}
				Log.e(TAG, "--handleMessage cmd --" +  String.format("%d",iAction) );//+ " status ="+ String.format("%d",bVoice) );
			 }else
			 {
				 switch (msg.what) {
		            case MESSAGE_STATE_CHANGE:
		                Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
		                switch (msg.arg1) {
		                case BluetoothChatService.STATE_CONNECTED:
		                	 mActivity.setStatus("蓝牙连接成功");
		                    break;
		                case BluetoothChatService.STATE_CONNECTING:
		                    mActivity.setStatus("蓝牙连接中");
		                    break;
		                case BluetoothChatService.STATE_LISTEN:
		                case BluetoothChatService.STATE_NONE:
		                	 mActivity.setStatus("蓝牙未连接");
		                    break;
		                }
		                break;
		       
		            case MESSAGE_TOAST:
		                Toast.makeText(mActivity.getApplicationContext(), msg.getData().getString(TOAST),
		                               Toast.LENGTH_SHORT).show();
		                break;
		            }
			 }
		}
	};
	
	/** 定交ServiceConnection，用于绑定Service的 */
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mService.setHandler(mHandler);
			mBound = true;
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};
	
	public VoiceRecognize(MainActivity act)
	{
		mActivity = act;
	    String appKey = "CTzs3b2ZOCaVx9wKSaS116l7";
        String secretKey = "SnMBmwPGoWaS1xq39rcZ3azwnfLx09ih";
        SpeechConfig.setup(mActivity.getApplicationContext(), appKey, secretKey);
        //DEFAULT_CONFIG.enableBeginSoundEffect(R.raw.audio_start);
        mMyRecognitionListener = new MyRecognitionListener();
		mSpeechRecognizer = SpeechRecognizer.getInstance(mActivity);
	    mSpeechRecognizer.setRecognitionListener(mMyRecognitionListener);
	}
	
	public void Initialize()
	{
		 mChatService = new BluetoothChatService(mActivity, mHandler);
		 mAudioManager =(AudioManager)mActivity.getSystemService(mActivity.getApplicationContext().AUDIO_SERVICE);
		 mAudioManager.setMode(AudioManager.MODE_IN_CALL);
		 mAudioManager.setSpeakerphoneOn(true);
	}
	public void onResume()
	{
	  if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_DISCONNECTED) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
	}
	
	public void onDestroy()
	{
		mChatService.stop();
		mSpeechRecognizer.destroy();
	}
	
	public void onStart()
	{
		// 绑定Service，绑定后就会调用mConnetion里的onServiceConnected方法
		Intent intent = new Intent(mActivity,DeviceService.class);
		mActivity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	public void onStop()
	{
		if (mBound) {
			mActivity.unbindService(mConnection);
			mBound = false;
		}
	}
	
	public void startVoice()
	{
			 
		boolean bAvailable = mAudioManager.isBluetoothScoAvailableOffCall();
		if(bAvailable)
		{
			if(mAudioManager.isBluetoothScoOn())
	        	mAudioManager.stopBluetoothSco();
	        mAudioManager.startBluetoothSco();
			  	  
	        mActivity.getApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
	                int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
	
	                if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) { 
	                		mAudioManager.setBluetoothScoOn(true);  //打开SCO
	                		mActivity.getApplicationContext().unregisterReceiver(this);  //别遗漏
	                		
	                		Bundle bundle = new Bundle();
		                	mSpeechRecognizer.startListening(SpeechRecognizer.SpeechMode.VOICE_TO_TEXT, bundle);
	                		Log.e(TAG,"Connect Success!!!");
	                }else if(AudioManager.SCO_AUDIO_STATE_CONNECTING == state)
	                {
	                	Log.e(TAG,"Establish SCOConnecting!!!"+String.valueOf(state));
	                }
	                else{//等待一秒后再尝试启动SCO
	                		Log.e(TAG,"Connect Failed!!!"+String.valueOf(state));
	                }
	            }
	        }, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
	    }
 	}
	
	public void startVoice1()
	{
		Bundle bundle = new Bundle();
        mSpeechRecognizer.startListening(SpeechRecognizer.SpeechMode.VOICE_TO_TEXT, bundle);
	}
	 
	public void stopVoice()
 	{
 		if(mAudioManager.isBluetoothScoOn()){
	            mAudioManager.setBluetoothScoOn(false);
	      
	    }
 	      mAudioManager.stopBluetoothSco();
 		Log.e(TAG," stop voice!!!");
 		mSpeechRecognizer.stopListening();
 		 Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);//系统自带提示音 
		 Ringtone rt = RingtoneManager.getRingtone(mActivity.getApplicationContext(), uri);
		 rt.play();
		 
      // showCommonResult("");*/
	}
	
	
	private class MyRecognitionListener implements RecognitionListener,
    android.speech.RecognitionListener {
	    private static final int WAV_HEADER_LENGTH = 44;
	    private static final short WAV_FORMAT_PCM = 0x0001;
	    private float fPrev = -100.0f;
	    @Override
	    public void onReadyForSpeech(Bundle params) {
	        //mResultTextView.setText("ready for speech");
			 Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);//系统自带提示音 
			 Ringtone rt = RingtoneManager.getRingtone(mActivity.getApplicationContext(), uri);
			 rt.play();
			 
	    }
	
	    @Override
	    public void onBeginningOfSpeech() {
	    	Log.e(TAG,"onBeginningOfSpeech=====");
	    	//VoiceMenu voiceMenu = (VoiceMenu) findViewById(R.id.Voice_menu);
			//voiceMenu.startVoice();
	    }
	
	    @Override
	    public void onRmsChanged(float rmsdB) {
	    	if(rmsdB > fPrev + 200 || rmsdB < fPrev + 200 )
	    	{
	    		fPrev = rmsdB;
	    	//	VoiceMenu voiceMenu = (VoiceMenu) findViewById(R.id.Voice_menu);
	    	//	voiceMenu.VoiceVolume(rmsdB/1000);
	    	}
	    }
	
	    @Override
	    public void onBufferReceived(byte[] buffer) {
	     
	    }
	
	    @Override
	    public void onEndOfSpeech() {
	    	Log.e(TAG,"onEndOfSpeech!");
	    	//VoiceMenu voiceMenu = (VoiceMenu) findViewById(R.id.Voice_menu);
			//voiceMenu.stopVoice();
	    	stopVoice();
	    }
	
	    @Override
	    public void onError(int error) {
	    	String subTitle = "语音识别错误 ERROR:";

			switch (error) {
			case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
				subTitle += "ERROR_NETWORK_TIMEOUT";
				break;
			case SpeechRecognizer.ERROR_NETWORK:
				subTitle += "ERROR_NETWORK";
				break;
			case SpeechRecognizer.ERROR_AUDIO:
				subTitle += "ERROR_AUDIO";
				break;
			case SpeechRecognizer.ERROR_SERVER:
				subTitle += "ERROR_AUDIO";
				break;
			case SpeechRecognizer.ERROR_CLIENT:
				subTitle += "客户端错误";
				break;
			case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
				subTitle += "未检测出声音 ";
				break;
			case SpeechRecognizer.ERROR_NO_MATCH:
				subTitle += "ERROR_NO_MATCH";
				break;
			}
			subTitle += String.format("%d", error);
			Log.e(TAG, "onError=" + subTitle);
			mActivity.setStatus(subTitle);
			stopVoice();
	    }
	
	    @Override
	    public void onResults(Bundle results) {
	        if (results == null) {
	        	
	            return;
	        }
	        
	        ArrayList<String> resultList = results.getStringArrayList(RESULTS_KEY);
	        if (resultList == null || resultList.size() < 1) {
	            return;
	        }
	        
	        String result = resultList.get(0);
	        Log.e(TAG,"onResults=" + result);
	        handleResult(result);
	    
	    }
	
	    @Override
	    public void onPartialResults(Bundle partialResults) {
	        int i = 0;
	        ArrayList<String> resultList = partialResults.getStringArrayList(PARTIAL_RESULTS_KEY);
	        float[] scores = partialResults.getFloatArray(PARTIAL_SCORES_KEY);
	        if (resultList == null) {
	            return;
	        }
	        String result = "";//mResultTextView.getText().toString();
	        for (String data : resultList) {
	            result += data + "[" + scores[i] + "]\r\n";
	            i++;
	        }
	        //showCommonResult(result);
	    }
	
	    @Override
	    public void onEvent(int eventType, Bundle params) {
	    }
	    
	    private void  handleResult(String strVoice)
	    {
	     	Message msg = mActivity.mHandler.obtainMessage();
	     	Bundle bundle = new Bundle();
	    	if(strVoice.equals("打开音乐")||strVoice.equals("播放音乐"))
	    	{
	    		msg.what = VOICE_TYPE_MUSIC;
	    		mSessionState = VOICE_TYPE_MUSIC;
	    	}else if(strVoice.contains("导航到"))
	    	{
	    		msg.what = VOICE_TYPE_NAVIGATE;
	    		mSessionState = VOICE_TYPE_NAVIGATE;
				bundle.putString(CMD_NAV_DEST, strVoice.replace("导航到", ""));
	    	}else if(strVoice.contains("打开广播"))
	    	{
	    		msg.what = VOICE_TYPE_RECEIVE_SHARE;
	    		mSessionState = VOICE_TYPE_RECEIVE_SHARE;
	    	}else if(strVoice.contains("堵车"))
	    	{
	    		msg.what = VOICE_TYPE_SHARE;
	    		mSessionState = VOICE_TYPE_SHARE;
	    		bundle.putInt(CMD_SHARE_TYPE, CMD_SHARE_TYPE_JAM);
	    	}else if(strVoice.contains("分享音乐"))
	    	{
	    		msg.what = VOICE_TYPE_SHARE;
	    		mSessionState = VOICE_TYPE_SHARE;
	    		bundle.putInt(CMD_SHARE_TYPE, CMD_SHARE_TYPE_MUSIC);
	    	}
	    	
	    	if(mSessionState > 0)
			{
	    		
				msg.setData(bundle);
				msg.sendToTarget();
			}
	    }
	    private boolean showWebContent(String result) {
	        if (!TextUtils.isEmpty(result)) {
	        	Log.e(TAG,"语音识别结果：" + result);
				
	          /*  try {
	                JSONObject jsonContent = new JSONObject(result);
	                String command_str = jsonContent.getString("command_str");
	                JSONObject jsonCommands = new JSONObject(command_str);
	                JSONArray jsonCommandArray = jsonCommands.getJSONArray("commandlist");
	                JSONObject oneCommand = jsonCommandArray.getJSONObject(0);
	                String ttsBody = oneCommand.optString("ttsbody");
	                if (oneCommand.has("commandcontent")) {
	                    JSONObject commandContent = oneCommand.optJSONObject("commandcontent");
	                    if (commandContent != null) {
	                        if (commandContent.has("web")) {
	                            String url = commandContent.optString("baseurl");
	                            String content = commandContent.optString("web");
	                            mResultTextView.setVisibility(View.GONE);
	                            mWebView.setVisibility(View.VISIBLE);
	                            mTTSBodyTextView.setVisibility(View.VISIBLE);
	
	                            mWebView.loadDataWithBaseURL(url, content, "text/html", "utf-8", null);
	                            mTTSBodyTextView.setText(ttsBody);
	                            return true;
	                        }
	                    }
	                }
	            } catch (JSONException e) {
	              //  e.printStackTrace();
	            }*/
	        }
	        return false;
	    }
	}
}
