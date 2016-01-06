package vn.trans.track;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.util.Log;
import vn.trans.json.JSONParser;

public class ResponseTrack {
	String responseRoad;
	String responseDist;
	String placeId;
	private static ResponseTrack instance = null;

	public String getPlaceId() {
		return placeId;
	}

	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}

	private ResponseTrack() {
		this.responseRoad = "";
		this.responseDist = "";
		this.placeId = "";
	}

	public static ResponseTrack createObj() {
		if (instance == null) {
			instance = new ResponseTrack();
		}
		return instance;
	}

	public String getResponseRoad() {
		return responseRoad;
	}

	public String getResponseDist() {
		return responseDist;
	}

	public void setResponseRoad(String response) {
		this.responseRoad = response;
	}

	public void setResponseDist(String response) {
		this.responseDist = response;
	}

	public double getDistanceRp() {
		JSONParser parser = new JSONParser();
		Log.v("dist", responseDist);
		double length = 0.0;

		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(responseDist);

			JSONArray rows = (JSONArray) jsonObject.get("rows");
			JSONObject items = (JSONObject) rows.get(0);
			JSONArray elements = (JSONArray) items.get("elements");
			JSONObject e = (JSONObject) elements.get(0);
			JSONObject distance = (JSONObject) e.get("distance");
			length = (Long) distance.get("value");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return (length / 1000.0);
	}

	public LatLng[] getPathsRp() {
		Log.v("road", responseRoad);

		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(responseRoad);

			JSONArray snappedPoints = (JSONArray) jsonObject.get("snappedPoints");
			if (snappedPoints != null) {
				int size = snappedPoints.length();
				LatLng[] paths = new LatLng[size];
				for (int i = 0; i < size; i++) {
					JSONObject points = (JSONObject) snappedPoints.get(i);
					JSONObject location = (JSONObject) points.get("location");
					paths[i] = new LatLng((Double) location.get("latitude"), (Double) location.get("longitude"));
					this.placeId = (String) points.get("placeId");
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
