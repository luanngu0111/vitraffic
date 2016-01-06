package vn.trans.entities;

import java.util.List;

import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import vn.trans.db.GISDbAdapter;
import vn.trans.json.JSONObj;

public class Road extends JSONObj {
	int id;
	Coordinate coordStart;
	Coordinate coordEnd;
	List<LatLng> arr_paths;
	double avg_speed;
	int way_pos; // Way position
	private static SQLiteDatabase database;

	public Road(Context context) {
		GISDbAdapter gisAdapter = new GISDbAdapter(context);
		gisAdapter.open();
		this.database = gisAdapter.getGISdatabase();

	}

	public Road() {
		// TODO Auto-generated constructor stub
	}

	public static List<Road> GetRoadList(Context context) {
		GISDbAdapter gisAdapter = new GISDbAdapter(context);
		gisAdapter.open();
		database = gisAdapter.getGISdatabase();
		Cursor cur = database.rawQuery("select * from ways_nodes, nodes where id=node_id", null);
		int iwayid = cur.getColumnIndex("way_id");
		int inodeid = cur.getColumnIndex("node_id");
		int ilat = cur.getColumnIndex("lat");
		int ilon = cur.getColumnIndex("lon");
		int iwaypos = cur.getColumnIndex("way_pos");
		int way_id = 0;
		Road r = null;
		for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
			int way_id_curr = cur.getInt(iwayid);

			int node_id = cur.getInt(inodeid);
			double lat = cur.getDouble(ilat);
			double lon = cur.getDouble(ilon);
			int way_pos = cur.getInt(iwaypos);
			if (way_id_curr != way_id) {
				r = new Road();
				

			}
			r.setId(way_id_curr);
			r.arr_paths.add(new LatLng(lat, lon));
			r.setAvg_speed(0.0);
			r.setWay_pos(way_pos);

		}
		return null;
	}

	public int getWay_pos() {
		return way_pos;
	}

	public void setWay_pos(int way_pos) {
		this.way_pos = way_pos;
	}

	public List<LatLng> getArr_paths() {
		return arr_paths;
	}

	public void setArr_paths(List<LatLng> arr_paths) {
		this.arr_paths = arr_paths;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Coordinate getCoordStart() {
		return coordStart;
	}

	public void setCoordStart(Coordinate coordStart) {
		this.coordStart = coordStart;
	}

	public Coordinate getCoordEnd() {
		return coordEnd;
	}

	public void setCoordEnd(Coordinate coordEnd) {
		this.coordEnd = coordEnd;
	}

	public double getAvg_speed() {
		return avg_speed;
	}

	public void setAvg_speed(double avg_speed) {
		this.avg_speed = avg_speed;
	}

	public void GenerateID() {

	}

	@Override
	public JSONObject conv2JsonObj() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object conv2Obj(String json) {
		// TODO Auto-generated method stub
		return null;
	}

}
