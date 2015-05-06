package org.hawk.nativeapi;

public class HawkNativeApi {
	public static native boolean initHawk();
	
	public static native boolean checkHawk();
	
	public static native boolean tickHawk();
	
	public static native boolean protocol(int type, int size, int reserve, int crc);
	
	/*
	public static boolean initHawk() {
		return true;
	}
	
	public static boolean checkHawk() {
		return true;
	}
	
	public static boolean tickHawk() {
		return true;
	}
	
	public static boolean protocol(int type, int size, int reserve, int crc) {
		return true;
	}
	*/
}
