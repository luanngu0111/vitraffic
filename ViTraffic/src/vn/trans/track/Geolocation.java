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
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import vn.trans.vitraffic.MainActivity;
import vn.trans.vitraffic.R;
import vn.trans.vitraffic.TrackerTab;

public class Geolocation extends Service implements LocationListener {

	private Context mContext;
	int flag = 0;
	Notification notification;
	Location mylocation = new Location("");
	Location dest_location = new Location("");
	float distance;
	NotificationManager notifier;

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
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters

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
				// no network provider is enabled
			} else {
				this.canGetLocation = true;
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

	@SuppressWarnings({ "deprecation", "deprecation" })
	@Override
	public void onLocationChanged(Location location) {
		mylocation = getLocation();
		Log.i("Tag", "location changed");
		distance = mylocation.distanceTo(dest_location);
		Log.i("Tag", "" + distance);
		if (flag == 0) {
			Intent intent1 = new Intent("vi.trans.vitraffic");
			intent1.putExtra("lati", mylocation.getLatitude());
			intent1.putExtra("longi", mylocation.getLongitude());
			sendBroadcast(intent1);

			// Toast.makeText(this, "broadcasted",
			// Toast.LENGTH_LONG).show();
			if ((distance) < 600) {
				Log.i("Distance", "dist. b/w < 1km");
				Log.d("location", "" + distance);
				NotificationManager notificationManager = (NotificationManager) mContext
						.getSystemService(Context.NOTIFICATION_SERVICE);
				PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 1,
						new Intent(mContext, TrackerTab.class), 0);
				Notification notification = new Notification(R.drawable.ic_launcher, "You areached ur destination!!",
						System.currentTimeMillis());
				notification.defaults |= Notification.DEFAULT_SOUND;
				notification.setLatestEventInfo(mContext, "You areached ur destination!!",
						"You areached ur destination!!", pendingIntent);

				notificationManager.notify(11, notification);
				Vibrator vi = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

				vi.vibrate(1000);
				
			}
		}
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
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
