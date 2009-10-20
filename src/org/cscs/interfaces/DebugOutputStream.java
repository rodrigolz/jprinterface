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
		String bs = Integer.toBinaryString(b);		
		System.out.println(String.format("<< sent %s%s ", pad.substring(0, 8-bs.length()), bs));
		wrapped.write(b);
	}

}
