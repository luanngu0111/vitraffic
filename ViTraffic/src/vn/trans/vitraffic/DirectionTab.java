package vn.trans.vitraffic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import vn.trans.direction.FastPath;
import vn.trans.direction.PlaceAutoCompleteAdapter;
import vn.trans.json.JSONParser;
import vn.trans.utils.IConstants;
import vn.trans.utils.IURLConst;

public class DirectionTab extends FragmentActivity
		implements ConnectionCallbacks, OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
	private GoogleMap map;
	private Button bnLoad, bnSwap, bnClear;
	private AutoCompleteTextView txtStart, txtEnd;
	private static LatLng[] directions = new LatLng[2];
	private static int index = 0;
	GoogleApiClient mGoogleApiClient;
	boolean mRequestingLocationUpdates = true;
	LocationRequest mLocationRequest;
	boolean init = true;
	PlacesDialog dialog = null;
	PlaceAutoCompleteAdapter mAdapter;
	LatLng start, end;

	private static final String LOG_TAG = "places";
	private static final LatLngBounds BOUNDS_HCM = new LatLngBounds(new LatLng(10.3676, 106.0304),
			new LatLng(11.1784, 107.4257));

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_direction_tab);
		initilizeMap();
		buildGoogleApiClient();
		createLocationRequest();

		txtStart = (AutoCompleteTextView) findViewById(R.id.start);
		txtEnd = (AutoCompleteTextView) findViewById(R.id.end);

		// bnLoad = (Button) findViewById(R.id.bnLoad);
		// bnLoad.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// // GetFastestPath2(directions[0], directions[1]);
		// // showDialog(getBaseContext());
		// // openAutocompleteActivity(REQUEST_START_PLACE);
		// dialog = new PlacesDialog();
		// FragmentTransaction ft = getFragmentManager().beginTransaction();
		// dialog.show(ft, "dialog");
		// }
		// });
		// bnSwap = (Button) findViewById(R.id.bnSwap);
		// bnSwap.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// // TODO Auto-generated method stub
		// LatLng tmp = directions[0];
		// directions[0] = directions[1];
		// directions[1] = tmp;
		// }
		// });
		//
		// bnClear = (Button) findViewById(R.id.bnClear);
		// bnClear.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// if (map != null) {
		// map.clear();
		// }
		// index = 0;
		//
		// }
		// });

		mAdapter = new PlaceAutoCompleteAdapter(this, android.R.layout.simple_list_item_1, mGoogleApiClient, BOUNDS_HCM,
				null);
//		txtStart.setAdapter(mAdapter);
//		txtEnd.setAdapter(mAdapter);

		txtStart.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
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
		
		txtEnd.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
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
        These text watchers set the start and end points to null because once there's
        * a change after a value has been selected from the dropdown
        * then the value has to reselected from dropdown to get
        * the correct location.
        * */
        txtStart.addTextChangedListener(new TextWatcher() {
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

            }

        });

        txtEnd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                if(end!=null)
                {
                    end=null;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

	}

	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(LocationServices.API).addApi(Places.GEO_DATA_API)
				.addApi(Places.PLACE_DETECTION_API).build();
	}

	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(IConstants.INTERVAL);
		mLocationRequest.setFastestInterval(IConstants.FAST_INTV);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
					Toast.makeText(getApplicationContext(), "Cannot Add Marker" + directions.length, Toast.LENGTH_SHORT)
							.show();
			}
		});
		// }
	}

	/*
	 * Dialog chon dia diem tren ban do
	 */
	private void openAutocompleteActivity(int req_code) {
		try {
			// The autocomplete activity requires Google Play Services to be
			// available. The intent
			// builder checks this and throws an exception if it is not the
			// case.
			Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(this);
			startActivityForResult(intent, req_code);
		} catch (GooglePlayServicesRepairableException e) {
			// Indicates that Google Play Services is either not installed or
			// not up to date. Prompt
			// the user to correct the issue.
			GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(), 0 /* requestCode */)
					.show();
		} catch (GooglePlayServicesNotAvailableException e) {
			// Indicates that Google Play Services is not available and the
			// problem is not easily
			// resolvable.
			String message = "Google Play Services is not available: "
					+ GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

			Log.e("place", message);
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
		}
	}

	private void displayResult(List<LatLng> points) {
		if (points != null) {
			PolylineOptions po = new PolylineOptions();
			po.addAll(points);
			map.addPolyline(po.color(0xff0000ff).visible(true));
			Log.i("fast", "Drew");
		} else {
			Toast.makeText(this, "Not found fastest way. Common way is recommended", Toast.LENGTH_LONG).show();
		}
	}

	// Need a function to get point in map

	/*
	 * Get fastest path function
	 */
	private void GetFastestPath(LatLng start, LatLng end) {
		FastPath fast = FastPath.createPath();
		List<LatLng> points;
		points = fast.getFastPath(start, end);
		displayResult(points);
		// fast.getFastPath(start, end);

	}

	/**
	 * @param start
	 *            id cua nut start
	 * @param end
	 *            id cua nut end
	 */
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

	public class FindPath extends AsyncTask<Object, Object, List<LatLng>> {

		@Override
		protected List<LatLng> doInBackground(Object... params) {
			// TODO Auto-generated method stub

			List<NameValuePair> param = new ArrayList<NameValuePair>();
			param.add(new BasicNameValuePair("start", "Node_" + params[0].toString()));
			param.add(new BasicNameValuePair("end", "Node_" + params[1].toString()));
			param.add(new BasicNameValuePair("slat", String.valueOf(((LatLng) params[2]).latitude)));
			param.add(new BasicNameValuePair("slon", String.valueOf(((LatLng) params[2]).longitude)));

			param.add(new BasicNameValuePair("elat", String.valueOf(((LatLng) params[3]).latitude)));
			param.add(new BasicNameValuePair("elon", String.valueOf(((LatLng) params[3]).longitude)));
			JSONParser jParser = new JSONParser();
			JSONObject json = jParser.makeHttpRequest(IURLConst.URL_FAST_PATH, "GET", param);
			Log.i("fast", String.format(IURLConst.URL_FAST_PATH + "?start=%s&end=%s&slat=%s&slon=%s&elat=%s&elon=%s",
					params[0].toString(), params[1].toString(), String.valueOf(((LatLng) params[2]).latitude),
					String.valueOf(((LatLng) params[2]).longitude), String.valueOf(((LatLng) params[3]).latitude),
					String.valueOf(((LatLng) params[3]).longitude)));
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

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub

		// Open Google map
		if (mGoogleApiClient.isConnected() == false) {
			mGoogleApiClient.connect();
		}
		if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
			startLocationUpdates();
		}

		super.onResume();
	}

	private void startLocationUpdates() {
		// TODO Auto-generated method stub
		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		if (init) {
			CameraPosition camPos = new CameraPosition.Builder()
					.target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(15)
					.bearing(location.getBearing()).build();
			CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
			map.animateCamera(camUpd3);
			init = false;
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		Log.d("connect", "Connected");
		if (mRequestingLocationUpdates) {
			startLocationUpdates();
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub

	}
}
