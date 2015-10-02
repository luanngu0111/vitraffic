package vn.trans.vitraffic;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import vn.trans.track.AlarmUploadService;
import vn.trans.track.RequestTrack;
import vn.trans.track.ResponseTrack;
import vn.trans.traff.AlarmDownloadService;
import vn.trans.utils.IConstants;

public class TrackTab extends FragmentActivity
		implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

	private static final String REQUESTING_LOCATION_UPDATES_KEY = "Request Update";
	private static final String LOCATION_KEY = "Location Key";
	private static final String PREV_LOCATION_KEY = "Previous Location Key";
	private static final String LAST_UPDATED_TIME_STRING_KEY = "Last Update Time";
	private GoogleMap map;

	private static Location mCurrentLocation;
	private static Location mPrevLocation = null;
	private static Location mStartLocation = null;
	private static double mCurrSpeed = 0.0;
	private boolean init = true;
	String mLastUpdateTime;
	GoogleApiClient mGoogleApiClient;
	private double mDistance = 0.0;
	boolean mRequestingLocationUpdates = true;
	private String mPlace = "";
	private PendingIntent mAlarmIntent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_track_tab);
		initilizeMap();
		buildGoogleApiClient();
		createLocationRequest();
		Intent launchIntent = new Intent(this, AlarmUploadService.class);
		mAlarmIntent = PendingIntent.getBroadcast(this, 0, launchIntent, 0);
		// updateValuesFromBundle(savedInstanceState);

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		mGoogleApiClient.connect();
		super.onStart();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		if (savedInstanceState != null) {
			// Update the value of mRequestingLocationUpdates from the Bundle,
			// and
			// make sure that the Start Updates and Stop Updates buttons are
			// correctly enabled or disabled.
			Log.i("Tag", "restored");
			if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
				mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
				// setButtonsEnabledState();
			}

			if (savedInstanceState.keySet().contains(LOCATION_KEY)) {

				mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
			}
			if (savedInstanceState.keySet().contains(PREV_LOCATION_KEY)) {

				mPrevLocation = savedInstanceState.getParcelable(PREV_LOCATION_KEY);
			}
			// Update the value of mLastUpdateTime from the Bundle and update
			// the UI.
			if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
				mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
			}
			LatLng prev = new LatLng(mPrevLocation.getLatitude(), mPrevLocation.getLongitude());
			updateUI(prev);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.track_tab, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		} else if (id == R.id.action_detail) {
			/*
			 * Tong hanh trinh Van toc Tong thoi gian
			 */
			RequestTrack rq = new RequestTrack(this);
			LatLng src = new LatLng(mPrevLocation.getLatitude(), mPrevLocation.getLongitude());
			LatLng dest = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
			rq.DistanceRequest(src, dest);
			ResponseTrack rp = ResponseTrack.createObj();
			mDistance += rp.getDistanceRp();
			long totalTime = mCurrentLocation.getTime() - mStartLocation.getTime();
			double hours = totalTime / 3600000.0;
			long hour = totalTime / 3600000;
			totalTime = totalTime % 3600000;
			long minute = totalTime / 60000;
			totalTime = totalTime % 60000;
			long second = totalTime / 1000;
			mDistance = mStartLocation.distanceTo(mCurrentLocation) / 1000.0;
			mCurrSpeed = mDistance / hours;
			String stime = hour + ":" + minute + ":" + second;
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("Thông Tin Hành Trình");
			dialog.setMessage("Tổng quãng đường: " + Math.round(mDistance * 1000) / 1000.0 + "(km) \nTổng thời gian: "
					+ stime + "\nVận tốc: " + Math.round(mCurrSpeed * 1000) / 1000.0 + "(km/h)");
			AlertDialog alert = dialog.create();
			alert.show();

		} else if (id == R.id.action_start) {
			mRequestingLocationUpdates = true;
			startLocationUpdates();
		} else if (id == R.id.action_stop) {
			mRequestingLocationUpdates = false;
			stopLocationUpdates();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// registerReceiver(receiver, new
		// IntentFilter("vn.trans.trackingtraff"));
		AlarmManager man = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		man.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), IConstants.INTERVAL,
				mAlarmIntent);
		if (mGoogleApiClient.isConnected() == false) {
			mGoogleApiClient.connect();
		}
		if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
			startLocationUpdates();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		stopLocationUpdates();
		mRequestingLocationUpdates = false;
		mPrevLocation = null;
		mGoogleApiClient.disconnect();
		super.onStop();

	}

	protected void stopLocationUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
	}

	LocationRequest mLocationRequest;

	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(IConstants.INTERVAL);
		mLocationRequest.setFastestInterval(IConstants.FAST_INTV);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	private void initilizeMap() {
		// if (map == null) {
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		Log.i("Tag", "map");
		// draw line b/w intial and final location.
		map.setMyLocationEnabled(true);
		map.getUiSettings().setZoomControlsEnabled(true);

		map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
		map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(106, 10)));

		// check if map is created successfully or not
		if (map == null) {
			Toast.makeText(getApplicationContext(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
		}
		// }
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		Location tmp = mPrevLocation;
		if (mPrevLocation == null) {
			mPrevLocation = location;
			init = true;
		} else {
			init = false;
		}
		if (mStartLocation == null) {
			mStartLocation = location;
		}
		mCurrentLocation = location;
		mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
		LatLng prev = new LatLng(mPrevLocation.getLatitude(), mPrevLocation.getLongitude());
		Log.v("point", location.getLongitude() + " " + location.getLatitude());

		updateUI(prev);

	}

	private void updateUI(LatLng prev) {
		RequestTrack rq = new RequestTrack(this);
		ResponseTrack rp = ResponseTrack.createObj();
		LatLng strt = prev;
		LatLng end = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
		rq.RoadRequest(new LatLng[] { strt, end });
		LatLng[] paths = rp.getPathsRp();
		List<LatLng> tmp = new ArrayList<LatLng>();
		if (init == false)
			tmp.add(prev);

		if (paths != null) {
			tmp.addAll(Arrays.asList(paths));
			paths = tmp.toArray(new LatLng[tmp.size()]);
			Log.v("path", String.valueOf(paths.length));
			strt = paths[0];
			for (int i = 1; i < paths.length; i++) {
				end = paths[i];
				Log.v("point", end.longitude + " " + end.latitude);
				Polyline line = map.addPolyline(new PolylineOptions().add(strt, end).width(10).color(0xff0000ff));
				line.setVisible(true);
				strt = end;
			}

			mPrevLocation = mCurrentLocation;
			mPrevLocation.setLatitude(end.latitude);
			mPrevLocation.setLongitude(end.longitude);
			WriteToFile(paths, mPlace);
		}
	}

	private void WriteToFile(LatLng[] paths, String placeid) {
		vn.trans.entities.Location loc = new vn.trans.entities.Location();
		if (paths != null) {
			loc.setArr_coord(Arrays.asList(paths));
		}
		long totalTime = mCurrentLocation.getTime() - mStartLocation.getTime();
		double hours = totalTime / 3600000.0;
		double distance = mCurrentLocation.distanceTo(mStartLocation) / 1000.0;
		loc.setSpeed(distance / hours);
		loc.setCoord(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
		loc.setUser_id(android.os.Build.SERIAL);
		loc.setRoad_id(placeid);
		loc.saveToFile();
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		Log.d("connect", "Connected");
		if (mRequestingLocationUpdates) {
			startLocationUpdates();
		}
	}

	private void startLocationUpdates() {
		// TODO Auto-generated method stub
		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
		savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
		savedInstanceState.putParcelable(PREV_LOCATION_KEY, mPrevLocation);
		savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
		super.onSaveInstanceState(savedInstanceState);
	}

	private void updateValuesFromBundle(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			// Update the value of mRequestingLocationUpdates from the Bundle,
			// and
			// make sure that the Start Updates and Stop Updates buttons are
			// correctly enabled or disabled.
			if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
				mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
				// setButtonsEnabledState();
			}

			if (savedInstanceState.keySet().contains(LOCATION_KEY)) {

				mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
			}
			if (savedInstanceState.keySet().contains(PREV_LOCATION_KEY)) {

				mPrevLocation = savedInstanceState.getParcelable(PREV_LOCATION_KEY);
			}
			// Update the value of mLastUpdateTime from the Bundle and update
			// the UI.
			if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
				mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
			}

		}
	}

}
