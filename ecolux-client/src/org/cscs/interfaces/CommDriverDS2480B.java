package org.cscs.interfaces;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class CommDriverDS2480B {
	
    final private static int TIMEOUT_OPEN_MILLIS = 2000;
    
	final private SerialPort port;
	
	final private DataInputStream input;
	final private DataOutputStream output;
	private AtomicBoolean debug = new AtomicBoolean(false);
	
	private static enum Speed { REGULAR, FLEXIBLE, OVERDRIVE };
	private static enum ResetStatus { BUS_SHORTED, PRESENCE_PULSE, ALARM_PULSE, NO_REACTION };
	

	public CommDriverDS2480B(CommPortIdentifier identifier) throws PortInUseException {
    
		System.out.println(String.format("Opening %s", identifier.getName()));
		
    	if (identifier.isCurrentlyOwned())
    		throw new IllegalStateException("Comm port owned by someone else");
    	
		CommPort unknownPort = identifier.open(this.getClass().getName(), TIMEOUT_OPEN_MILLIS);
			
		if (!(unknownPort instanceof SerialPort)) 
			throw new IllegalStateException("not a serial port");
			
		this.port = (SerialPort) unknownPort;
		try {
			// this.port.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
			this.port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
						
			// flashRTS();			
			input = new DataInputStream(new DebugInputStream(debug, this.port.getInputStream()));
			output = new DataOutputStream(new DebugOutputStream(debug, this.port.getOutputStream()));

			// cause software reset by using PARITY_SPACE for one byte...
			System.out.println("initial hardware reset/clock sync");
			this.port.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_SPACE);
			output.write(0x00);
			Thread.sleep(100);
			
			// send sync (reset) pulse for clock calibration
			this.port.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			output.write(Integer.parseInt("11000001", 2));
			output.flush();
			Thread.sleep(50);			
			// note, no response byte generated
			
		} catch (UnsupportedCommOperationException e) {
			throw new IllegalStateException("port did not accept params", e);
		} catch (IOException e) {
			throw new IllegalStateException("port did not accept params", e);
		} catch (InterruptedException e) {
			throw new IllegalStateException("port did not accept params", e);
		}
	}

