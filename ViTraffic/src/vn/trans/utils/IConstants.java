package vn.trans.utils;

public interface IConstants {
	public static String FTP_SERVER = "ftp.byethost24.com";
	public static String FORMAT_DATE = "dd/MM/yyyy HH:mm:ss";
	public static long REQUEST_TRAFF = 50000000;
	public static long ALARM_INTERVAL = 900000;
	public static int INTERVAL = 4000; // time to request location mms
	public static int FAST_INTV = 4000; // fast time to request location mms
	public static String KEY_API_SERV = "AIzaSyAjg-AyQtRQTb472UPe9TrLQU-ADlzWYKw";
	public static String RQ_ROAD = "https://roads.googleapis.com/v1";
	public static String RQ_DIST = "https://maps.googleapis.com/maps/api/distancematrix";
	public static int[] COLORS = new int[] { 0x50FF0000, 0x50FD0E0A, 0x50F92A1E, 0x50F54632, 0x50F16246, 0x50E18B6B,
			0x50D29480, 0x50BEA09C, 0x50AAACB8, 0x5096B8D4, 0x5082CAFF, 0x507ECEEF, 0x507AD2DF, 0x5076D6CF, 0x5072DABF,
			0x506EDEAF, 0x506AE29F, 0x5066E68F, 0x5062EA7F, 0x505EEE6F, 0x505FE964 };
	public static String ROOT_PATH = "/storage/sdcard0/vitraff";
	public static String USERNAME = "b24_16715952";
	public static String PASSWORD = "123456789";
	public static int PORT = 21;

}
