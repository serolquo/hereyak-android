package com.locution.hereyak;

import android.util.Log;

public class Constants {
	public static final String PREFS_FILE_NAME = "hereyakprefs";
	public static final char[] SERKEET = "".toCharArray();
	public static final String UTF8 = "utf-8";
	public static final int RANGE_LIMIT = 1000; //in meters
	public static final int RANGE_FOR_NEW_LOCATION = 200; //in meters, distance users need to travel before they can manually set a new location
	public static boolean DEBUG = false;
	
	public static void logMessage(int type, String tag, String message) {
		//type: 1-debug, 2-error, defaults to error
		
		if (DEBUG == false) {
			return;
		}
		switch (type) {
		case 1: 
			Log.d(tag, message);
			break;
		default:
			Log.e(tag, message);
			break;
		}
	}
	
}
