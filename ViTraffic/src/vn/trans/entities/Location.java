package vn.trans.entities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.util.Log;
import vn.trans.ftpserver.ServerUtil;
import vn.trans.json.JSONObj;
import vn.trans.json.JSONParser;
import vn.trans.track.RequestTrack;
import vn.trans.track.ResponseTrack;
import vn.trans.utils.IConstants;

public class Location extends JSONObj {
	String user_id;
	LatLng coord;
	Date time; // realtime;
	double speed;
	int road_id;
	int way_pos;

	public int getWay_pos() {
		return way_pos;
	}

	public void setWay_pos(int way_pos) {
		this.way_pos = way_pos;
	}

	List<LatLng> arr_coord;
	public static Date prev_time = new Date();
	Road road;
	double distance;

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public Location() {
		this.user_id = "";
		this.coord = new LatLng(0, 0);
		this.time = new Date();
		this.speed = 0.0f;
		this.road_id = 0;
		this.arr_coord = new ArrayList<LatLng>();
	}

	public Location(LatLng coord, Date time, float speed, int road_id) {
		this();
		this.setCoord(coord);
		this.setTime(time);
		this.setSpeed(speed);
		this.setRoad_id(road_id);
		this.arr_coord = new ArrayList<LatLng>();
	}

	public Location(String user_id, LatLng coord, Date time, float speed, int road_id, Road road) {
		this.user_id = user_id;
		this.coord = coord;
		this.time = time;
		this.speed = speed;
		this.road_id = road_id;
		this.road = road;
	}

	public Location(LatLng coord) {
		this();
		this.setCoord(new LatLng(coord.latitude, coord.longitude));
		Location.prev_time = time;
		this.setTime(new Date());
	}

	public Location(float longtitude, float latitude) {
		this();
		this.setCoord(new LatLng(latitude, longtitude));
		/* Set time is now */
		this.setTime(new Date());
		/* Locate the road is being */

	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
		Log.v("id", user_id);
	}

	public LatLng getCoord() {
		return coord;
	}

	public void setCoord(float longtitude, float latitude) {
		this.setCoord(new LatLng(latitude, longtitude));
	}

