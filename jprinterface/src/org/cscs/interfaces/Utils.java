package org.cscs.interfaces;

public class Utils {

	public static String byteArrayToHexString(byte... path) {
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < path.length; i++) {
			byte b = path[i];
			int bi = (b >= 0) ? b : 256+b;
			String s = Integer.toHexString(bi);
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

	public static void main(String[] args) {
		byte b = 0x00;
		System.out.println(byteArrayToHexString(b));
		b = (byte) 0x0F;
		System.out.println(byteArrayToHexString(b));
		b = (byte) 0xF0;
		System.out.println(byteArrayToHexString(b));
		b = (byte) 0xFF;
		System.out.println(byteArrayToHexString(b));
		
	}

	public static Object intsToHexString(int... ints) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ints.length; i++) {
			int bi = ints[i];
			String s = Integer.toHexString(bi);
			if (s.length() == 1) sb.append("0");
			sb.append(s);
		}
		
		return sb.toString();
	}
	
}
