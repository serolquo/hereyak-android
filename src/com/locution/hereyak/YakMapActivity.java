package com.locution.hereyak;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;

public class YakMapActivity extends FragmentActivity {
	private GoogleMap mMap;
	private Location currentLocation;
	private Location newLocation;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_yak_map);
		// Show the Up button in the action bar.
		setupActionBar();
		
	}
	
	@Override
	public void onStart () {
		super.onStart();
		currentLocation = getIntent().getParcelableExtra("location");
		//currentLocation = new Location("blah");
		//currentLocation.setLongitude(-113.491);
		//currentLocation.setLatitude(53.5435);
		
		mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.setMyLocationEnabled(true);
		moveMapToLocation(currentLocation);
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.yak_map, menu);
		
		MenuItem item = menu.findItem(R.id.action_lock_location);

	    if (item != null) {
		    item.setOnMenuItemClickListener
		    (
		        new MenuItem.OnMenuItemClickListener() { 
		            public boolean onMenuItemClick(MenuItem item) { 
		            	lockPosition(item); 
		            	return true;
		            }
		        }
		    );
	    }
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	   if (keyCode == KeyEvent.KEYCODE_BACK) {
		   finishActivity();
	   }
	   return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void finish() {
	    finishActivity();
	    super.finish();
	}
	
	private void lockPosition(MenuItem item) {
		Location loc = mMap.getMyLocation();
		if (loc != null) {
			if (currentLocation.distanceTo(loc) > Constants.RANGE_FOR_NEW_LOCATION) {
				newLocation = loc;
				CharSequence text = getString(R.string.current_location_locked);
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(this, text, duration);
				toast.setGravity(Gravity.BOTTOM, 0, 0);
				toast.show();
				moveMapToLocation(newLocation);
				
			} else {
				CharSequence text = getString(R.string.current_location_too_close);
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(this, text, duration);
				toast.setGravity(Gravity.BOTTOM, 0, 0);
				toast.show();
			}
			
		} else {
			CharSequence text = getString(R.string.cannot_get_current_location);
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(this, text, duration);
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			toast.show();
		}
	}
	
	private void moveMapToLocation(Location loc) {
		mMap.clear();

		LatLng latlng = new LatLng(loc.getLatitude(), loc.getLongitude());
		
		mMap.addMarker(new MarkerOptions()
        .position(latlng)
        .title(getString(R.string.locked_location))
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));

		mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
		mMap.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);
		
		CircleOptions circleOptions = new CircleOptions()
		.center(latlng)
		.radius(Constants.RANGE_LIMIT) // In meters
		.strokeWidth(5)
		.strokeColor(Color.DKGRAY); 
		mMap.addCircle(circleOptions);
	}
	
	private void finishActivity() {
		if (newLocation != null) {
			Intent resultIntent = new Intent();
			resultIntent.putExtra("location", newLocation);
			setResult(RESULT_OK, resultIntent);
			
		} else {
			setResult(RESULT_CANCELED);
		}
	}

}
