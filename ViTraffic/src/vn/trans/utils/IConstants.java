package vn.trans.utils;

public interface IConstants {
	public static String FTP_SERVER = "ftp.byethost3.com";
	public static String FTP_USER = "b3_16668287";
	public static String FTP_PASS = "12345678";
	public static String FORMAT_DATE = "dd/MM/yyyy HH:mm:ss";
	public static long REQUEST_TRAFF = 50000000;
	public static long ALARM_INTERVAL = 900000;
	public static int INTERVAL = 4000; // time to request location mms
	public static int FAST_INTV = 4000; // fast time to request location mms
	public static String KEY_API_SERV = "AIzaSyBJbxAZEoTmQIy_Srepin2pnXqlxiwtS-w";
	public static String RQ_ROAD = "https://roads.googleapis.com/v1";
	public static String RQ_DIST = "https://maps.googleapis.com/maps/api/distancematrix";
	public static int[] COLORS = new int[] { 0xFFFF0000, 0xFFFD0E0A, 0xFFF92A1E, 0xFFF54632, 0xFFF16246, 0xFFE18B6B,
			0xFFD29480, 0xFFBEA09C, 0xFFAAACB8, 0xFF96B8D4, 0xFF82CAFF, 0xFF7ECEEF, 0xFF7AD2DF, 0xFF76D6CF, 0xFF72DABF,
			0xFF6EDEAF, 0xFF6AE29F, 0xFF66E68F, 0xFF62EA7F, 0xFF5EEE6F, 0xFF5FE964 };
	public static String ROOT_PATH = "/storage/sdcard0/vitraffic";

}
