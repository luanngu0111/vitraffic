package vn.trans.track;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import android.content.Context;

public class RequestUpdateTrack {
	private static RequestUpdateTrack mInstance;
	private RequestQueue mRequestQueue;
	private static Context mCtx;

	private RequestUpdateTrack(Context context) {
		mCtx = context;
		mRequestQueue = getRequestQueue();
	}

	private RequestQueue getRequestQueue() {
		// TODO Auto-generated method stub
		if (mRequestQueue == null) {
			// getApplicationContext() is key, it keeps you from leaking the
			// Activity or BroadcastReceiver if someone passes one in.
			Cache cache = new DiskBasedCache(mCtx.getCacheDir(), 1024 * 1024); // 1MB cap

			// Set up the network to use HttpURLConnection as the HTTP client.
			Network network = new BasicNetwork(new HurlStack());

			//mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
			mRequestQueue = new RequestQueue(cache, network);
		}
		return mRequestQueue;
	}

	public static synchronized RequestUpdateTrack getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new RequestUpdateTrack(context);
		}
		return mInstance;
	}

	public <T> void addToRequestQueue(Request<T> req) {
		getRequestQueue().add(req);
		getRequestQueue().start();
	}
}
