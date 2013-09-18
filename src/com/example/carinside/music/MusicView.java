package com.example.carinside.music;



import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.example.carinside.R;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class MusicView extends LinearLayout implements OnClickListener,
		OnTouchListener, OnCompletionListener, OnBufferingUpdateListener,
		OnItemSelectedListener{
	private Context viewContext;
	private boolean isVisible=false;
	private ImageButton playButton,pauseButton,backButton,forwardButton;
	private MediaPlayer mediaPlayer;
	private SeekBar seekBarProgress;
	private Spinner playlistSpinner;
	private int mediaFileLengthInMilliseconds; // this value contains the song duration in milliseconds. Look at getDuration() method in MediaPlayer class
	private MusicAdapter adapter;
	private SearchDialog searchDialog;
	private final Handler handler = new Handler();
	
	private Handler searchHandler;
	public MusicView(Context context) {
		super(context);
        initView(context);
	}

	public MusicView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public MusicView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        initView(context);
	}
	
	private void initView(Context context){
		viewContext=context;
		LayoutInflater  mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.view_music, this, true);
        playButton=(ImageButton) findViewById(R.id.playButton);
        pauseButton=(ImageButton) findViewById(R.id.pauseButton);
        backButton=(ImageButton) findViewById(R.id.backButton);
        forwardButton=(ImageButton) findViewById(R.id.forwardButton);
        playButton.setOnClickListener(this);
        pauseButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        forwardButton.setOnClickListener(this);
        playlistSpinner=(Spinner) findViewById(R.id.playlistSpinner);
        
        playButton.setVisibility(View.GONE);
        seekBarProgress = (SeekBar)findViewById(R.id.seekBarTestPlay);	
		seekBarProgress.setMax(99); // It means 100% .0-99
		seekBarProgress.setOnTouchListener(this);
		
		
		searchHandler=new Handler(){
			public void handleMessage(Message msg){
				ArrayList<String> names=searchDialog.getName();
				ArrayList<String> urls=searchDialog.getURL();
				ArrayList<Boolean> selected=searchDialog.getSelected();
				for (int i=0;i<names.size();i++){
					//Log.e("name",names.get(i));
					if (selected.get(i).booleanValue())
						add(names.get(i),urls.get(i));
				}
			}
		};
		searchDialog=new SearchDialog(context,searchHandler);
		this.DefaultList();
		
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnBufferingUpdateListener(this);
		mediaPlayer.setOnCompletionListener(this);
		adapter=initial(context);
		playlistSpinner.setAdapter(adapter);
		//playlistSpinner.setSelection(0);
		
		playlistSpinner.setOnItemSelectedListener(this);
	}
	
	public void setVisible(boolean arg){
		this.isVisible=arg;
		if (!arg)
			this.setVisibility(View.INVISIBLE);
		else{
			this.setVisibility(View.VISIBLE);
			if (getSelectedId()!=AdapterView.INVALID_ROW_ID && !mediaPlayer.isPlaying()){
				this.switchto(0);
				mediaPlayer.reset();
				try {
					String strList = (String)playlistSpinner.getSelectedItem();
					mediaPlayer.setDataSource(strList);
					mediaPlayer.prepare();
				} catch (Exception e) {
					e.printStackTrace();
				} 
				mediaFileLengthInMilliseconds = mediaPlayer.getDuration();
				mediaPlayer.start();
				primarySeekBarProgressUpdater();
			}
		}
	}
	public boolean getVisible(){
		return this.isVisible;
	}
	private void primarySeekBarProgressUpdater() {
    	seekBarProgress.setProgress((int)(((float)mediaPlayer.getCurrentPosition()/mediaFileLengthInMilliseconds)*100)); // This math construction give a percentage of "was playing"/"song length"
		if (mediaPlayer.isPlaying()) {
			Runnable notification = new Runnable() {
		        public void run() {
		        	primarySeekBarProgressUpdater();
				}
		    };
		    handler.postDelayed(notification,1000);
    	}
    }

	@Override
	public void onCompletion(MediaPlayer mp) {
		 next();
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		/** Method which updates the SeekBar secondary progress by current song loading from URL position*/
		seekBarProgress.setSecondaryProgress(percent);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(v.getId() == R.id.seekBarTestPlay){
			/** Seekbar onTouch event handler. Method which seeks MediaPlayer to seekBar primary progress position*/
			if(mediaPlayer.isPlaying()){
		    	SeekBar sb = (SeekBar)v;
				int playPositionInMillisecconds = (mediaFileLengthInMilliseconds / 100) * sb.getProgress();
				mediaPlayer.seekTo(playPositionInMillisecconds);
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.playButton:
		case R.id.pauseButton:
			
			if(!mediaPlayer.isPlaying()){
				mediaPlayer.start();
				playButton.setVisibility(View.GONE);
				pauseButton.setVisibility(View.VISIBLE);
			}else {
				mediaPlayer.pause();
				playButton.setVisibility(View.VISIBLE);
				pauseButton.setVisibility(View.GONE);
			}
			primarySeekBarProgressUpdater();
			break;
		case R.id.backButton:
			prev();
			break;
		case R.id.forwardButton:
			next();
			break;
			
		}
		
			
	}
	public void stop(){
		mediaPlayer.stop();
	}
	
	
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		if (this.isVisible){
			try {
				mediaPlayer.reset();
				mediaPlayer.setDataSource((String) playlistSpinner.getSelectedItem()); // setup song from http://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3 URL to mediaplayer data source
				mediaPlayer.prepare();
				mediaFileLengthInMilliseconds = mediaPlayer.getDuration();
				mediaPlayer.start();
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		
	}
	
	
	private class MusicAdapter extends BaseAdapter{
		private Context mContext;
        private ArrayList<String> name=new ArrayList <String>();
        private ArrayList<String> url=new ArrayList <String>();
        public MusicAdapter(Context c){
        	mContext = c; 
        }
		@Override
		public int getCount() {
			
			return name.size();
		}

		@Override
		public String getItem(int index) {
			return url.get(index);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView view=new TextView(mContext);
			view.setText(name.get(position));
			view.setTextSize(16.0f);
			view.setTextColor(0xff000000);
			view.setGravity(Gravity.CENTER);
			return view;
		}  
		
		public void add(String _name,String _url){
			this.name.add(_name);
			this.url.add(_url);
		}
		public void remove(int pos){
			this.name.remove(pos);
			this.url.remove(pos);
		}
		public String getName(int pos){
			return name.get(pos);
		}
        
	}
	
	MusicAdapter initial(Context context){
		MusicAdapter adapter = new MusicAdapter(context);
		//jameshe modify
		adapter.add("Welcome to Baidu!", "http://learn.tsinghua.edu.cn:8080/2010011355/test2.mp3");
		return adapter;		
	}
	
	
	
	public void add(String _name,String _url){
		adapter.add(_name, _url);
	}
	public void remove(int pos){
		adapter.remove(pos);
	}
	public int length(){
		//Log.e("Length",Integer.toString(adapter.getCount()));
		return adapter.getCount();
	}
	public void switchto(int pos){
		Log.e("Switchto",Integer.toString(pos));
		playlistSpinner.setSelection(pos);
	}
	public long getSelectedId(){
		//Log.e("getSelectedId",Integer.toString((int) playlistSpinner.getSelectedItemId()));
		return playlistSpinner.getSelectedItemId();
	}
	
	
	Map<String, String> getCurrentPlaying(){
		long selectedId=getSelectedId();
		if (selectedId==AdapterView.INVALID_ROW_ID) return null;
		
		HashMap<String, String> ret=new HashMap<String, String>();
		ret.put("name", adapter.getName((int)selectedId));
		ret.put("url", adapter.getItem((int) selectedId));
		return ret;
	}
	String getCurrentPlayingUrl(){
		String ret=null;
		long selectedId=getSelectedId();
		if (selectedId==AdapterView.INVALID_ROW_ID) return ret;
		
		return adapter.getItem((int) selectedId);
	}
	
	void searchByArtist(String artist,boolean popup){
		searchDialog.searchByArtist(artist,popup);
	}
	void searchByName(String name,boolean popup){
		searchDialog.searchByName(name,popup);
	}
	
	void DefaultList(){
		searchDialog.searchDefault();
	}
	
	public void next(){
		if (this.length()==0) return;
		int nextid=((int)this.getSelectedId()+10*this.length()+1)%this.length();
		this.switchto(nextid);
	}
	public void prev(){
		//For testing use
			//this.searchByArtist("¿Ó”Ó¥∫",true);   
			//this.searchByArtist("‘¯ÈÛø…",false);  
			//this.searchByName("My Heart Will Go on",false);
			//this.searchByName("Lovers",true);
		if (this.length()==0) return;
		int nextid=((int)this.getSelectedId()+10*this.length()-1)%this.length();
		this.switchto(nextid);
		
	}


}

