package com.example.carinside;

import com.example.carinside.nav.MapManager;
import com.example.carinside.nav.MyLocationMapView;
import com.example.carinside.nav.MyPoi;

import com.example.carinside.nav.BMapUtil;
import com.example.carinside.music.MusicView;
import com.example.carinside.share.ShareManager;
import com.example.carinside.voice.VoiceRecognize;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import com.baidu.android.speech.tts.TextToSpeech;
import com.baidu.mapapi.BMapManager;
import com.capricorn.RayMenu;
import com.example.carinside.nav.MyPoi;

public class MainActivity extends Activity {
	private static String 		TAG = "MainActivity";
	private static final int 	REQUEST_ADDR_SEARCH = 2;
	private static final int 	REQUEST_SHARE = 3;
	public static final String 	TOAST = "toast";

	public static boolean 		isShareIng = false;
	public static boolean 		mStart = false;
	public static VoiceRecognize mVoiceReg = null;
	PowerManager.WakeLock 		mWL = null;
	public MusicView			musicView = null;
	private MapManager 			mapMan = null;
	private TextToSpeech 		mTTS = null;
	private ImageView 			getVoice = null;

	private static final int[] ITEM_DRAWABLES = { R.drawable.composer_music,
			R.drawable.composer_place, R.drawable.composer_thought,
			R.drawable.composer_with };

	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			if (msg.what == VoiceRecognize.VOICE_TYPE_MUSIC) {

				mTTS.speak("播放音乐", TextToSpeech.QUEUE_FLUSH, null);
				doMusic();
				Log.e(TAG, "Play Music");
			} else if (msg.what == VoiceRecognize.VOICE_TYPE_NAVIGATE) {
				Bundle bundle = msg.getData();
				String strDest = bundle.getString(VoiceRecognize.CMD_NAV_DEST);
				musicView.stop();
				// mapMan.getDestination(strDest);
				Log.e(TAG, "Navigation Dest=" + strDest);

			} else if (msg.what == VoiceRecognize.VOICE_TYPE_SHARE) {
				Bundle bundle = msg.getData();
				String strContent = bundle
						.getString(VoiceRecognize.CMD_SHARE_CONTENT);
				int iType = bundle.getInt(VoiceRecognize.CMD_SHARE_TYPE);
				if (iType == VoiceRecognize.CMD_SHARE_TYPE_JAM) {
					ShareManager.sendShare(2, "taffic jam",
							mapMan.getLocation(), mHandler);
				} else if (iType == VoiceRecognize.CMD_SHARE_TYPE_MUSIC) {
					ShareManager.sendShare(0, "music_http_link",
							mapMan.getLocation(), mHandler);
				}
				Log.e(TAG, "Share Dest=" + strContent);
				musicView.stop();
			} else if (msg.what == VoiceRecognize.VOICE_TYPE_RECEIVE_SHARE) {
				// Bundle bundle = msg.getData();
				// int iType = bundle.getInt(VoiceRecognize.CMD_RECEIVE_TYPE);
				mapMan.RequestShareHere(mapMan.getLocation());
				musicView.stop();
				Log.e(TAG, "Receive type:");
			} else if (msg.what == ShareManager.TYPE_SHAREHERE) {
				Bundle bundle = msg.getData();
				int iType = bundle.getInt(ShareManager.TYPE_VALUE_RESULT);
				if (iType > 1)
					mTTS.speak("分享成功", TextToSpeech.QUEUE_FLUSH, null);
			}
		}

	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_ADDR_SEARCH:
			if (resultCode != 0) {
				MyPoi poi = data
						.getParcelableExtra(AddrSearchActivity.TYPE_RETURN_LOCATION);
				mapMan.setDriveRoute(poi.getGeoPoint());
			}
			break;
		case REQUEST_SHARE:
			if (resultCode != 0) {
				int status = data.getExtras().getInt("response");
				if (status == 1)
					Toast.makeText(getApplicationContext(), "share succeed!",
							Toast.LENGTH_SHORT).show();
			}
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mVoiceReg = new VoiceRecognize(this);
		mTTS = new TextToSpeech(this);
		musicView = (MusicView) findViewById(R.id.musicview);
		musicView.setVisibility(View.INVISIBLE);
		mapMan = new MapManager(this);
		mapMan.init();
		DemoApplication.getInstance().setMapManager(mapMan);

		// 设置按钮
		RayMenu rayMenu = (RayMenu) findViewById(R.id.ray_menu);
		initArcMenu(rayMenu, ITEM_DRAWABLES);
		mVoiceReg.Initialize();
		getVoice = (ImageView) findViewById(R.id.mic);
		getVoice.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View arg0, MotionEvent arg1) {
				mVoiceReg.startVoice1();
				return false;
			}
		});
	}

	private void initArcMenu(RayMenu rayMenu, int[] itemDrawables) {
		final int itemCount = itemDrawables.length;
		for (int i = 0; i < itemCount; i++) {
			ImageView item = new ImageView(this);
			item.setImageResource(itemDrawables[i]);

			final int position = i;
			rayMenu.addItem(item, new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (position == 0) {
						doMusic();
					} else if (position == 1) {
						Intent searchAddrIntent = new Intent(MainActivity.this,
								AddrSearchActivity.class);
						startActivityForResult(searchAddrIntent,
								REQUEST_ADDR_SEARCH);
					} else if (position == 2) {
						isShareIng = true;
						Intent shareIntent = new Intent(MainActivity.this,
								ShareActivity.class);
						startActivityForResult(shareIntent, REQUEST_SHARE);
					} else if (position == 3) {
						mapMan.RequestShareHere(mapMan.getLocation());
					}
				}
			});// Add a menu item
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mWL.release();
		mapMan.Pause();
		musicView.stop();
	}

	@Override
	protected void onResume() {
		mapMan.Resume();
		mVoiceReg.onResume();
		super.onResume();
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWL = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
		mWL.acquire();
	}

	@Override
	protected void onDestroy() {
		mapMan.Destroy();
		super.onDestroy();
		mVoiceReg.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapMan.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mapMan.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public final void setStatus(CharSequence subTitle) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(subTitle);
	}

	protected void onStart() {
		super.onStart();
		mVoiceReg.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// 解绑Service，这样可以节约内存
		mVoiceReg.onStop();
		musicView.stop();
	}

	void doMusic() {
		musicView.setVisible(!musicView.getVisible());
	}

}