	public void setCoord(LatLng coord) {
		this.coord = coord;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		SimpleDateFormat format = new SimpleDateFormat(IConstants.FORMAT_DATE);

		try {
			this.time = format.parse(time.toString());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setTime(String time) {
		// SimpleDateFormat dateformatter = new
		// SimpleDateFormat(IConstants.SPEC_FORM_DATE);
		// try {
		this.time = new Date(time.toString());
		// } catch (ParseException e) {
		// TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public int getRoad_id() {
		return road_id;
	}

	public void setRoad_id(int road_id) {
		this.road_id = road_id;
	}

	public void setRoad(Road r) {
		this.road = r;
	}

	public Road getRoad() {
		return this.road;
	}

	public List<LatLng> getArr_coord() {
		return arr_coord;
	}

	public void setArr_coord(List<LatLng> arr_coord) {
		this.arr_coord = arr_coord;
		this.coord = arr_coord.get(arr_coord.size() - 1);

	}

	
	/**
	 * Luu tru file duoi dang json.
	 */
	public void saveToFile() {
		/*
		 * format: <user_id>.json
		 */
		String stime = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(time);
		String proot = IConstants.ROOT_PATH;
		String csvroot = proot + "/csv";
		File root = new File(proot);
		File croot = new File(csvroot);
		if (!root.exists()) {
			root.mkdir();
		}
		if (!croot.exists()) {
			croot.mkdir();
		}
		String filename = String.format("%s/%s-%s-%s.txt", proot, this.user_id, stime, this.road_id);
		// Goi ham tao doi tuong json
		JSONObject jsobj = conv2JsonObj();
		if (jsobj != null) {
			String obj = conv2JsonObj().toString();

			// Ghi file len server:
			Log.v("json", obj);
			final File file = new File(filename);
			try {
				FileOutputStream out = new FileOutputStream(file);
				out.write(obj.getBytes());
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// ServerUtil ftpserver = ServerUtil.createServer();
			new Thread(new Runnable() {

				@Override
				public void run() {
					boolean status = false;
					ServerUtil ftpserver = ServerUtil.createServer();
					status = ftpserver.serverConnect(IConstants.USERNAME, IConstants.PASSWORD, IConstants.PORT);
					if (status == true) {
						Log.d("server", "Connection Success");
						ftpserver.Upload(file);
					} else {
						Log.d("server", "Connection failed");
					}
				}
			}).start();

			// Luu file tong hop csv
			if (arr_coord != null && arr_coord.size() > 0) {
				try {
					final File csv = new File(csvroot + "/" + user_id + ".csv");
					if (!csv.exists()) {
						PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(csv, false)));
						// Header
						out.println("ID, Longitude, Latitude, Time, Speed (km/h), Distance (km)");
						out.println(user_id + "," + coord.longitude + "," + coord.latitude + "," + time.toString() + ","
								+ speed + "," + distance);
						out.close();
					} else {
						PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(csv, true)));
						// Header
						out.println(String.format(user_id + "," + coord.longitude + "," + coord.latitude + ","
								+ time.toString() + "," + speed + "," + distance));
						out.close();
					}
					new Thread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							boolean status = false;
							ServerUtil ftpserver = ServerUtil.createServer();
							status = ftpserver.serverConnect(IConstants.USERNAME, IConstants.PASSWORD, IConstants.PORT);
							if (status == true) {
								Log.d("server", "Connection Success CSV");
								ftpserver.Upload(csv);
							} else {
								Log.d("server", "Connection failed CSV");
							}
						}
					}).start();
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
			}
		}
	}

	/**
	 * @param filename
	 *            : ten tap tin muon load
	 */
	public void loadFromFile(String filename) {
		/*
		 * Doc file duoc chi dinh bang filename Chuyen tu doi tuong Json sang
		 * lop Location
		 */
		// Tai file tu server ve
		ServerUtil ftpserver = ServerUtil.createServer();
		String jsonStr = ftpserver.Download(filename);
		conv2Obj(jsonStr);

	}

	/*
	 * Chuyen doi tuong Location sang dinh dang JSON.
	 */
	@Override
	public JSONObject conv2JsonObj() {
		// TODO Auto-generated method stub
		try {
			JSONObject jo = new JSONObject();
			JSONArray jsa = new JSONArray();
			for (LatLng pos : this.arr_coord) {
				JSONObject obj = new JSONObject();
				obj.put("longitude", pos.longitude);
				obj.put("latitude", pos.latitude);
				jsa.put(obj);
			}
			jo.put("longitude", this.coord.longitude);
			jo.put("latitude", this.coord.latitude);
			jo.put("user_id", this.user_id);
			jo.put("time", this.time.toString());
			jo.put("speed", this.speed);
			jo.put("road_id", this.road_id);
			jo.put("way_pos", this.way_pos);
			jo.put("paths", jsa);

			return jo;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * Chuyen chuoi json sang doi tuong Location.
	 */
	@Override
	public Location conv2Obj(String json) {
		// TODO Auto-generated method stub

		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(json.toString());

			this.user_id = (String) jsonObject.get("user_id");
			// this.coord = new Coordinate((Float) jsonObject.get("longtitude"),
			// (Float) jsonObject.get("latitude"));
			JSONArray arrcoord = (JSONArray) jsonObject.get("paths");
			for (int i = 0; i < arrcoord.length(); i++) {
				JSONObject c = (JSONObject) arrcoord.get(i);
				LatLng item = new LatLng((Double) c.get("latitude"), (Double) c.get("longitude"));
				this.arr_coord.add(item);
			}
			this.setTime((String) jsonObject.get("time"));
			Object sp = jsonObject.get("speed");
			if (sp != null) {
				this.speed = (Double) sp;
			} else {
				this.speed = 0.0;
			}
			this.road_id = (Integer) jsonObject.get("road_id");
			this.way_pos = (Integer) jsonObject.get("way_pos");
			return this;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public void calcuSpeed(Context context) {
		RequestTrack rq = new RequestTrack(context);
		ResponseTrack rp = ResponseTrack.createObj();
		if (this.arr_coord != null && this.arr_coord.size() > 0) {
			rq.DistanceRequest(this.arr_coord.get(0), this.arr_coord.get(this.arr_coord.size() - 1));
			double length = rp.getDistanceRp();
			long t = -prev_time.getTime() + time.getTime();
			double time = t / 3600000.0;
			Log.v("distance", length + " " + time + " " + t);
			if (time == 0) {
				this.speed = 0;
			} else {
				this.speed = length / time;
			}

		}
	}

}
