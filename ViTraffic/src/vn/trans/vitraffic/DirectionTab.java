package vn.trans.vitraffic;

import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import vn.trans.direction.FastestPath;

public class DirectionTab extends FragmentActivity {
	private GoogleMap map;
	private Button bnLoad, bnSwap, bnClear;
	private static LatLng[] directions = new LatLng[2];
	private static int index = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_direction_tab);
		initilizeMap();

		bnLoad = (Button) findViewById(R.id.bnLoad);
		bnLoad.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				GetFastestPath(directions[0], directions[1]);
			}
		});
		bnSwap = (Button) findViewById(R.id.bnSwap);
		bnSwap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				LatLng tmp = directions[0];
				directions[0] = directions[1];
				directions[1] = tmp;
			}
		});

		bnClear = (Button) findViewById(R.id.bnClear);
		bnClear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (map != null) {
					map.clear();
				}
			}
		});

	}

	private void initilizeMap() {
		// if (map == null) {
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapdirect)).getMap();
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

		map.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(LatLng position) {
				// TODO Auto-generated method stub
				if (index < 2) {
					directions[index++] = position;
					map.addMarker(new MarkerOptions().position(position).title("Somewhere").visible(true));
					Toast.makeText(getApplicationContext(), "Add marker " + index, Toast.LENGTH_SHORT).show();
				} else
					Toast.makeText(getApplicationContext(), "Cannot Add mk " + directions.length, Toast.LENGTH_SHORT)
							.show();
			}
		});
		// }
	}

	private void displayResult(List<LatLng> points) {
		if (points != null) {
			PolylineOptions po = new PolylineOptions();
			po.addAll(points);
			map.addPolyline(po.color(0xff0000ff).visible(true));
		}
	}

	// Need a function to get point in map

	/*
	 * Get fastest path function
	 */
	private void GetFastestPath(LatLng start, LatLng end) {
		FastestPath fast = new FastestPath(getApplicationContext());
		List<LatLng> points = fast.getFastestPath(start, end);
		displayResult(points);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.direction_tab, menu);
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
