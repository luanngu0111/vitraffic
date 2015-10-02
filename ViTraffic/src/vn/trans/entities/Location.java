package vn.trans.entities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import vn.trans.ftpserver.ServerUtil;
import vn.trans.json.JSONObj;
import vn.trans.track.RequestTrack;
import vn.trans.track.ResponseTrack;
import vn.trans.utils.IConstants;

public class Location extends JSONObj {
	String user_id;
	LatLng coord;
	Date time; // realtime;
	double speed;
	String road_id;
	List<LatLng> arr_coord;
	public static Date prev_time = new Date();
	Road road;

	public Location() {
		this.user_id = "";
		this.coord = new LatLng(0, 0);
		this.time = new Date();
		this.speed = 0.0f;
		this.road_id = "0";
		this.arr_coord = new ArrayList<LatLng>();
	}

	public Location(LatLng coord, Date time, float speed, String road_id) {
		this();
		this.setCoord(coord);
		this.setTime(time);
		this.setSpeed(speed);
		this.setRoad_id(road_id);
		this.arr_coord = new ArrayList<LatLng>();
	}

	public Location(String user_id, LatLng coord, Date time, float speed, String road_id, Road road) {
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
		DateFormat dateformatter = new SimpleDateFormat(IConstants.FORMAT_DATE);
		try {
			this.time = dateformatter.parse(time);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public String getRoad_id() {
		return road_id;
	}

	public void setRoad_id(String string) {
		this.road_id = string;
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

	public void saveToFile() {
		/*
		 * format: <user_id>.json
		 */
		String stime = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(time);
		String proot = "/storage/sdcard0/vitraff";
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
		String obj = conv2JsonObj().toJSONString();
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

		final ServerUtil ftpserver = ServerUtil.createServer();
		new Thread(new Runnable() {

			@Override
			public void run() {
				boolean status = false;
				status = ftpserver.serverConnect("b3_16668287", "123456789", 21);
				if (status == true) {
					Log.d("server", "Connection Success");
					ftpserver.Upload(file);
				} else {
					Log.d("server", "Connection failed");
				}
			}
		}).start();
		// Save summary file in local;
		if (arr_coord != null && arr_coord.size() > 0) {
			try {
				final File csv = new File(csvroot + "/" + user_id + ".csv");
				if (!csv.exists()) {
					PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(csv, false)));
					// Header
					out.println("ID, Longitude, Latitude, Time, Speed (km/h)");
					out.println(user_id + "," + coord.longitude + "," + coord.latitude + "," + time.toString() + ","
							+ speed);
					out.close();
				} else {
					PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(csv, true)));
					// Header
					out.println(String.format(user_id + "," + coord.longitude + "," + coord.latitude + ","
							+ time.toString() + "," + speed));
					out.close();
				}
				new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						boolean status = false;
						status = ftpserver.serverConnect("b3_16668287", "123456789", 21);
						if (status == true) {
							Log.d("server", "Connection Success");
							ftpserver.Upload(csv);
						} else {
							Log.d("server", "Connection failed");
						}
					}
				}).start();
			} catch (IOException e) {
				// exception handling left as an exercise for the reader
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

	

	@Override
	public JSONObject conv2JsonObj() {
		// TODO Auto-generated method stub
		JSONObject jo = new JSONObject();
		JSONArray jsa = new JSONArray();
		for (LatLng pos : this.arr_coord) {
			JSONObject obj = new JSONObject();
			obj.put("longitude", pos.longitude);
			obj.put("latitude", pos.latitude);
			jsa.add(obj);
		}
		jo.put("longitude", this.coord.longitude);
		jo.put("latitude", this.coord.latitude);
		jo.put("user_id", this.user_id);
		jo.put("time", this.time.toString());
		jo.put("speed", this.speed);
		jo.put("road_id", this.road_id);
		jo.put("paths", jsa);

		return jo;
	}

	@Override
	public Location conv2Obj(String json) {
		// TODO Auto-generated method stub
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(json);
			JSONObject jsonObject = (JSONObject) obj;
			this.user_id = (String) jsonObject.get("user_id");
			// this.coord = new Coordinate((Float) jsonObject.get("longtitude"),
			// (Float) jsonObject.get("latitude"));
			JSONArray arrcoord = (JSONArray) jsonObject.get("paths");
			for (int i = 0; i < arrcoord.size(); i++) {
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
			this.road_id = (String) jsonObject.get("road_id");
		} catch (org.json.simple.parser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return this;
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
