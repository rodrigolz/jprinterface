package org.cscs.interfaces;

public class Utils {

	public static String byteArrayToHexString(byte[] path) {
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < path.length; i++) {
			String s = Integer.toHexString(path[i]);
			if (s.length() == 1) sb.append("0");
			sb.append(s);
		}
		
		return sb.toString();
		
	}

	public static byte[] hexStringToByteArray(String device) {
		// expect string length to be multiple of two
		int len = device.length() / 2;
		byte[] data = new byte[len];
		for (int i = 0; i < len; i++) {
			data[i] = (byte) Integer.parseInt(device.substring(i*2, i*2+2), 16);
		}
		return data;
	}

}
