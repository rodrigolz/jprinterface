package org.cscs.interfaces;
import java.io.IOException;
import java.io.InputStream;


public class DebugInputStream extends InputStream {
	final private static String pad = "00000000";
	private final InputStream wrapped;
	public DebugInputStream(InputStream inputStream) {
		this.wrapped = inputStream;
	}

	@Override
	public int read() throws IOException {
		int b = wrapped.read();
		String bs = Integer.toBinaryString(b);		
		System.out.println(String.format(">> recv %s%s ", pad.substring(0, 8-bs.length()), bs));
		return b;
	}

}
