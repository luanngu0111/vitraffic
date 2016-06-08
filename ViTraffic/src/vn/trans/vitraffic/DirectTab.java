package vn.trans.vitraffic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import vn.trans.direction.FastPath;
import vn.trans.direction.PlaceAutoCompleteAdapter;
import vn.trans.json.JSONParser;
import vn.trans.utils.IURLConst;

public class DirectTab extends FragmentActivity
		implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

	protected GoogleMap map;
	protected LatLng start;
	protected LatLng end;
	AutoCompleteTextView starting;
	AutoCompleteTextView destination;
	ImageView send, clear;
	private static final String LOG_TAG = "direct";
	protected GoogleApiClient mGoogleApiClient;
	private PlaceAutoCompleteAdapter mAdapter;
	private ProgressDialog progressDialog;
	private List<Polyline> polylines;
	private static final LatLngBounds BOUNDS_HCM = new LatLngBounds(new LatLng(10.3676, 106.0304),
			new LatLng(11.1784, 107.4257));
	private TextView txtSummaryResult;
	public double duration;
	public double distance;
	public Activity activity;
	private int index;
	private LatLng[] directions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.direct_tab);
		index = 0;
		directions = new LatLng[2];
		activity = this;
		polylines = new ArrayList<Polyline>();
		mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Places.GEO_DATA_API).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();
		MapsInitializer.initialize(this);
		mGoogleApiClient.connect();

		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map_direct);

		if (mapFragment == null) {
			mapFragment = SupportMapFragment.newInstance();
			getSupportFragmentManager().beginTransaction().replace(R.id.map_direct, mapFragment).commit();
		}
		map = mapFragment.getMap();

		mAdapter = new PlaceAutoCompleteAdapter(this, android.R.layout.simple_list_item_1, mGoogleApiClient, BOUNDS_HCM,
				null);

		starting = (AutoCompleteTextView) findViewById(R.id.start);
		destination = (AutoCompleteTextView) findViewById(R.id.destination);
		send = (ImageView) findViewById(R.id.send);
		clear = (ImageView) findViewById(R.id.clear);
		txtSummaryResult = (TextView) findViewById(R.id.txtSummaryResult);
		/*
		 * Updates the bounds being used by the auto complete adapter based on
		 * the position of the map.
		 */
		map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
			@Override
			public void onCameraChange(CameraPosition position) {
				LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
				mAdapter.setBounds(bounds);
			}
		});

		CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(10.7804, 106.6896));
		CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

		map.moveCamera(center);
		map.animateCamera(zoom);
		map.setMyLocationEnabled(true);

		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {

				// CameraUpdate center = CameraUpdateFactory.newLatLng(new
				// LatLng(location.getLatitude(),location.getLongitude()));
				// CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
				//
				// map.moveCamera(center);
				// map.animateCamera(zoom);
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {

			}

			@Override
			public void onProviderEnabled(String provider) {

			}

			@Override
			public void onProviderDisabled(String provider) {

			}
		});

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				CameraUpdate center = CameraUpdateFactory
						.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
				CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);

				map.moveCamera(center);
				map.animateCamera(zoom);

			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {

			}

			@Override
			public void onProviderEnabled(String provider) {

			}

			@Override
			public void onProviderDisabled(String provider) {

			}
		});

		/*
		 * Adds auto complete adapter to both auto complete text views.
		 */
		starting.setAdapter(mAdapter);
		destination.setAdapter(mAdapter);

		/*
		 * Sets the start and destination points based on the values selected
		 * from the autocomplete text views.
		 */

		starting.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				final PlaceAutoCompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
				final String placeId = String.valueOf(item.placeId);
				Log.i(LOG_TAG, "Autocomplete item selected: " + item.description);

				/*
				 * Issue a request to the Places Geo Data API to retrieve a
				 * Place object with additional details about the place.
				 */
				PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
				placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
					@Override
					public void onResult(PlaceBuffer places) {
						if (!places.getStatus().isSuccess()) {
							// Request did not complete successfully
							Log.e(LOG_TAG, "Place query did not complete. Error: " + places.getStatus().toString());
							places.release();
							return;
						}
						// Get the Place object from the buffer.
						final Place place = places.get(0);

						start = place.getLatLng();
					}
				});

			}
		});
		destination.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				final PlaceAutoCompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
				final String placeId = String.valueOf(item.placeId);
				Log.i(LOG_TAG, "Autocomplete item selected: " + item.description);

				/*
				 * Issue a request to the Places Geo Data API to retrieve a
				 * Place object with additional details about the place.
				 */
				PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
				placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
					@Override
					public void onResult(PlaceBuffer places) {
						if (!places.getStatus().isSuccess()) {
							// Request did not complete successfully
							Log.e(LOG_TAG, "Place query did not complete. Error: " + places.getStatus().toString());
							places.release();
							return;
						}
						// Get the Place object from the buffer.
						final Place place = places.get(0);

						end = place.getLatLng();
					}
				});

			}
		});

		/*
		 * These text watchers set the start and end points to null because once
		 * there's a change after a value has been selected from the dropdown
		 * then the value has to reselected from dropdown to get the correct
		 * location.
		 */
		starting.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int startNum, int before, int count) {
				if (start != null) {
					start = null;
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				index++;

			}
		});

		destination.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

				if (end != null) {
					end = null;
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				index++;
			}
		});

		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				hideSoftKeyboard(activity);
				String txtStart = starting.getText().toString();
				String txtEnd = destination.getText().toString();
				if (txtStart.isEmpty() || txtEnd.isEmpty()) {
					if (directions != null) {
						GetFastestPath2(directions[0], directions[1]);

						txtSummaryResult.setVisibility(View.VISIBLE);
						String result = String.format("Total time: %s\nTotal distance: %f km",
								convHour2String(duration), distance);
						txtSummaryResult.setText(result);
					} else {
						Toast.makeText(getApplicationContext(), "Please choose start/destination place",
								Toast.LENGTH_LONG).show();
					}

				} else {
					GetFastestPath2(start, end);

					txtSummaryResult.setVisibility(View.VISIBLE);
					String result = String.format("Total time: %s\nTotal distance: %f km", convHour2String(duration),
							distance);
					txtSummaryResult.setText(result);
				}
			}
		});
		map.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(LatLng position) {
				// TODO Auto-generated method stub
				if (index < 2) {
					directions[index++] = position;
					map.addMarker(new MarkerOptions().position(position).title("Some where " + index).visible(true));
					Toast.makeText(getApplicationContext(), "Add marker " + index, Toast.LENGTH_SHORT).show();
				} else
					Toast.makeText(getApplicationContext(), "Cannot Add Marker" + directions.length, Toast.LENGTH_SHORT)
							.show();
			}
		});
		clear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				map.clear();
				starting.setText("");
				destination.setText("");
				txtSummaryResult.setVisibility(View.GONE);
				index = 0;
				directions = new LatLng[2];
			}
		});
	}

	public static void hideSoftKeyboard(Activity activity) {
		InputMethodManager inputMethodManager = (InputMethodManager) activity
				.getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
	}

	public String convHour2String(double hour) {
		int h = (int) hour;
		int m = (int) (hour * 60 - 60 * h);
		String rs = String.format("%d hours %d minutes", h, m);
		return rs;
	}

	private void GetFastestPath2(LatLng src, LatLng dest) {
		// find nearest src nodes nodes.lat < src.lat && nodes.lon <src.lon
		String start = FastPath.getNearestLoc(src);
		// find nearest dest nodes nodeslat < dest.lat && nodes.lon <dest.lon
		String end = FastPath.getNearestLoc(dest);
		List<LatLng> points;
		try {
			points = new FindPath()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[] { start, end, src, dest }).get();
			displayResult(points);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // fast.getFastPath(start, end);

	}

	private void displayResult(List<LatLng> points) {
		if (points != null) {
			PolylineOptions po = new PolylineOptions();
			po.addAll(points);
			map.addPolyline(po.color(0xff0000ff).visible(true));
			Log.i("fast", "Drew " +Arrays.toString(points.toArray()));

			// Start marker
			MarkerOptions options = new MarkerOptions();
			if (start != null)
				options.position(start);
			else
				options.position(directions[0]);
			options.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue));
			map.addMarker(options);

			// End marker
			options = new MarkerOptions();
			if (end != null)
				options.position(end);
			else
				options.position(directions[1]);
			options.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
			map.addMarker(options);
			CameraUpdate center;
			if (start != null)
				center = CameraUpdateFactory.newLatLng(start);
			else
				center = CameraUpdateFactory.newLatLng(directions[0]);
			CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

			map.moveCamera(center);
			map.animateCamera(zoom);

		} else {
			Toast.makeText(this, "Not found fastest way. Common way is recommended", Toast.LENGTH_LONG).show();
		}
	}

	public class FindPath extends AsyncTask<Object, Object, List<LatLng>> {
		private List<LatLng> find_path(Object... params) {
			List<NameValuePair> param = new ArrayList<NameValuePair>();
			param.add(new BasicNameValuePair("start", "Node_" + params[0].toString()));
			param.add(new BasicNameValuePair("end", "Node_" + params[1].toString()));
			param.add(
					new BasicNameValuePair("slat", String.format(Locale.ENGLISH, "%f", ((LatLng) params[2]).latitude)));
			param.add(new BasicNameValuePair("slon",
					String.format(Locale.ENGLISH, "%f", ((LatLng) params[2]).longitude)));

			param.add(
					new BasicNameValuePair("elat", String.format(Locale.ENGLISH, "%f", ((LatLng) params[3]).latitude)));
			param.add(new BasicNameValuePair("elon",
					String.format(Locale.ENGLISH, "%f", ((LatLng) params[3]).longitude)));
			JSONParser jParser = new JSONParser();
			JSONObject json = jParser.makeHttpRequest(IURLConst.URL_FAST_PATH, "GET", param);
			Log.i("fast",
					String.format(IURLConst.URL_FAST_PATH + "?start=%s&end=%s&slat=%s&slon=%s&elat=%s&elon=%s",
							params[0].toString(), params[1].toString(),
							String.format(Locale.ENGLISH, "%f", ((LatLng) params[2]).latitude),
							String.format(Locale.ENGLISH, "%f", ((LatLng) params[2]).longitude),
							String.format(Locale.ENGLISH, "%f", ((LatLng) params[3]).latitude),
							String.format(Locale.ENGLISH, "%f", ((LatLng) params[3]).longitude)));
			int success;
			try {

				success = json.getInt(IURLConst.TAG_SUCCESS);

				if (success == 1) {

					JSONArray jarr = json.getJSONArray(IURLConst.TAG_PATH);
					List<LatLng> paths = new ArrayList<LatLng>();

					for (int i = 0; i < jarr.length(); i++) {
						JSONObject jso = jarr.getJSONObject(i);
						LatLng l = new LatLng(jso.getDouble("lat"), jso.getDouble("lon"));
						paths.add(l);
					}
					duration = json.getDouble("total");
					distance = json.getDouble("tot_dist");
					return paths;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		private List<LatLng> find_route(Object... params) {
			List<NameValuePair> param = new ArrayList<NameValuePair>();
			param.add(new BasicNameValuePair("start", params[0].toString()));
			param.add(new BasicNameValuePair("end", params[1].toString()));
			JSONParser jParser = new JSONParser();
			JSONObject json = jParser.makeHttpRequest(IURLConst.URL_ROUTING, "GET", param);
			Log.i("fast",
					String.format(IURLConst.URL_FAST_PATH + "?start=%s&end=%s", params[0].toString(),
							params[1].toString(), String.format(Locale.ENGLISH, "%f", ((LatLng) params[2]).latitude),
							String.format(Locale.ENGLISH, "%f", ((LatLng) params[2]).longitude)));
			int success;
			try {

				success = json.getInt(IURLConst.TAG_SUCCESS);

				if (success == 1) {

					JSONArray jarr = json.getJSONArray(IURLConst.TAG_PATH);
					List<LatLng> paths = new ArrayList<LatLng>();

					for (int i = 0; i < jarr.length(); i++) {
						JSONObject jso = jarr.getJSONObject(i);
						LatLng l = new LatLng(jso.getDouble("lat"), jso.getDouble("lon"));
						paths.add(l);
					}
					return paths;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected List<LatLng> doInBackground(Object... params) {
			// TODO Auto-generated method stub

			return find_path(params);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.direct_tab, menu);
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

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}
}
