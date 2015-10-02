package vn.trans.entities;

import java.util.List;

import org.json.simple.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import vn.trans.json.JSONObj;

public class Road extends JSONObj{
	int id;
	Coordinate coordStart;
	Coordinate coordEnd;
	List<LatLng> arr_paths;
	double avg_speed;
	
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
	
	public void GenerateID(){
		
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
