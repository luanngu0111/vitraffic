package vn.trans.entities;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import vn.trans.direction.Vertex;
import vn.trans.json.JSONObj;
import vn.trans.utils.IURLConst;

public class Road implements Comparable<Road> {
	String id_start;
	String id_end;
	LatLng pos_start;
	LatLng pos_end;
	int amount;
	double avg_speed;
	String timestamps;
	String color;
	int direct;

	public String getTimestamps() {
		return timestamps;
	}

	public void setTimestamps(String timestamps) {
		this.timestamps = timestamps;
	}

	public int getDirect() {
		return direct;
	}

	public void setDirect(int direct) {
		this.direct = direct;
	}

	public String getId_start() {
		return id_start;
	}

	public void setId_start(String id_start) {
		this.id_start = id_start;
	}

	public String getId_end() {
		return id_end;
	}

	public void setId_end(String id_end) {
		this.id_end = id_end;
	}

	public LatLng getPos_start() {
		return pos_start;
	}

	public void setPos_start(LatLng pos_start) {
		this.pos_start = pos_start;
	}

	public LatLng getPos_end() {
		return pos_end;
	}

	public void setPos_end(LatLng pos_end) {
		this.pos_end = pos_end;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public double getAvg_speed() {
		return avg_speed;
	}

	public void setAvg_speed(double avg_speed) {
		this.avg_speed = avg_speed;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public Road() {
		// TODO Auto-generated constructor stub
	}

	public static List<Road> conv2Object(String jsonStr) {
		// TODO Auto-generated method stub
		List<Road> roads = new ArrayList<Road>();
		// Loop result to put vertex (nodes) value (id, name, coord)
		try {
			JSONObject json = new JSONObject(jsonStr);
			int success = 1;// json.getInt(IURLConst.TAG_SUCCESS);
			if (success == 1) {
				JSONArray traff_arr = json.getJSONArray(IURLConst.TAG_TRAFFIC);
				for (int i = 0; i < traff_arr.length(); i++) {
					JSONObject w = traff_arr.getJSONObject(i);
					Road r = new Road();
					// r.setId_start(w.getString(IURLConst.TAG_START));
					// r.setId_end(w.getString(IURLConst.TAG_END));
					// r.setPos_start(
					// new LatLng(w.getDouble(IURLConst.TAG_START_LAT),
					// w.getDouble(IURLConst.TAG_START_LON)));
					r.setPos_end(new LatLng(w.getDouble(IURLConst.TAG_LAT), w.getDouble(IURLConst.TAG_LON)));
					r.setAvg_speed(w.getDouble(IURLConst.TAG_AVG_SPEED));
					r.setAmount(w.getInt(IURLConst.TAG_AMOUNT));
					r.setColor(w.getString(IURLConst.TAG_COLOR));
					r.setTimestamps(w.getString(IURLConst.TAG_TIME));
					r.setDirect(w.getInt(IURLConst.TAG_DIRECT));
					roads.add(r);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return roads;
	}
	
	public static List<Road> conv2Object(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		List<Road> roads = new ArrayList<Road>();
		// Loop result to put vertex (nodes) value (id, name, coord)
		try {
			JSONObject json = jsonObject;
			int success = 1;// json.getInt(IURLConst.TAG_SUCCESS);
			if (success == 1) {
				JSONArray traff_arr = json.getJSONArray(IURLConst.TAG_TRAFFIC);
				for (int i = 0; i < traff_arr.length(); i++) {
					JSONObject w = traff_arr.getJSONObject(i);
					Road r = new Road();
					// r.setId_start(w.getString(IURLConst.TAG_START));
					// r.setId_end(w.getString(IURLConst.TAG_END));
					// r.setPos_start(
					// new LatLng(w.getDouble(IURLConst.TAG_START_LAT),
					// w.getDouble(IURLConst.TAG_START_LON)));
					r.setPos_end(new LatLng(w.getDouble(IURLConst.TAG_LAT), w.getDouble(IURLConst.TAG_LON)));
					r.setAvg_speed(w.getDouble(IURLConst.TAG_AVG_SPEED));
					r.setAmount(w.getInt(IURLConst.TAG_AMOUNT));
					r.setColor(w.getString(IURLConst.TAG_COLOR));
					r.setTimestamps(w.getString(IURLConst.TAG_TIME));
					r.setDirect(w.getInt(IURLConst.TAG_DIRECT));
					roads.add(r);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return roads;
	}

	@Override
	public int compareTo(Road another) {
		// TODO Auto-generated method stub
		int val1 = this.timestamps.compareTo(another.timestamps);
		int val2 = this.direct - another.direct;
		if (this.direct < another.direct)
			return -1;
		if (this.direct > another.direct)
			return 1;
		return val1;
	}

}
