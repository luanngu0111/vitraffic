package vn.trans.vitraffic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import vn.trans.track.RequestTrack;
import vn.trans.track.RequestUpdateTrack;
import vn.trans.track.ResponseTrack;
import vn.trans.utils.IConstants;
import vn.trans.utils.IURLConst;

public class TrackTab extends FragmentActivity
		implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

	private static final String REQUESTING_LOCATION_UPDATES_KEY = "Request Update";
	private static final String LOCATION_KEY = "Location Key";
	private static final String PREV_LOCATION_KEY = "Previous Location Key";
	private static final String LAST_UPDATED_TIME_STRING_KEY = "Last Update Time";
	private static final String INIT_KEY = "Init";
	private static final String TRACKING_KEY = "Tracking";
	private GoogleMap map;
	private static int COEF = 0; // he so

	private static Location mCurrentLocation;
	private static Location mPrevLocation = null;
	private static Location mStartLocation = null;
	private static double mCurrSpeed = 0.0;
	private boolean init = true;
	String mLastUpdateTime;
	GoogleApiClient mGoogleApiClient;
	private double mDistance = 0.0;
	boolean mRequestingLocationUpdates = true;
	boolean mTracking = false;
	private String mPlace = "";
	private PendingIntent mAlarmIntent;
	private Button bnStart, bnStop, bnDetail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_track_tab);
		mRequestingLocationUpdates = true;
		initilizeMap();
		buildGoogleApiClient();
		createLocationRequest();
		bnStart = (Button) findViewById(R.id.bnStart);
		bnStop = (Button) findViewById(R.id.bnStop);
		bnDetail = (Button) findViewById(R.id.bnDetail);
		final MarkerOptions options = new MarkerOptions();
		
		
		bnStart.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				options.position(new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()));
				options.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue));
				map.addMarker(options);
				Toast.makeText(getApplicationContext(), "Start Tracking ...", Toast.LENGTH_LONG).show();
				bnStop.setEnabled(true);
				bnStart.setEnabled(false);
				mRequestingLocationUpdates = true;
				mTracking = true;
				startLocationUpdates();
			}
		});
		init = true;
		bnStop.setEnabled(false);
		bnStop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				options.position(new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()));
				options.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
				map.addMarker(options);
				Toast.makeText(getApplicationContext(), "Stop Tracking !", Toast.LENGTH_LONG).show();
				bnStop.setEnabled(false);
				bnStart.setEnabled(true);
				mRequestingLocationUpdates = false;
				mTracking = false;
				stopLocationUpdates();
			}
		});

		final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		bnDetail.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mTracking) {
					getDetailInfo();
				} else {
					dialog.setTitle("Trip Information");
					dialog.setMessage("Please turn on Tracking Mode !");
					AlertDialog alert = dialog.create();
					alert.show();
				}
			}
		});
		// updateValuesFromBundle(savedInstanceState);

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

			if (savedInstanceState.keySet().contains(INIT_KEY)) {
				init = savedInstanceState.getBoolean(INIT_KEY);
			}

			if (savedInstanceState.keySet().contains(TRACKING_KEY)) {
				mTracking = savedInstanceState.getBoolean(TRACKING_KEY);
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

	/**
	 * Lay thong tin hanh trinh
	 */
	private void getDetailInfo() {
		long totalTime = mCurrentLocation.getTime() - mStartLocation.getTime();
		double hours = totalTime / 3600000.0;
		long hour = totalTime / 3600000;
		totalTime = totalTime % 3600000;
		long minute = totalTime / 60000;
		totalTime = totalTime % 60000;
		long second = totalTime / 1000;
		mDistance = mCurrentLocation.distanceTo(mStartLocation) / 1000.0;
		double avgSpeed = mDistance / hours;
		String stime = hour + ":" + minute + ":" + second;
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("Trip Information");
		dialog.setMessage("Total Distance : " + Math.round(mDistance * 1000) / 1000.0 + "(km) \nTotal time : "
				+ stime + "\nSpeed : " + Math.round(avgSpeed * 1000) / 1000.0 + "(km/h)");
		AlertDialog alert = dialog.create();
		alert.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
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
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (mGoogleApiClient.isConnected()) {
			stopLocationUpdates();
			mRequestingLocationUpdates = false;
			mPrevLocation = null;
			mGoogleApiClient.disconnect();
			mTracking = false;
		}
		super.onDestroy();
	}

	protected void stopLocationUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
	}

	LocationRequest mLocationRequest;

	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(IConstants.INTERVAL);
		// mLocationRequest.setFastestInterval(IConstants.FAST_INTV);
		// mLocationRequest.setSmallestDisplacement(30);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	}

	private void initilizeMap() {
		// if (map == null) {
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maptracking)).getMap();
		Log.i("Tag", "map");
		// draw line b/w intial and final location.
		map.setMyLocationEnabled(true);
		map.getUiSettings().setZoomControlsEnabled(true);

		CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(10.7804, 106.6896));
		CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

		map.moveCamera(center);
		map.animateCamera(zoom);

		// check if map is created successfully or not
		if (map == null) {
			Toast.makeText(getApplicationContext(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
		}
		// }
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (mGoogleApiClient.isConnected()) {
			stopLocationUpdates();
			mRequestingLocationUpdates = false;
			mTracking = false;
			mPrevLocation = null;
			mGoogleApiClient.disconnect();
		}
		super.onBackPressed();
	}

	/*
	 * Ham lang nghe su thay doi vi tri.
	 */
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		CameraPosition camPos = new CameraPosition.Builder()
				.target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(15)
				.bearing(location.getBearing()).build();
		CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
		map.animateCamera(camUpd3);
		if (mPrevLocation == null) {
			mPrevLocation = location;

		}
		if (mStartLocation == null) {
			mStartLocation = location;
		}

		mCurrentLocation = location;
		mCurrSpeed = location.getSpeed();
		mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
		LatLng prev = new LatLng(mPrevLocation.getLatitude(), mPrevLocation.getLongitude());
		Log.v("point", location.getLongitude() + " " + location.getLatitude());
		if (mTracking)
			updateUI(prev);
		mPrevLocation = location;
	}

	/**
	 * Ve tracking len ban do.
	 * 
	 * @param prev:
	 *            thong tin toa do vi tri truoc do.
	 */
	private void updateUI(LatLng prev) {
		RequestTrack rq = new RequestTrack(this);
		ResponseTrack rp = ResponseTrack.createObj();
		LatLng start = prev;
		LatLng end = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
		rq.RoadRequest(new LatLng[] { start, end });
		LatLng[] paths = rp.getPathsRp();

		COEF = rp.getCoef();
		Log.i("Track", "COEF:" + COEF);
		if (paths != null) {

			TrackUpdate(start, paths[0], mCurrSpeed * 3.6, COEF * IConstants.SUB_COEF);
			Log.v("path", String.valueOf(paths.length));
			for (int i = 1; i < paths.length; i++) {
				start = paths[i - 1];
				end = paths[i];
				Log.v("points", String.format("(%f; %f)", end.latitude, end.longitude));

				// List<LatLng> point = new ArrayList<LatLng>();
				// PolylineOptions po = new PolylineOptions();
				// po.addAll(point);

				// if (neg) {
				// sideStart = new LatLng(start.latitude, start.longitude -
				// IConstants.AUT_LONG);
				// sideEnd = new LatLng(end.latitude, end.longitude -
				// IConstants.AUT_LONG);
				// } else {
				// sideStart = new LatLng(start.latitude, start.longitude +
				// IConstants.AUT_LONG);
				// sideEnd = new LatLng(end.latitude, end.longitude +
				// IConstants.AUT_LONG);
				// }
				Polyline line = map.addPolyline(new PolylineOptions().add(start, end).width(8).color(0xff0000ff));
				line.setVisible(true);
				TrackUpdate(start, end, mCurrSpeed * 3.6, COEF * 100 + i);
			}

			end = paths[paths.length - 1];
			// TrackUpdate(paths[0], end, mCurrSpeed * 3.6);
			// WriteToFile(paths, mPlace);
			// mPrevLocation = mCurrentLocation;
			// mPrevLocation.setLatitude(end.latitude);
			// mPrevLocation.setLongitude(end.longitude);

		}
	}

	/**
	 * Ghi vao file
	 * 
	 * @param paths:
	 *            Danh sach toa do duoc sinh ra tu 2 toa do Dau-Cuoi.
	 * @param placeid
	 */
	private void WriteToFile(LatLng[] paths, String placeid) {
		vn.trans.entities.Location loc = new vn.trans.entities.Location();
		LatLng curr = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
		if (paths != null) {
			loc.setArr_coord(Arrays.asList(paths));
		}
		long totalTime = mCurrentLocation.getTime() - mPrevLocation.getTime();
		double hours = totalTime / 3600000.0;
		double distance = mCurrentLocation.distanceTo(mPrevLocation) / 1000.0;
		loc.setDistance(distance);
		Log.i("speed", mCurrSpeed * 3.6 + "");
		// new UpdateTracking().execute(curr, mCurrSpeed * 3.6);
		// LatLng prev = new LatLng(mPrevLocation.getLatitude(),
		// mPrevLocation.getLongitude());
		// TrackUpdate(prev, curr, mCurrSpeed * 3.6);
		// loc.setSpeed(mCurrSpeed * 3.6);
		// loc.setCoord(curr);
		// loc.setUser_id(android.os.Build.SERIAL);
		// loc.setRoad_id(0);
		// loc.saveToDb(curr, mCurrSpeed * 3.6);
	}

	public void TrackUpdate(LatLng prev, LatLng curr, double speed, int pos) {

		@SuppressWarnings("unused")
		int direct = 0;

		String addr = String.format(IURLConst.URL_UPDATE_TRAFF + "?lat=%s&lon=%s&speed=%s&direct=%s",
				String.format(Locale.ENGLISH, "%f", curr.latitude), String.format(Locale.ENGLISH, "%f", curr.longitude),
				String.valueOf(speed), String.valueOf(pos));

		Log.v("url_req", addr);

		StringRequest strRequest = new StringRequest(Request.Method.GET, addr, new Listener<String>() {

			@Override
			public void onResponse(String arg0) {
				// TODO Auto-generated method stub
				Log.i("Track", arg0.toString());

			}
		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				// TODO Auto-generated method stub
				Log.i("Track", arg0.toString());
			}
		});

		RequestUpdateTrack.getInstance(getApplicationContext()).addToRequestQueue(strRequest);
	}
	
	public void TrackingUpdate(LatLng prev, LatLng curr, double speed, int pos){
		String url =  String.format(IURLConst.URL_UPDATE_TRAFF + "?lat=%s&lon=%s&speed=%s&direct=%s",
				String.format(Locale.ENGLISH, "%f", curr.latitude), String.format(Locale.ENGLISH, "%f", curr.longitude),
				String.valueOf(speed), String.valueOf(pos));
		URL obj;
		try {
			obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			// add request header

			int responseCode = con.getResponseCode();
			Log.i("Tracking", "Update data :" + responseCode);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * public void UpdateRequest(String start, String end, double avg_speed, int
	 * amount) {
	 * 
	 * String color = getColor(avg_speed); String url =
	 * String.format(IURLConst.URL_UPDATE_TRAFF +
	 * "?start=%s&end=%s&speed=%s&amount=%d&color=%s", start, end,
	 * String.valueOf(avg_speed), amount, color); StringRequest jsReq = new
	 * StringRequest(Request.Method.GET, url, new Listener<String>() {
	 * 
	 * @Override public void onResponse(String arg0) { // TODO Auto-generated
	 * method stub Log.i("Track", "Update " + arg0); } }, new ErrorListener() {
	 * 
	 * @Override public void onErrorResponse(VolleyError arg0) { // TODO
	 * Auto-generated method stub Log.i("Track", "Update Error :" +
	 * arg0.getMessage()); } });
	 * //RequestUpdateTrack.getInstance(getApplicationContext()).
	 * addToRequestQueue(jsReq); }
	 */

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

	// Dang ky gui request toa do vi tri cua user.
	private void startLocationUpdates() {
		// TODO Auto-generated method stub
		init = false;
		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// TODO Auto-generated method stub

	}

	/*
	 * Luu tru du lieu hien tai khi ung dung tam ngung.
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
		savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
		savedInstanceState.putParcelable(PREV_LOCATION_KEY, mPrevLocation);
		savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
		savedInstanceState.putBoolean(INIT_KEY, init);
		savedInstanceState.putBoolean(TRACKING_KEY, mTracking);
		super.onSaveInstanceState(savedInstanceState);
	}

}
