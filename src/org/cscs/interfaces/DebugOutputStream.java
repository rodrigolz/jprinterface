package org.cscs.interfaces;
import java.io.IOException;
import java.io.OutputStream;


public class DebugOutputStream extends OutputStream {

	final private OutputStream wrapped;
	final private static String pad = "00000000";
	public DebugOutputStream(OutputStream outputStream) {
		this.wrapped = outputStream;
	}
	
	@Override
	public void write(int b) throws IOException {
		String bs = Integer.toBinaryString(b & 0xFF);		
		String hs = Integer.toHexString(b & 0xFF);
		if (bs.length() > 8) throw new RuntimeException(bs);
		if (hs.length() > 2) throw new RuntimeException(bs);
		System.out.println(String.format("%d << sent %s%s %s%s", System.currentTimeMillis(), pad.substring(0, 8-bs.length()), bs, pad.substring(0,2-hs.length()), hs));
		wrapped.write(b);
	}

}
