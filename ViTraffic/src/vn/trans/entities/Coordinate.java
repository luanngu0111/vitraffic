package vn.trans.entities;

/**
 * @author root Lop luu thuoc tinh cua toa do
 */
public class Coordinate {
	double longtitude;
	double latitude;

	public Coordinate() {
		this.longtitude = 0;
		this.latitude = 0;
	}

	public Coordinate(double latid, double longid) {
		this.longtitude = latid;
		this.latitude = longid;
	}

	public double getLongtitude() {
		return longtitude;
	}

	public void setLongtitude(float longtitude) {
		this.longtitude = longtitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	public static double distance(Coordinate curr, Coordinate prev) {
		double length = 0.0;
		double mlong = curr.longtitude - prev.longtitude;
		double mlat = curr.latitude - prev.latitude;
		length = Math.sqrt(Math.pow(mlong, 2) + Math.pow(mlat, 2));
		return length;

	}

}