//	private static void flashRTSLoop(SerialPort port) throws InterruptedException {
//		while (true) {			
//			port.setRTS(true);
//			System.out.print("on");
//			Thread.sleep(1000);			
//			port.setRTS(false);
//			System.out.print("off");
//			Thread.sleep(1000);
//		}		
//	}

	private ResetStatus sendReset(Speed speed) throws IOException {
		int b = 0;
		switch (speed) {
		case REGULAR:
			b = Integer.parseInt("11000001", 2); break;
		case FLEXIBLE:
			b = Integer.parseInt("11000101", 2); break;
		case OVERDRIVE:
			b = Integer.parseInt("11001001", 2); break;			
		}
		output.write(b);
		output.flush();

		// mask check for  11X011XX  
		int read = readChecked(0xCC, 0xDC);
		if ((read & 3) == 0) return ResetStatus.BUS_SHORTED;
		if ((read & 3) == 1) return ResetStatus.PRESENCE_PULSE;
		if ((read & 3) == 2) return ResetStatus.ALARM_PULSE;
		return ResetStatus.NO_REACTION;		
	}
    

    public Collection<String> enumerateBusDevices() {
    	Collection<String> devices = new LinkedHashSet<String>();
    	Queue<byte[]> routes = new ArrayDeque<byte[]>();
    	{
    		byte[] route = new byte[8];
    		Arrays.fill(route, (byte)0x00);
    		routes.add(route);
    	}
    	System.out.println("Enumerating bus devices");
    	try {
	    	while (routes.size() > 0) {
	    		byte[] route = routes.remove();
				byte[][] result = testRoute(route);
				byte[] path = result[0];
				byte[] discrepancy = result[1];
				
				// we've ended up with one address, discrepancies or otherwise, what is it?
				System.out.println(String.format(" route %s device %s  discrepancies %s", Utils.byteArrayToHexString(route), Utils.byteArrayToHexString(path), Utils.byteArrayToHexString(discrepancy)));
	
				String s = Utils.byteArrayToHexString(path);
				assert(s.length() == 16);
				devices.add(s);
				
				// each position we have a discrepancy, add the opposite (unexplored)
				// route to routes. 64 bits to check. We can generate the new
				// route by inverting corresponding bit in path[]
				for (int b = 0; b < 64; b++) {
					int bi = b / 8;
					int bj = b % 8;
					boolean bit = ((discrepancy[bi] >> bj) & 0x1) == 1 && 
								  ((route[bi] >> bj) & 0x1) == 0;
					if (bit) { 
						// System.out.println(String.format(" discrepancy bit %d (%d %d)", b, bi, bj));
						byte[] newpath = route.clone();
						newpath[bi] ^= (1 << bj);
						System.out.println(String.format("   discrepancy bit %d, adding route %s", b, Utils.byteArrayToHexString(newpath)));
						routes.add(newpath);
					}
					
				}
					

	    	}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return devices;
	}

    private byte[][] testRoute(byte[] route) throws IOException {
    	sendCheckedReset(Speed.REGULAR);
		// System.out.println(" switch data mode");
		sendByte(0xE1);
		System.out.println(" search bus command");
		sendByteCheckedReply(0xF0, 0xF0);
		sendByte(0xE3);
		sendByte(0xB1);				
		sendByte(0xE1);
		
		System.out.println(" exploring route " + Utils.byteArrayToHexString(route) );
		byte[] tx_buf = new byte[16];
		byte[] rx_buf = new byte[16];
		
		// initialise tx_buf with route
		for (int c = 0; c < route.length; c++) {
			tx_buf[c*2+0] = (byte) ((route[c] & 1) << 1 | (route[c] & 2) << 2 | (route[c] & 4) << 3 | (route[c] & 8) << 4);
			tx_buf[c*2+1] = (byte) ((route[c] & 16) >> 3| (route[c] & 32) >> 2 | (route[c] & 64) >> 1 | (route[c] & 128) >> 0);
		}		
		for (int c = 0; c < tx_buf.length; c++) {
			sendByte(tx_buf[c]);
			rx_buf[c] = input.readByte();
		}
		System.out.println(String.format(" sent: %s", Utils.byteArrayToHexString(tx_buf)));
		System.out.println(String.format(" recv: %s", Utils.byteArrayToHexString(rx_buf)));
		
		byte[] path = new byte[8];
		byte[] dscp = new byte[8];
		for (int d = 0; d < 64; d++) {
			// [ id_rx_buf[0-9] >> (0,2,4,6) ] & 00000011
			int rd = (rx_buf[d/4] >> ((d%4)*2)) & 3;
			// bit 0 = discrepancy [0/1]
			// bit 1 = path taken [0/1]
			if ((rd & 0x1) == 0x1) dscp[d/8] = (byte) (dscp[d/8] | 1 << (d % 8));
			if ((rd & 0x2) == 0x2) path[d/8] = (byte) (path[d/8] | 1 << (d % 8));			 
		}
		
		sendByte(0xE3);  // command mode
		sendByte(0xA1);  // search accell off
		
		return new byte[][] { path, dscp }; 
	}

	public void close() {
		
    	this.port.close();
		
	}
	
   public String DS18B20singleDeviceReadAddress() throws IOException {
		
		System.out.println(String.format("reading single device address")); 

		// assume in command mode
		sendCheckedReset(Speed.REGULAR);
		
		sendByte(0xE1); // data mode
		sendByteCheckedReply(0x33, 0x33); // read rom
		byte[] address = new byte[8];
		readByteArrayFully(address, 0xFF);
		// check CRC (byte 8)
		
		// back to command mode
		sendByte(0xE3);
		// return bytes 0-7
		
		return Utils.byteArrayToHexString(address);		
		
	}

	private void readByteArrayFully(byte[] address, int dummy) throws IOException {
		for (int x = 0; x < address.length; x++) {
			sendByte(dummy);
			address[x] = this.input.readByte();
		}
		System.out.println("Read buffer " + Utils.byteArrayToHexString(address));
	}

//	private static readByteForever() throws IOException {
//		while (true) {
//			sendByte(0xFF);
//			this.input.read();
//		}
//	}

	public void DS18B20requestConversion(String device) throws IOException {
		assert(device.length() == 16);
		byte[] address = Utils.hexStringToByteArray(device);
		System.out.println(String.format("Requesting conversion for %s", device)); 
				
		// sendByte(0xE3); // cmd mode
		// sendByteCheckedReply(0x39, 0x38); // configure pullup duration 524ms
		sendByteCheckedReply(0x3B, 0x3A); // configure pullup duration 1048ms
		
		sendCheckedReset(Speed.REGULAR);
		
		sendByte(0xE1); // data mode
		if ( device == null) {
			sendByte(0xCC); // skip rom
		} else {			
			sendByteCheckedReply(0x55, 0x55); // match rom
			for (int i = 7; i >= 0; i--) 
				sendByteCheckedReply(address[i], 0x00, 0x00);
		}
		sendByte(0xE3); // cmd mode
		sendByte(0xEF); // arm strong pullup (note 1s pulse now active)
		sendByteCheckedReply(0xF1, 0xEC, 0xFC); // arm strong pullup, last 2 bits undefined

		sendByte(0xE1); // data mode
		sendByteCheckedReply(0x44, 0x44); // convert
		
		// read end of pullup code MSB(44)== 0 so expect 76 (76: MSB==0  F6: MSB==1)
		readChecked(0x76, 0xFF);
				
		sendByte(0xE3); // cmd mode
		sendByte(0xED); // disable storng pullup
		sendByteCheckedReply(0xF1, 0xEC, 0xFC); // cancel pulse + pullup reply, last 2 bits undefined
			
		sendCheckedReset(Speed.REGULAR);
		
	}

	private int sendByteCheckedReply(int b, int expect) throws IOException {
		return sendByteCheckedReply(b, expect, 0xFF);
	}
	
	private int sendByteCheckedReply(int b, int expect, int mask) throws IOException {
		expect = expect & 0x000000FF;
		sendByte(b);
		return readChecked(expect, mask);		
	}

	private int readChecked(int expect, int mask) throws IOException {
		int reply = this.input.read();
		int mreply = reply & (mask & 0x000000FF);
		if (expect != mreply) throw new RuntimeException(String.format("lost sync, expecting %s, reply %s with mask %s was %s",
					Utils.intsToHexString(expect),
					Utils.intsToHexString(reply),
					Utils.intsToHexString(mask),
					Utils.intsToHexString(mreply)
					));
		return reply;
	}

	public double DS18B20readTemperature(String device) throws IOException {
		assert(device.length() == 16);
		byte[] address = Utils.hexStringToByteArray(device);
		System.out.println(String.format("Reading temperature for %s", device)); 
		
		sendCheckedReset(Speed.REGULAR);

		sendByte(0xE1); // data mode
		sendByteCheckedReply(0x55, 0x55); // match rom
		for (int i = 7; i >= 0; i--) sendByteCheckedReply(address[i], address[i]); //send address
		sendByteCheckedReply(0xBE, 0xBE); // read scratchpad

		byte[] scratch = new byte[9];
		for (int i = 0; i < scratch.length; i++) {
			sendByte(0xFF);
			scratch[i] = this.input.readByte();
		}
		
		// output values
		System.out.println(" scatchpad " + Utils.byteArrayToHexString(scratch));
		System.out.println(String.format(" crc '%s' OK", Utils.byteArrayToHexString(scratch[8])));
		
		sendByte(0xE3); // cmd mode
		sendCheckedReset(Speed.REGULAR);
		
		ByteBuffer buffer = ByteBuffer.wrap(scratch);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		int traw = buffer.getShort();
		return ((double)traw) / 16.0;
		
	}

	private void sendCheckedReset(Speed speed) throws IOException {
		// System.out.println("resetting...");
		ResetStatus status = sendReset(speed);
		System.out.println(" bus reset performed, state: " + status.toString());
		if (status == ResetStatus.PRESENCE_PULSE) return;
		throw new IllegalStateException(status.toString());
				
	}

	private void sendByte(int b) throws IOException {
		this.output.write(b); //command mode
		this.output.flush();
	}

	@SuppressWarnings("unchecked")
	public static Set<CommPortIdentifier> getAvailableSerialPorts() {
        HashSet<CommPortIdentifier> h = new HashSet<CommPortIdentifier>();
        Enumeration<CommPortIdentifier> thePorts = CommPortIdentifier.getPortIdentifiers();
        while (thePorts.hasMoreElements()) {
            CommPortIdentifier com = thePorts.nextElement();
            switch (com.getPortType()) {
            case CommPortIdentifier.PORT_SERIAL:
                try {
                    CommPort thePort = com.open("CommUtil", 50);
                    thePort.close();
                    h.add(com);
                } catch (PortInUseException e) {
                    System.out.println("Port, "  + com.getName() + ", is in use.");
                } catch (Exception e) {
                    System.err.println("Failed to open port " +  com.getName());
                    e.printStackTrace();
                }
            }
        }
        return h;
    }

}
