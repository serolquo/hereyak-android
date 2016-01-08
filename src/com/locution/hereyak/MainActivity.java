package com.locution.hereyak;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.SupportMapFragment;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private ListView messagesView;
	//private ArrayAdapter<String> listAdapter;
	private YakAdapter listAdapter;
	private String username;
	private String device_key;
	private String access_token;
	private LocationManager locationManager;
	private Location location;
	private Address address;
	private boolean retrieving = false;
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final String SAVE_FILE_NAME = "MainActivitySaveState";
	private static final int ACTIVITY_SHOW_ANDROID_SETTINGS = 2;
	private static final int ACTIVITY_SHOW_MAP = 3;
	
	private GetMessagesTask mGetMessagesTask = null;
	private SendMessageTask mSendMessageTask = null;
	private EditText mMessageBoxView; 
	private boolean isNewLocation = true;
	private boolean runningActivity = true;
	private String messageBoxContent;
	private final LocationListener gpsLocationListener = new LocationListener() {
	    
	    @Override
	    public void onLocationChanged(Location loc) {
	    	Constants.logMessage(1,"onLocationChanged","Location: "+loc.toString());
	        locationHasChanged(loc);
	    }


		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
			
		}
	};
	
private final LocationListener networkLocationListener = new LocationListener() {
	    
	    @Override
	    public void onLocationChanged(Location loc) {
	    	Constants.logMessage(1,"onLocationChanged","Location: "+loc.toString());
	        locationHasChanged(loc);
	    }


		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
			
		}
	};
	
	final Handler retrieveMsgHandler = new Handler();
	Runnable retrieveMsgPoll = new Runnable() {
	    public void run() {
	    	if (!isNewLocation && runningActivity && (location != null)) {
	    		retrieveMessages();
	    	}
	    	if (runningActivity) {
	    		retrieveMsgHandler.postDelayed(this, 30000);
	    	}
	    }
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		
		SharedPreferences prefs = getSharedPreferences(Constants.PREFS_FILE_NAME, MODE_PRIVATE);
		SharedPreferences.Editor prefEdit = prefs.edit();
		
		username = getIntent().getStringExtra("username");
		device_key = getIntent().getStringExtra("device_key");
		access_token = getIntent().getStringExtra("access_token");
		
	    prefEdit.putString("username",ObscureData.obscureIt(this,username));
	    prefEdit.putString("device_key",ObscureData.obscureIt(this,device_key));
	    prefEdit.putString("access_token",ObscureData.obscureIt(this,access_token));
	    prefEdit.commit();


	    messagesView = (ListView) findViewById( R.id.messages);
	    messagesView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
	    
	    mMessageBoxView = (EditText) findViewById(R.id.message_box);
	    ((ImageButton) (findViewById(R.id.send_message_button))).setColorFilter(Color.GRAY);
	    mMessageBoxView.addTextChangedListener(new TextWatcher(){
	        public void afterTextChanged(Editable s) {
	           if (s.length() > 0) {
	        	   ((ImageButton) (findViewById(R.id.send_message_button))).setColorFilter(Color.BLACK);
	           }
	           else {
	        	   ((ImageButton) (findViewById(R.id.send_message_button))).setColorFilter(Color.GRAY);
	           }
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){}
	    }); 

		
		ArrayList<YakMessage> messages = new ArrayList<YakMessage>();
		
		boolean loadingFromStorage = false;
		// Check whether we're recreating a previously destroyed instance
	    if (savedInstanceState != null) {
	        // Restore value of members from saved state
	    	isNewLocation = savedInstanceState.getBoolean("isNewLocation");
	    	location = savedInstanceState.getParcelable("location");
	    	address = savedInstanceState.getParcelable("address");
	    	messages = savedInstanceState.getParcelableArrayList("messages");
	    } else { //try to get data from persistent storage
	    	try {
		    	FileInputStream fis = openFileInput(SAVE_FILE_NAME);
		    	ObjectInputStream is = new ObjectInputStream(fis);
		    	MainActivitySaveObject saveObject = (MainActivitySaveObject) is.readObject();
		    	
		    	is.close();
		    	deleteFile(SAVE_FILE_NAME);
		    	
		    	isNewLocation = saveObject.getIsNewLocation();
		    	location = saveObject.getLoc();
		    	address = saveObject.getAddress();
		    	messages = saveObject.getMessages();
		    	loadingFromStorage = true;
		    	updateTitle(true);
		    	
	    	} catch (Exception e) {
	    		Constants.logMessage(1,"onCreate","cannot restore data from file: "+e.getMessage());
	    	} 

	    }
	    
	    listAdapter = new YakAdapter(getApplicationContext(), messages, username);
		messagesView.setAdapter(listAdapter);
		messagesView.setClickable(true);
	    
		if (!isNewLocation) { //screen is rotated
			findViewById(R.id.message_status).setVisibility(View.GONE);
			findViewById(R.id.content_area_layout).setVisibility(View.VISIBLE);
		}
		
		if (loadingFromStorage) {
			retrieveMessages(); //load fresh messages
		}
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		//10minutes (600000), or 20m
	    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 600000, 20, networkLocationListener);
	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 600000, 20, gpsLocationListener); 
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    // Save the user's current game state
	    savedInstanceState.putBoolean("isNewLocation", isNewLocation);
	    savedInstanceState.putParcelable("location", location);
	    if (address != null) {
	    	savedInstanceState.putParcelable("address", address);
	    }
	    savedInstanceState.putParcelableArrayList("messages", listAdapter.getData());
	    
	    // Always call the superclass so it can save the view hierarchy state
	    super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		runningActivity = true;
	    checkLocationSettings(); 
	    retrieveMessages();
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		MenuItem item = menu.findItem(R.id.action_show_locked_location);

	    if (item != null) {
		    item.setOnMenuItemClickListener
		    (
		        new MenuItem.OnMenuItemClickListener() { 
		            public boolean onMenuItemClick(MenuItem item) { 
		            	onShowLockedPosition(item); 
		            	return true;
		            }
		        }
		    );
	    }
	    
	    item = menu.findItem(R.id.action_logout);

	    if (item != null) {
		    item.setOnMenuItemClickListener
		    (
		        new MenuItem.OnMenuItemClickListener() { 
		            public boolean onMenuItemClick(MenuItem item) { 
		            	onLogout(item); 
		            	return true;
		            }
		        }
		    );
	    }
		
		return true;
	}
	
	@Override
	protected void onStop() {
		// Always call the superclass so it can save the view hierarchy state
	    super.onStop();
	}
	
	@Override
	protected void onPause() {
		super.onStop();
		
		runningActivity = false;
	    //stopUpdates when inactive
		//locationManager.removeUpdates(locationListener);
		
		if (location != null) {
		    MainActivitySaveObject saveObject = new MainActivitySaveObject(location,isNewLocation,address,listAdapter.getData());
		    try {
			    FileOutputStream fos = openFileOutput(SAVE_FILE_NAME, Context.MODE_PRIVATE);
			    ObjectOutputStream os = new ObjectOutputStream(fos);
			    os.writeObject(saveObject);
			    os.close();
			    
		    } catch (Exception e) {
		    	Constants.logMessage(1,"onStop","trouble saving state: "+e.getMessage());
		    }
	    }
	}
	
	@Override
	public void finish() {
	    super.finish();
	    runningActivity = false;
	}
	
	private void onShowLockedPosition(MenuItem menu) {
		Constants.logMessage(1,"MainActivity", "Show Locked position clicked");
		if (location != null) {
			Intent i = new Intent(getApplicationContext(), YakMapActivity.class);
			i.putExtra("location", location);
			this.startActivityForResult(i, ACTIVITY_SHOW_MAP);
		} else {
			CharSequence text = getString(R.string.detecting_location);
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(this, text, duration);
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			toast.show();
		}
	}
	
	private void onLogout(MenuItem menu) {
		Constants.logMessage(1,"MainActivity", "Logout clicked");
		SharedPreferences prefs = getSharedPreferences(Constants.PREFS_FILE_NAME, MODE_PRIVATE);
		SharedPreferences.Editor prefEdit = prefs.edit();
		prefEdit.clear();
		prefEdit.commit();
		
		Intent i = new Intent(getApplicationContext(), LoginActivity.class);
		startActivity(i);
		finish();
	}
	
	public void onClickSend(View view) {
		Constants.logMessage(1,"MainActivity", "Send button clicked");
		if (mSendMessageTask != null) {
			return;
		}
		
		boolean cancel = false;

		mMessageBoxView.setError(null);
		
		messageBoxContent = mMessageBoxView.getText().toString();
		// Check for a valid message
		if (TextUtils.isEmpty(messageBoxContent)) {
			cancel = true;
		} else if (messageBoxContent.length()< 3) {
			mMessageBoxView.setError(getString(R.string.message_too_short));
			cancel = true;
		} else if (messageBoxContent.length() > 256) {
			mMessageBoxView.setError(getString(R.string.message_too_long));
			cancel = true;
		}
		
		if (cancel) {
			if (!TextUtils.isEmpty(messageBoxContent)) {
				mMessageBoxView.requestFocus();
			}
		} else {
			
			findViewById(R.id.send_progressbar).setVisibility(View.VISIBLE);
			findViewById(R.id.send_message_button).setVisibility(View.GONE);
			
			mSendMessageTask = new SendMessageTask();
			mSendMessageTask.execute((Void) null);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	   if (keyCode == KeyEvent.KEYCODE_BACK) {
	      setResult(RESULT_OK);
	      finish();
	   }
	   return super.onKeyDown(keyCode, event);
	}
	
	
	private void checkLocationSettings() {
		boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if (!gpsEnabled || !networkEnabled) { 
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage(R.string.gps_required_dialog_message)
	        .setTitle(R.string.gps_required_dialog_title);
	    	builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	            	Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	        	    startActivityForResult(settingsIntent, ACTIVITY_SHOW_ANDROID_SETTINGS);
	        	    //setResult(RESULT_OK);
	            	//finish();
	            }
	        });
		    	
	    	AlertDialog dialog = builder.create();
	    	dialog.setCancelable(false); 
	    	dialog.show();
		} else { 
			
			final Handler checkLocationLockHandler = new Handler();
			Runnable checkLocationLockRunner = new Runnable() {
			    public void run() {
			    	if (runningActivity) {
			    		checkLocationLock();
			    	}
			    }
			};
			checkLocationLockHandler.postDelayed(checkLocationLockRunner, 30000);
			
			retrieveMsgHandler.postDelayed(retrieveMsgPoll, 35000);
			
			
		}

	}
	
	private void checkLocationLock() {
		Constants.logMessage(1,"checkLocationLock","about to see if we have a lock or not");
		if (runningActivity && (location == null)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage(R.string.gps_error_dialog_message)
	        .setTitle(R.string.gps_error_dialog_title);
	    	builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	            	setResult(RESULT_OK);
	            	finish();
	            }
	        });
		    	
	    	AlertDialog dialog = builder.create();
	    	dialog.setCancelable(false);
	    	dialog.show();
		}
	}
	
	private void retrieveMessages(){
		if ((location == null) || (!runningActivity)) {
			return;
		}
		
		if (retrieving) { 
			if (isNewLocation) {
				mGetMessagesTask.cancel(true);
				mGetMessagesTask = null;
			} else {
				return;
			}
		}
		
		if (mGetMessagesTask != null) {
			return;
		}
		
		
		Constants.logMessage(1,"retrieveMessage","updating the message list");

		retrieving = true;
		
		mGetMessagesTask = new GetMessagesTask();
		mGetMessagesTask.execute((Void) null);
		
		retrieving = false;

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	   super.onActivityResult(requestCode, resultCode, data);
	   if (requestCode == ACTIVITY_SHOW_ANDROID_SETTINGS) {
		   checkLocationSettings(); 
	   } else if ((requestCode == ACTIVITY_SHOW_MAP) && (resultCode == RESULT_OK)) {
		   setNewLocation((Location) data.getParcelableExtra("location"));
	   }
	}
	
	protected void locationHasChanged(Location newLocation) {
		if (isBetterLocation(newLocation, location)) {
			if ((location == null) || (location.distanceTo(newLocation) > Constants.RANGE_LIMIT)) { 
				//out of range, so set new location
				if (location != null) {
					Constants.logMessage(1,"locationHasChanged","Distance from old location: "+Float.toString(location.distanceTo(newLocation)));
				}
				if (newLocation.getProvider() == LocationManager.NETWORK_PROVIDER) { //don't use network location fix anymore
					locationManager.removeUpdates(networkLocationListener);
				}
				setNewLocation(newLocation);
			}
        	
        }
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private void setNewLocation(Location newLocation) {
		Constants.logMessage(1,"setNewLocation", "inside setnewlocation");
		location = newLocation;
    	isNewLocation = true;
    	
    	findViewById(R.id.message_status).setVisibility(View.VISIBLE);
		findViewById(R.id.content_area_layout).setVisibility(View.GONE);
		
		//if (!runningActivity) {
    	//	return;
    	//}
		
		Context context = getApplicationContext();
		CharSequence text = getString(R.string.new_location_lock);
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, text, duration);
		toast.setGravity(Gravity.BOTTOM, 0, 0);
		toast.show();
    	
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
            (new ReverseGeocodingTask(this)).execute(new Location[] {location});
        }
   		retrieveMessages();
	}
	
	
	private boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	
	
	public class GetMessagesTask extends AsyncTask<Void, Void, Map<String,Object>> {
		@Override
		protected Map<String,Object> doInBackground(Void... params) {
	
			try {
				String last_message_received = "";
				Constants.logMessage(1,"GetMessagesTask", "about to get last mesasge received.");
				if (isNewLocation == false) {
					last_message_received = (listAdapter.getCount() > 0) ? listAdapter.getItem(listAdapter.getCount()-1).getId() : "";
				}
				Constants.logMessage(1,"GetMessagesTask", "finished with get last message received");
				YakHttpClient yakClient = new YakHttpClient("get_posts");
				Map<String,Object> ret = new HashMap<String, Object>();
				try {
					ret = yakClient.get_posts(device_key,access_token,location,last_message_received);
				} catch (Exception e) {
					Constants.logMessage(1,"GetMessagesTask","weird exception: "+e.getMessage());
				}
				Constants.logMessage(1,"GetMessagesTask", "finished yakClient API call");
				if (Integer.parseInt(((String)ret.get("status"))) != 0) {
					return null;
				}
				//Constants.logMessage(1,"GetMessagesTask", ret.toString());
				return ret;
				
			} catch (MalformedURLException e) {
				Constants.logMessage(2,"GetMessagesTask",e.getMessage());
			} catch (IOException e) {
				Constants.logMessage(2,"GetMessagesTask",e.getMessage());
			} catch (RuntimeException e) {
				Constants.logMessage(2,"GetMessagesTask",e.getMessage());			
			} catch (Exception e) {
				Constants.logMessage(2,"GetMessagesTask",e.getMessage());			
			}
			return null;
		}

		@Override
		protected void onPostExecute(final Map<String,Object> success) {
			mGetMessagesTask = null;

			if (success!=null) {
				Constants.logMessage(1,"onPostExecute","Success!");

				JSONArray json_array = (JSONArray)(success.get("data"));
				try {
					ArrayList<YakMessage> messages = new ArrayList<YakMessage>();
					for(int i = 0; i < json_array.length(); i++)
				    {
				        JSONObject json = json_array.getJSONObject(i);
				        YakMessage m = new YakMessage(json.getString("id"),
				        		json.getString("content"),
				        		json.getString("post_date"),
				        		json.getString("user_id"),
				        		json.getJSONObject("user").getString("username"),
				        		"0"
				        	);
				        messages.add(m);
				    }
					if (isNewLocation) {
						findViewById(R.id.message_status).setVisibility(View.GONE);
						findViewById(R.id.content_area_layout).setVisibility(View.VISIBLE);
					}
					if (isNewLocation) {
						listAdapter.set(messages);
						isNewLocation = false;
					} else {
						listAdapter.add(messages);
					}
					if (json_array.length() > 1) {
						messagesView.setSelection(listAdapter.getCount() - 1);
					}
					
				} catch (JSONException e) {
					Context context = getApplicationContext();
					CharSequence text = getString(R.string.error_server);
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context, text, duration);
					toast.setGravity(Gravity.BOTTOM, 0, 0);
					toast.show();
				}
			} else {
				Context context = getApplicationContext();
				CharSequence text = getString(R.string.error_server);
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(context, text, duration);
				toast.setGravity(Gravity.BOTTOM, 0, 0);
				toast.show();
			}
		}

		@Override
		protected void onCancelled() {
			//do nothing for now
			mGetMessagesTask = null;
		}

	}
	
	public class SendMessageTask extends AsyncTask<Void, Void, Map<String,Object>> {
		@Override
		protected Map<String,Object> doInBackground(Void... params) {
	
			try {

				YakHttpClient yakClient = new YakHttpClient("send_post");
				
				Map<String,Object> ret2 = new HashMap<String, Object>(); 
				
				ret2 = yakClient.send_post(device_key,access_token,messageBoxContent,location,address);
								
				if (Integer.parseInt(((String)ret2.get("status"))) != 0) {
					return null;
				}
				Constants.logMessage(1,"SendMessageTask", ret2.toString());
				return ret2;
				
			} catch (MalformedURLException e) {
				Constants.logMessage(2,"SendMessageTask",e.getMessage());
			} catch (IOException e) {
				Constants.logMessage(2,"SendMessageTask",e.getMessage());
			} catch (Exception e) {
				Constants.logMessage(2,"SendMessageTask",e.getMessage());			
			}
			return null;
		}

		@Override
		protected void onPostExecute(final Map<String,Object> ret) {
			mSendMessageTask = null;
			
			Boolean success = false;
			
			if ((ret != null) && Integer.parseInt(((String)ret.get("status"))) == 0) {
				success = true;
			}

			if (success) {
				Constants.logMessage(1,"SendMessageTask","Success!");
							
				mMessageBoxView.clearComposingText();
				mMessageBoxView.setText("");
				
				retrieveMessages();
				
				Context context = getApplicationContext();
				CharSequence text = getString(R.string.message_sent);
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.setGravity(Gravity.BOTTOM, 0, 0);
				toast.show();
				
			} else {
				
				if ((ret != null) &&(Integer.parseInt((String)ret.get("status")) == 1)){
					mMessageBoxView.setError(getString(R.string.message_invalid));
					mMessageBoxView.requestFocus();
				} else {
					Context context = getApplicationContext();
					CharSequence text = getString(R.string.error_server);
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, text, duration);
					toast.setGravity(Gravity.BOTTOM, 0, 0);
					toast.show();
				}
			}
			findViewById(R.id.send_progressbar).setVisibility(View.GONE);
			findViewById(R.id.send_message_button).setVisibility(View.VISIBLE);
		}

		@Override
		protected void onCancelled() {
			//do nothing for now
			mSendMessageTask = null;
		}

	}
	
	private class ReverseGeocodingTask extends AsyncTask<Location, Void, Void> {
	    Context mContext;

	    public ReverseGeocodingTask(Context context) {
	        super();
	        mContext = context;
	    }

	    @Override
	    protected Void doInBackground(Location... params) {
	        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
	        Constants.logMessage(1,"reversegeocodingtask","getting ready to use the reverse geocoder");
	        Location loc = params[0];
	        List<Address> addresses = null;
	        try {
	            // Call the synchronous getFromLocation() method by passing in the lat/long values.
	            addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
	        } catch (IOException e) {
	            Constants.logMessage(1,"reversegeocodingtask","exception encountered: "+e.getMessage());
	        }
	        final Boolean gotAddress;
	        if (addresses != null && addresses.size() > 0) {
	            address = addresses.get(0);
	            gotAddress = true;
	            Constants.logMessage(1,"reversegeocodingtask","address got: "+address.toString());
	        } else {
	        	gotAddress = false;
	        }
	        runOnUiThread(new Runnable(){
                @Override
                public void run(){
                	updateTitle(gotAddress);
                }
            });
	        return null;
	    }
	}
	
	private void updateTitle(Boolean gotAddress){
		if ((gotAddress == true)&&(address != null)&&(address.getAddressLine(0) != null)) {
			setTitle(getString(R.string.near)+" "+address.getAddressLine(0));
			Constants.logMessage(1,"updateTitle","setting title with addressline");
		} else {
			String strLatitude = Location.convert(location.getLatitude(), Location.FORMAT_MINUTES).replace(':', (char) 0x00B0)+"'";
			String strLongitude = Location.convert(location.getLongitude(), Location.FORMAT_MINUTES).replace(':', (char) 0x00B0)+"'";
			
			setTitle(getString(R.string.near)+" "+strLatitude+"  "+strLongitude);
			Constants.logMessage(1,"updateTitle","setting title with degrees");
		}
	}

}
