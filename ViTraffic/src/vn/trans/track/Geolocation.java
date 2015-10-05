package vn.trans.track;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import vn.trans.vitraffic.R;
import vn.trans.vitraffic.TrackerTab;

public class Geolocation extends Service implements LocationListener {

	private Context mContext;
	int flag = 0;
	Notification notification;
	Location mylocation = new Location("");
	Location dest_location = new Location("");
	public float distance;
	public long triptime;

	NotificationManager notifier;
	Location mStartLocation = null;

	// flag for GPS status
	boolean isGPSEnabled = false;

	// flag for network status
	boolean isNetworkEnabled = false;

	// flag for GPS status
	boolean canGetLocation = false;

	Location location; // location
	double latitude; // latitude
	double longitude; // longitude

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 4; // 1 minute

	// Declaring a Location Manager
	protected LocationManager locationManager;

	public Geolocation() {
		this.mContext = getBaseContext();
	}

	public Geolocation(Context context) {
		this.mContext = context;
		getLocation();
	}

	public Location getLocation() {
		try {
			locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

			// getting GPS status
			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				Toast.makeText(mContext, "No GPS and Network", Toast.LENGTH_LONG).show();
			} else {
				// First get location from Network Provider
				if (isNetworkEnabled) {
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					Log.d("Network", "Network");
					if (locationManager != null) {
						location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}
				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					this.canGetLocation = true;
					if (location == null) {
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						Log.d("GPS Enabled", "GPS Enabled");
						if (locationManager != null) {
							location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if (location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	/**
	 * Stop using GPS listener Calling this function will stop using GPS in your
	 * app
	 */
	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(Geolocation.this);
		}
	}

	/**
	 * Function to get latitude
	 */
	public double getLatitude() {
		if (location != null) {
			latitude = location.getLatitude();
		}

		// return latitude
		return latitude;
	}

	/**
	 * Function to get longitude
	 */
	public double getLongitude() {
		if (location != null) {
			longitude = location.getLongitude();
		}

		// return longitude
		return longitude;
	}

	/**
	 * Function to check GPS/wifi enabled
	 * 
	 * @return boolean
	 */
	public boolean canGetLocation() {
		return this.canGetLocation;
	}

	public String getTriptime() {
		long hour = triptime / 3600000;
		triptime = triptime % 3600000;
		long minute = triptime / 60000;
		triptime = triptime % 60000;
		long second = triptime / 1000;
		String stime = hour + ":" + minute + ":" + second;
		return stime;
	}

	public double getSpeed() {
		return ((distance / 1000.0) / (triptime / 3600000.0));
	}

	public float getDistance() {
		return (float) (distance / 1000.0);
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

		mylocation = getLocation();
		if (dest_location.getLatitude() == 0 && dest_location.getLatitude() == 0) {
			dest_location = mylocation;
		}
		Log.i("Tag", "location changed");
		distance = mylocation.distanceTo(dest_location);
		triptime = mylocation.getTime() - dest_location.getTime();
		Log.i("Tag", "" + distance);
		Intent intent1 = new Intent("vn.trans.vitraffic");
		intent1.putExtra("lati", mylocation.getLatitude());
		intent1.putExtra("longi", mylocation.getLongitude());
		mContext.sendBroadcast(intent1);
	}

	/**
	 * Function to show settings alert dialog On pressing Settings button will
	 * lauch Settings Options
	 */
	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

		// Setting Dialog Title
		alertDialog.setTitle("GPS is settings");

		// Setting Dialog Message
		alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				mContext.startActivity(intent);
			}
		});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		// TODO Auto-generated method stub
		mContext = this;
		Log.i("tag", "on start");

		mylocation = getLocation();

		Double msg = mylocation.getLatitude();
		Log.i("my long", msg.toString());

		Double dest_lat = intent.getDoubleExtra("lat", 0.0);
		Double dest_lon = intent.getDoubleExtra("lon", 0.0);
		Log.i("get lat", dest_lat.toString());
		Log.i("get lon", dest_lon.toString());

		this.dest_location.setLatitude(dest_lat);
		this.dest_location.setLongitude(dest_lon);
		Log.i("get lon", dest_lon.toString());
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}
}
