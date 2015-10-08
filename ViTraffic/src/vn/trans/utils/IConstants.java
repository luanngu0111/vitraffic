package vn.trans.utils;

public interface IConstants {
	public static String FTP_SERVER = "ftp.byethost24.com";
	public static String FORMAT_DATE = "dd/MM/yyyy HH:mm:ss";
	public static long REQUEST_TRAFF = 3600000+11*3600000;
	public static long ALARM_INTERVAL = 900000;
	public static int INTERVAL = 4000; // time to request location mms
	public static int FAST_INTV = 4000; // fast time to request location mms
	public static String KEY_API_SERV = "AIzaSyAjg-AyQtRQTb472UPe9TrLQU-ADlzWYKw";
	public static String RQ_ROAD = "https://roads.googleapis.com/v1";
	public static String RQ_DIST = "https://maps.googleapis.com/maps/api/distancematrix";
	// public static int[] COLORS = new int[] { 0x50FF0000, 0x50FD0E0A,
	// 0x50F92A1E, 0x50F54632, 0x50F16246, 0x50E18B6B,
	// 0x50D29480, 0x50BEA09C, 0x50AAACB8, 0x5096B8D4, 0x5082CAFF, 0x507ECEEF,
	// 0x507AD2DF, 0x5076D6CF, 0x5072DABF,
	// 0x506EDEAF, 0x506AE29F, 0x5066E68F, 0x5062EA7F, 0x505EEE6F, 0x505FE964 };
	public static int[] COLORS = new int[] { 0x88811F1E, 0x888D1C1B, 0x889D1817, 0x88AD1413, 0x88BD100F, 0x88DA0301,
			0x88DD1501, 0x88E12D01, 0x88E54501, 0x88E95D01, 0x88EB7A00, 0x88E67F04, 0x88E18408, 0x88DC890C, 0x88D78E10,
			0x88D29314, 0x88CD9818, 0x88C89D1C, 0x88C3A220, 0x88BEA724, 0x8889D84A };
	public static String ROOT_PATH = "/storage/sdcard0/vitraff";
	public static String USERNAME = "b24_16715952";
	public static String PASSWORD = "123456789";
	public static int PORT = 21;

}
