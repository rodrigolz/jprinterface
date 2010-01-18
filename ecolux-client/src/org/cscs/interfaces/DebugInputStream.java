package org.cscs.interfaces;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;


public class DebugInputStream extends InputStream {
	final private static String pad = "00000000";
	final private InputStream wrapped;
	final private AtomicBoolean debug;
	
	public DebugInputStream(AtomicBoolean debug, InputStream inputStream) {
		this.wrapped = inputStream;
		this.debug = debug;
	}

	@Override
	public int read() throws IOException {
		int b = wrapped.read();
		if (debug.get()) {
			String bs = Integer.toBinaryString(b);
			String hs = Integer.toHexString(b);
			System.out.println(String.format("%d >> recv %s%s %s%s", System.currentTimeMillis(), pad.substring(0, 8-bs.length()), bs, pad.substring(0,2-hs.length()), hs));
		}
		return b;
	}

}
