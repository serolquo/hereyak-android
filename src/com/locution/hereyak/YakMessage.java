package com.locution.hereyak;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import android.os.Parcel;
import android.os.Parcelable;

public class YakMessage implements Comparable<YakMessage>, Parcelable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6101457571539186589L;
	private String id;
	private String content;
	private String post_date;
	private String user_id;
	private String username;
	private String likes;
	

	public YakMessage (String id, String content, String post_date, String user_id, String username, String likes ) {
		this.id = id;
		this.content = content;
		this.post_date = post_date;
		this.user_id = user_id;
		this.username = username;
		this.likes = likes;
	}
	
	public YakMessage (Parcel parcel) {
		this.id = parcel.readString();
		this.content = parcel.readString();
		this.post_date = parcel.readString();
		this.user_id = parcel.readString();
		this.username = parcel.readString();
		this.likes = parcel.readString();
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getContent() {
		return this.content;
	}
	
	public String getPostDate() {
		return this.post_date;
	}
	
	public String getUserId() {
		return this.user_id;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getLikes() {
		return this.likes;
	}
	
	public DateTime getPostDateTime(){
		DateTimeFormatter parser2 = ISODateTimeFormat.dateTimeNoMillis();
		return parser2.parseDateTime(post_date);
	}

	@Override
	public int compareTo(YakMessage o) {
		return getPostDateTime().compareTo(o.getPostDateTime());
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(id);
		parcel.writeString(content);
		parcel.writeString(post_date);
		parcel.writeString(user_id);
		parcel.writeString(username);
		parcel.writeString(likes);
		
	}
	
	public static Creator<YakMessage> CREATOR = new Creator<YakMessage>() {
		public YakMessage createFromParcel(Parcel parcel) {
	    	return new YakMessage(parcel);
		}

	    public YakMessage[] newArray(int size) {
	    	return new YakMessage[size];
	    }
	};
}
