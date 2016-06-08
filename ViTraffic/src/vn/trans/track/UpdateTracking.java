package vn.trans.track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.os.AsyncTask;
import android.util.Log;
import vn.trans.json.JSONParser;
import vn.trans.utils.IConstants;
import vn.trans.utils.IURLConst;

public class UpdateTracking extends AsyncTask<Object, String, String> {
	LatLng position;
	double speed;

	private String getColor(double speed) {
		if (speed >= 0 && speed < 5)
			return IConstants.COLOR_RED;
		if (speed >= 5 && speed < 15)
			return IConstants.COLOR_ORANGE;
		if (speed >= 15 && speed < 30)
			return IConstants.COLOR_BLUE;

		return IConstants.COLOR_GREEN;
	}

	@Override
	protected String doInBackground(Object... param) {
		// TODO Auto-generated method stub
		android.os.Debug.waitForDebugger();
		position = (LatLng) param[0];
		speed = (Double) param[1];
		Log.i("Track", "Go in");
		JSONParser jParser = new JSONParser();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("lat", String.valueOf(position.latitude)));
		params.add(new BasicNameValuePair("lon", String.valueOf(position.longitude)));
		JSONObject json = jParser.makeHttpRequest(IURLConst.URL_FIND_WAY, "GET", params);
		double avg_speed = 0.0;
		String start = "", end = "";
		int amount = 0;
		Log.i("Track", json.toString());
		try {
			int success = json.getInt(IURLConst.TAG_SUCCESS);
			if (success == 1) {
				JSONObject w = json.getJSONObject(IURLConst.TAG_WAY);
				start = w.getString(IURLConst.TAG_START);
				end = w.getString(IURLConst.TAG_END);
				avg_speed = w.getDouble(IURLConst.TAG_AVG_SPEED);
				amount = w.getInt(IURLConst.TAG_AMOUNT);
				avg_speed = (avg_speed * amount + speed) / (++amount) * 1.0;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String urlUpdate = IURLConst.URL_UPDATE_TRAFF;
		params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("start", start));
		params.add(new BasicNameValuePair("end", end));
		params.add(new BasicNameValuePair("speed", String.valueOf(avg_speed)));
		params.add(new BasicNameValuePair("amount", String.valueOf(amount)));
		params.add(new BasicNameValuePair("color", getColor(avg_speed)));

		json = jParser.makeHttpRequest(urlUpdate, "GET", params);
		try {
			int success = json.getInt(IURLConst.TAG_SUCCESS);
			if (success == 1) {
				Log.i("Track", "Sucessfully");
			} else {
				Log.i("Track", "Failed");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
