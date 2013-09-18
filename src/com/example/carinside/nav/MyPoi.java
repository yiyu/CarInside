package com.example.carinside.nav;

import android.os.Parcel;
import android.os.Parcelable;

import com.baidu.platform.comapi.basestruct.GeoPoint;

public class MyPoi implements Parcelable{
	private GeoPoint poi;
	private int ctype;
	private String name;
	private String content;
	public MyPoi(){};
	public MyPoi(MyPoi p)
	{
		poi = p.poi;
		ctype = p.ctype;
		name = p.name;
		content = p.content;
	}
	public MyPoi(GeoPoint p, int t) {
		poi = p;
		ctype = t;
	}
	public MyPoi(GeoPoint p, int t, String c) {
		poi = p;
		ctype = t;
		content = c;
	}
	
	public MyPoi(GeoPoint p, int t, String n,String c) {
		poi = p;
		ctype = t;
		content = c;
		name = n;
	}
	public GeoPoint getGeoPoint() {
		return poi;
	}
	public int getCtype() {
		return ctype;
	}
	public String getContent() {
		return content;
	}
	
	public String getName() {
		return name;
	}
	
	public void setGeoPoint(GeoPoint p) {
		poi = p;
	}
	public void setCtype(int t) {
		ctype = t;
	}
	public void setContent(String c) {
		content = c;
	}
	
	public void setName(String c) {
		name = c;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		// TODO Auto-generated method stub
		dest.writeInt(poi.getLatitudeE6());
		dest.writeInt(poi.getLongitudeE6());
		dest.writeInt(ctype);
        dest.writeString(name);  
        dest.writeString(content);  
	}
	
	public static final Parcelable.Creator<MyPoi> CREATOR = new Creator<MyPoi>()  
		    {  
		        public MyPoi createFromParcel(Parcel source)  
		        {  
		        	GeoPoint pt = new GeoPoint(source.readInt(),source.readInt());
		        	MyPoi poi = new MyPoi(pt,
		        							 source.readInt(),
		        							 source.readString(),
		        							 source.readString());  
		        
		            return poi;  
		        }  
		        public MyPoi[] newArray(int size)  
		        {  
		            return new MyPoi[size];  
		        }  
		    };  
}
