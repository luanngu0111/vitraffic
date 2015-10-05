package vn.trans.vitraffic;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewDebug.FlagToString;
import android.widget.Toast;
import vn.trans.track.AlarmUploadService;
import vn.trans.track.Geolocation;
import vn.trans.track.LocationReceiver;
import vn.trans.track.RequestTrack;
import vn.trans.track.ResponseTrack;
import vn.trans.utils.IConstants;

public class TrackerTab extends FragmentActivity {
	private static final String REQUESTING_LOCATION_UPDATES_KEY = "Request Update";
	private static final String LOCATION_KEY = "Location Key";
	private static final String PREV_LOCATION_KEY = "Previous Location Key";
	private static final String LAST_UPDATED_TIME_STRING_KEY = "Last Update Time";
	private static final String MAPS = "maps";
	double latitude;
	double longi;
	private static final LatLng JLNStadium = new LatLng(28.590401000000000000, 77.233255999999980000);
	LatLng JLNS = new LatLng(28.55, 77.54);
	private static double pLati, plongi, dLati, dlongi;// previous latitude and
	// longitude

	private GoogleMap map;
	private static Geolocation gps;
	Intent loc_intent;
	String mLastUpdateTime;
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			Log.i("Here", "onreceived");
			if (bundle != null) {
				mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
				latitude = bundle.getDouble("lati");
				Log.i("Tag", latitude + "");

				longi = bundle.getDouble("longi");
				Log.i("Tag", longi + "");
				drawmap(latitude, longi);
			}
		}
	};

	public void drawmap(double latid, double longid) {
		// draw on map here
		// draw line from intial to final location and draw tracker location map

		if (pLati == 0 && plongi == 0) {
			pLati = latid;
			plongi = longid;
		}
		Log.i("Tag", "map draw");

		// add line b/w current and prev location.

		LatLng prev = new LatLng(pLati, plongi);
		LatLng my = new LatLng(latid, longid);
		// Polyline line = map.addPolyline(new PolylineOptions().add(prev,
		// my).width(8).color(Color.BLUE));
		// line.setVisible(true);
		// pLati = my.latitude;
		// plongi = my.longitude;
		RequestTrack rq = new RequestTrack(this);
		ResponseTrack rp = ResponseTrack.createObj();
		rq.RoadRequest(new LatLng[] { prev, my });
		LatLng[] paths = rp.getPathsRp();
		LatLng end = new LatLng(0, 0);
		if (paths != null && paths.length > 0) {
			Log.i("Tag", "map draw");
			LatLng start = paths[0];
			for (int i = 1; i < paths.length; i++) {
				end = paths[i];
				Polyline line = map.addPolyline(new PolylineOptions().add(start, end).width(8).color(Color.BLUE));
				Log.i("path", String.format("(%f; %f) (%f; %f)", start.latitude, start.longitude, end.latitude,
						end.longitude));
				line.setVisible(true);
				start = end;
			}
			pLati = end.latitude;
			plongi = end.longitude;
			WriteToFile(paths, "", end);
		}

	}

	private void tmpDrawmap() {
		int col1 = 0x50ff0000;
		int col2 = 0x5000ff00;
		int col3 = 0x5000dafa;
		LatLng start = new LatLng(0, 0);
		LatLng end = new LatLng(0, 106);
		LatLng mid = new LatLng(0, 50);
		map.addPolyline(new PolylineOptions().add(start, end).width(8).color(col1)).setVisible(true);
		map.addPolyline(new PolylineOptions().add(start, end).width(8).color(col2)).setVisible(true);
		map.addPolyline(new PolylineOptions().add(start, mid).width(8).color(col3)).setVisible(true);

	}

	private void WriteToFile(LatLng[] paths, String placeid, LatLng curr) {
		vn.trans.entities.Location loc = new vn.trans.entities.Location();
		if (paths != null) {
			loc.setArr_coord(Arrays.asList(paths));
		}
		long totalTime = gps.triptime;
		double hours = totalTime / 3600000.0;
		double distance = gps.getDistance();
		loc.setSpeed(distance / hours);
		loc.setCoord(curr);
		loc.setUser_id(android.os.Build.SERIAL);
		loc.setRoad_id(placeid);
		loc.saveToFile();
	}

	@Override
	protected void onResume() {
		registerReceiver(receiver, new IntentFilter("vn.trans.vitraffic"));
		super.onResume();

	}

	@Override
	protected void onPause() {
		Log.i("Tag", "Alarm stop");
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub\
		Log.i("Tag", "Stop all service");
		unregisterReceiver(receiver);
		stopService(new Intent(getBaseContext(), Geolocation.class));
		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_track_tab);
		final double src_lati = getIntent().getDoubleExtra("src_lati", 0.0);
		final double src_longi = getIntent().getDoubleExtra("src_longi", 0.0);
		final double dest_lati = getIntent().getDoubleExtra("dest_lati", 0.0);
		final double dest_longi = getIntent().getDoubleExtra("dest_longi", 0.0);
		gps = new Geolocation(TrackerTab.this);
		// check if GPS enabled
		if (!gps.canGetLocation()) {

			gps.showSettingsAlert();

		}
		// & get destination lai and longi. from intent data
		dLati = dest_lati;
		dlongi = dest_longi;

		try {
			// Loading map
			initilizeMap();

		} catch (Exception e) {
			e.printStackTrace();
		}
		loc_intent = new Intent(getBaseContext(), Geolocation.class);
		loc_intent.putExtra("lat", dLati);
		loc_intent.putExtra("lon", dlongi);
		startService(loc_intent);

	}

	@SuppressLint("NewApi")
	private void initilizeMap() {
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		Log.i("Tag", "map init");
		// draw line b/w intial and final location.
		map.setMyLocationEnabled(true);
		map.getUiSettings().setZoomControlsEnabled(true);

		map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
		map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(106, 10)));

		// check if map is created successfully or not
		if (map == null) {
			Toast.makeText(getApplicationContext(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.track_tab, menu);
		return true;
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

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("Thông Tin Hành Trình");
			dialog.setMessage("Tổng quãng đường: " + Math.round(gps.getDistance() * 1000) / 1000.0
					+ "(km) \nTổng thời gian: " + gps.getTriptime() + "\nVận tốc: "
					+ Math.round(gps.getSpeed() * 1000) / 1000.0 + "(km/h)");
			AlertDialog alert = dialog.create();
			alert.show();

		} else if (id == R.id.action_start) {

		} else if (id == R.id.action_stop) {
			stopService(loc_intent);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelable(LOCATION_KEY, new LatLng(dLati, dlongi));
		savedInstanceState.putParcelable(PREV_LOCATION_KEY, new LatLng(pLati, plongi));
		savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
		super.onSaveInstanceState(savedInstanceState);
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

			LatLng prev = null;
			if (savedInstanceState.keySet().contains(LOCATION_KEY)) {

				LatLng curr = savedInstanceState.getParcelable(LOCATION_KEY);
				dLati = curr.latitude;
				dlongi = curr.longitude;
			}
			if (savedInstanceState.keySet().contains(PREV_LOCATION_KEY)) {

				prev = savedInstanceState.getParcelable(PREV_LOCATION_KEY);
				pLati = prev.latitude;
				plongi = prev.longitude;
			}
			// Update the value of mLastUpdateTime from the Bundle and update
			// the UI.
			if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
				mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
			}
			drawmap(dLati, dlongi);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}
}
