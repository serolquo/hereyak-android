package com.locution.hereyak;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import android.location.Address;
import android.location.Location;

public class MainActivitySaveObject implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4517323841456682402L;
	private Double loc_lat;
	private Double loc_lon;
	private String provider;
	private Boolean isNewLocation;
	private String address;
	private Locale locale;
	
	private ArrayList<YakMessage> messages;
	public MainActivitySaveObject(Location loc, Boolean isNewLocation, Address address, ArrayList<YakMessage> messages) {
		this.loc_lat = Double.valueOf(loc.getLatitude());
		this.loc_lon = Double.valueOf(loc.getLongitude());
		this.provider = loc.getProvider();
		this.isNewLocation = isNewLocation;
		if (address == null) {
			this.address = "";
			this.locale = new Locale("null");
		} else {
			this.address = address.getAddressLine(0); 
			this.locale = address.getLocale();
		}
		this.messages = messages;
	}
	
	public Location getLoc() {
		Location loc = new Location(provider);
		loc.setLongitude(loc_lon);
		loc.setLatitude(loc_lat);
		return loc;
	}
	
	public Boolean getIsNewLocation() {
		return this.isNewLocation;
	}
	
	public Address getAddress(){
		if (locale.getLanguage().equals("null")) {
			return null;
		} else {
			Address address = new Address(locale);
			address.setAddressLine(0, this.address);
			return address;
		}
	}
	
	public ArrayList<YakMessage> getMessages(){
		return this.messages;
	}

}
