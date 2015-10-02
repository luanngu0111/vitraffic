package vn.trans.vitraffic;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import vn.trans.track.Geolocation;

public class TrackerTab extends FragmentActivity {
	double latitude;
	double longi;
	private static final LatLng JLNStadium = new LatLng(28.590401000000000000, 77.233255999999980000);
	LatLng JLNS = new LatLng(28.55, 77.54);
	private double pLati, plongi, dLati, dlongi;// previous latitude and
												// longitude

	private GoogleMap map;
	private Geolocation gps;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			Log.i("Here", "onreceived");
			if (bundle != null) {

				latitude = bundle.getDouble("lati");
				Log.i("Tag", latitude + "");

				longi = bundle.getDouble("longi");
				Log.i("tag", longi + "");
				drawmap(latitude, longi);
			}
		}
	};

	public void drawmap(double latid, double longid) {
		// draw on map here
		// draw line from intial to final location and draw tracker location map

		Log.i("Tag", "map");

		// add line b/w current and prev location.

		LatLng prev = new LatLng(pLati, plongi);
		LatLng my = new LatLng(latid, longid);
		// map.moveCamera(CameraUpdateFactory.newLatLngZoom(my, 15));
		// map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

		Polyline line = map.addPolyline(new PolylineOptions().add(prev, my).width(8).color(Color.BLUE));
		line.setVisible(true);
		pLati = latid;
		plongi = longid;

	}

	// @Override
	// protected void onStart() {
	// // TODO Auto-generated method stub
	// gps = new Geolocation(TrackerTab.this);
	//
	// // check if GPS enabled
	// if (gps.canGetLocation()) {
	//
	// double latitude = gps.getLatitude();
	// double longitude = gps.getLongitude();
	//
	// // \n is for new line
	// Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " +
	// latitude + "\nLong: " + longitude,
	// Toast.LENGTH_LONG).show();
	// } else {
	// // can't get location
	// // GPS or Network is not enabled
	// // Ask user to enable GPS/network in settings
	// gps.showSettingsAlert();
	// }
	// super.onStart();
	// }

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter("vn.trans.vitraffic"));
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
		this.onStop();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_track_tab);
		final double src_lati = getIntent().getDoubleExtra("src_lati", 0.0);
		Log.i("Tracker_Src_lati", src_lati + "");
		final double src_longi = getIntent().getDoubleExtra("src_longi", 0.0);
		final double dest_lati = getIntent().getDoubleExtra("dest_lati", 0.0);
		final double dest_longi = getIntent().getDoubleExtra("dest_longi", 0.0);

		pLati = src_lati;// intialize latitude and longitude here from intent
							// data.
		plongi = src_longi;

		// & get destination lai and longi. from intent data
		dLati = dest_lati;
		dlongi = dest_longi;

		try {
			// Loading map
			initilizeMap();

		} catch (Exception e) {
			e.printStackTrace();
		}
		Intent loc_intent;
		loc_intent = new Intent(getBaseContext(), Geolocation.class);
		loc_intent.putExtra("lat", dLati);
		loc_intent.putExtra("lon", dlongi);
		startService(loc_intent);

	}

	@SuppressLint("NewApi")
	private void initilizeMap() {
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tracker_tab, menu);
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
		}
		return super.onOptionsItemSelected(item);
	}
}
