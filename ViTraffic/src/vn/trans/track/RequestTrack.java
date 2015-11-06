package vn.trans.track;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.util.Log;
import vn.trans.utils.IConstants;

public class RequestTrack {
	Context context;

	public RequestTrack(Context context) {
		super();
		this.context = context;

	}

	public void RoadRequest(LatLng[] paths) {
		String param = "";
		String pathEncoded = "";
		for (int i = 0; i < paths.length; i++) {
			LatLng co = paths[i];
			if (i < paths.length - 1) {
				pathEncoded += co.latitude + "," + co.longitude + "|";
			} else {
				pathEncoded += co.latitude + "," + co.longitude;
			}
		}
		param = String.format("path=%s&interpolate=false", pathEncoded);
		sendRequest(IConstants.RQ_ROAD, "snapToRoads", param);
	}

	public void DistanceRequest(LatLng src, LatLng dest) {
		String param = "origins=" + src.latitude + "," + src.longitude+ "&destinations=" + dest.latitude+ ","
				+ dest.longitude;
		sendRequest(IConstants.RQ_DIST, "json", param);
	}

	public void sendRequest(String url, final String output, String param) {
		RequestQueue queue = Volley.newRequestQueue(context);

		String addr = String.format("%s/%s?%s&key=%s", url, output, param,
				IConstants.KEY_API_SERV);
		Log.v("url", addr);
		StringRequest strRequest = new StringRequest(Request.Method.GET, addr, new Listener<String>() {

			@Override
			public void onResponse(String arg0) {
				// TODO Auto-generated method stub
				ResponseTrack rp = ResponseTrack.createObj();
				if (output == "snapToRoads") {
					rp.setResponseRoad(arg0);
				} else {
					rp.setResponseDist(arg0);
				}

			}
		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				// TODO Auto-generated method stub
				Log.v("response", arg0.toString());
			}
		});
		queue.add(strRequest);

	}

}
