package vn.trans.vitraffic;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.android.gms.maps.model.CameraPosition;
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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import vn.trans.track.AlarmUploadService;
import vn.trans.track.RequestTrack;
import vn.trans.track.ResponseTrack;
import vn.trans.track.UpdateTracking;
import vn.trans.utils.IConstants;
import vn.trans.utils.IURLConst;
import com.android.volley.Response.ErrorListener;

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
		bnStart.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Toast.makeText(getApplicationContext(), "Start Tracking ...", Toast.LENGTH_LONG).show();
				bnStop.setEnabled(true);
				bnStart.setEnabled(false);
				mTracking = true;
				startLocationUpdates();
			}
		});

		bnStop.setEnabled(false);
		bnStop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
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
					dialog.setTitle("Thông Tin Hành Trình");
					dialog.setMessage("Vui lòng bật chế độ ghi hành trình !");
					AlertDialog alert = dialog.create();
					alert.show();
				}
			}
		});
		// updateValuesFromBundle(savedInstanceState);

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		mGoogleApiClient.connect();
		init = true;
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
		dialog.setTitle("Thông Tin Hành Trình");
		dialog.setMessage("Tổng quãng đường: " + Math.round(mDistance * 1000) / 1000.0 + "(km) \nTổng thời gian: "
				+ stime + "\nVận tốc: " + Math.round(avgSpeed * 1000) / 1000.0 + "(km/h)");
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

		map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
		map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(106, 10)));

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
			mGoogleApiClient.disconnect();
		}

		mRequestingLocationUpdates = false;
		mTracking = false;
		mPrevLocation = null;

		super.onBackPressed();
	}

	/*
	 * Ham lang nghe su thay doi vi tri.
	 */
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		if (mPrevLocation == null) {
			mPrevLocation = location;
			CameraPosition camPos = new CameraPosition.Builder()
					.target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(15)
					.bearing(location.getBearing()).build();
			CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
			map.animateCamera(camUpd3);
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

		if (paths != null) {
			Log.v("path", String.valueOf(paths.length));
			for (int i = 1; i < paths.length; i++) {
				start = paths[i - 1];
				end = paths[i];
				Log.v("points", String.format("(%f; %f)  (%f; %f)", end.latitude, end.longitude, paths[i].latitude,
						paths[i].longitude));

				List<LatLng> point = new ArrayList<LatLng>();
				PolylineOptions po = new PolylineOptions();
				po.addAll(point);
				Polyline line = map.addPolyline(new PolylineOptions().add(start, end).width(8).color(0xff0000ff));
				line.setVisible(true);
			}

			end = paths[paths.length - 1];
			WriteToFile(paths, mPlace);
			mPrevLocation = mCurrentLocation;
			mPrevLocation.setLatitude(end.latitude);
			mPrevLocation.setLongitude(end.longitude);

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
			// curr = paths[paths.length-1];
		}
		long totalTime = mCurrentLocation.getTime() - mPrevLocation.getTime();
		double hours = totalTime / 3600000.0;
		double distance = mCurrentLocation.distanceTo(mPrevLocation) / 1000.0;
		loc.setDistance(distance);
		Log.i("speed", mCurrSpeed * 3.6 + "");
		// new UpdateTracking().execute(curr, mCurrSpeed * 3.6);
		TrackUpdate(curr, mCurrSpeed * 3.6);
		// loc.setSpeed(mCurrSpeed * 3.6);
		// loc.setCoord(curr);
		// loc.setUser_id(android.os.Build.SERIAL);
		// loc.setRoad_id(0);
		// loc.saveToDb(curr, mCurrSpeed * 3.6);
	}

	private static RequestQueue mQueue;

	public void TrackUpdate(LatLng curr, final double speed) {
		mQueue = Volley.newRequestQueue(this);

		String addr = String.format(IURLConst.URL_FIND_WAY + "?lat=%s&lon=%s", String.valueOf(curr.latitude),
				String.valueOf(curr.longitude));
		Log.v("url", addr);

		StringRequest strRequest = new StringRequest(Request.Method.GET, addr, new Listener<String>() {

			@Override
			public void onResponse(String arg0) {
				// TODO Auto-generated method stub
				double avg_speed = 0.0;
				String start = "", end = "";
				int amount = 0;

				try {
					JSONObject json = new JSONObject(arg0);
					Log.i("Track", json.toString());
					int success = json.getInt(IURLConst.TAG_SUCCESS);
					if (success == 1) {
						JSONObject w = json.getJSONObject(IURLConst.TAG_WAY);
						start = w.getString(IURLConst.TAG_START);
						end = w.getString(IURLConst.TAG_END);
						avg_speed = w.getDouble(IURLConst.TAG_AVG_SPEED);
						amount = w.getInt(IURLConst.TAG_AMOUNT);
						avg_speed = (avg_speed * amount + speed) / (++amount) * 1.0;
					}
					UpdateRequest(start, end, avg_speed, amount);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				// TODO Auto-generated method stub
				Log.v("response", arg0.toString());
			}
		});
		mQueue.add(strRequest);
	}

	private String getColor(double speed) {
		if (speed >= 0 && speed < 5)
			return IConstants.COLOR_RED;
		if (speed >= 5 && speed < 15)
			return IConstants.COLOR_ORANGE;
		if (speed >= 15 && speed < 30)
			return IConstants.COLOR_BLUE;

		return IConstants.COLOR_GREEN;
	}

	public void UpdateRequest(String start, String end, double avg_speed, int amount) {
		Map<String, String> jsonParams = new HashMap<String, String>();
		jsonParams.put("start", start);
		jsonParams.put("end", end);
		jsonParams.put("speed", String.valueOf(avg_speed));
		jsonParams.put("amount", String.valueOf(amount));
		jsonParams.put("color", getColor(avg_speed));
		JsonObjectRequest jsReq = new JsonObjectRequest(IURLConst.URL_UPDATE_TRAFF, new JSONObject(jsonParams),
				new Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject arg0) {
						// TODO Auto-generated method stub

					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						// TODO Auto-generated method stub

					}
				});

		mQueue.add(jsReq);
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

	// Dang ky gui request toa do vi tri cua user.
	private void startLocationUpdates() {
		// TODO Auto-generated method stub
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
