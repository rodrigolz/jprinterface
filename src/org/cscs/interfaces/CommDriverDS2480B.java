package org.cscs.interfaces;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class CommDriverDS2480B {
	
    final private static int TIMEOUT_OPEN_MILLIS = 2000;
    
	final private SerialPort port;
	
	final private DataInputStream input;
	final private DataOutputStream output;
	
	final private Semaphore semaphore = new Semaphore(1);
	
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
			input = new DataInputStream(new DebugInputStream(this.port.getInputStream()));
			output = new DataOutputStream(new DebugOutputStream(this.port.getOutputStream()));

			// cause software reset by using PARITY_SPACE for one byte...
			System.out.println("sending hardware reset");
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

	private static void flashRTSLoop(SerialPort port) throws InterruptedException {
		while (true) {			
			port.setRTS(true);
			System.out.print("on");
			Thread.sleep(1000);			
			port.setRTS(false);
			System.out.print("off");
			Thread.sleep(1000);
		}		
	}

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
		
		int read = input.read();
		if ((read & 3) == 0) return ResetStatus.BUS_SHORTED;
		if ((read & 3) == 1) return ResetStatus.PRESENCE_PULSE;
		if ((read & 3) == 2) return ResetStatus.ALARM_PULSE;
		return ResetStatus.NO_REACTION;
		
	}
    

    private List<String> enumerateBusDevices() {
    	List<String> devices = new ArrayList<String>();
    	try {
    		System.out.println("enumerating bus devices");
			    		
    		byte[] id_tx_buf = new byte[16];
			byte[] id_rx_buf = new byte[16];
			boolean busy = true;
			int highestDiscrepancy = -1;
			
			while (busy) {
    			sendCheckedReset(Speed.REGULAR);
				System.out.println("switch data mode");
				sendByte(0xE1);
				System.out.println("search bus");
				sendByteCheckedReply(0xF0, 0xF0);
				sendByte(0xE3);
				sendByte(0xB1);				
				sendByte(0xE1);
				System.out.println("sending 16-byte search address");
				for (int c = 0; c < id_tx_buf.length; c++) {
					sendByte(id_tx_buf[c]);
					id_rx_buf[c] = input.readByte();
				}
				
				busy = false;
				int discrepancies = 0;
				byte[] path = new byte[8];
				for (int d = 0; d < 64; d++) {
					int rd = (id_rx_buf[d/4] >> ((d%4)*2)) & 3;
					// System.out.println(String.format(" bit %d  reads %d", d, rd));
					switch (rd) {
					case 0: // no discrepancy, chose 0
						break ;
					case 1: // discrepancy, chose 0
						System.out.println(String.format("discrepancy at %d", d));
						if (highestDiscrepancy < d) highestDiscrepancy = d;
						discrepancies++;
						break;
					case 2: 	
						// no discrepancy, chose 1
						path[d/8] = (byte) (path[d/8] | 1 << (d % 8));
						break; 
					case 3:
						// discrepancy, chose 1
						System.out.println(String.format("discrepancy at %d", d));
						if (highestDiscrepancy < d) highestDiscrepancy = d;
						discrepancies++;
						break;
					} 
					
					// t = d ;
					// id_tx_buf[t/4] = (byte) (id_tx_buf[t/4] | (0x03 << ((t%4)*2)));
					// busy = true;

				}
				if (discrepancies == 0) {
					// only one node
					String s = Utils.byteArrayToHexString(path);
					System.out.println(String.format("discrepancies %d  address %s", discrepancies, s));
					assert(s.length() == 16);
					devices.add(s);
				}
				
				sendByte(0xE3);  // command mode
				sendByte(0xA1);  // search accell off
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return devices;
	}

    private void close() {
		
    	this.port.close();
		
	}
	
    
    public static void main ( String[] args ) {
        
    	CommDriverDS2480B driver = null;
    	try {
        	
        	Set<CommPortIdentifier> ports = getAvailableSerialPorts();
        	for (CommPortIdentifier port:ports) {
        		System.out.println(port.getName());
        	}
            
        	driver = new CommDriverDS2480B(CommPortIdentifier.getPortIdentifier("/dev/ttyUSB0"));
        	
        	double temp = 0;
        	while (true) {
	        	List<String> devices = driver.enumerateBusDevices();
	
	        	driver.DS18B20singleDeviceReadAddress();
	        	
	        	for (String device: devices) {
	        		if (!"281a2106020000f9".equals(device)) throw new RuntimeException("wrong device '" + device + "'");
	        		driver.DS18B20requestConversion(device);
	        		temp = driver.DS18B20readTemperature(device);
	        		System.out.println(String.format("TEMPERATURE                              TEMPERATURE %f", temp));
	        	}
	        	
	        	Thread.sleep(1000);
	        	if (temp > 80.0) break;
        	}
        	//driver.enumerateBusDevices();
        	//driver.enumerateBusDevices();
        	//driver.enumerateBusDevices();
        	//driver.enumerateBusDevices();
        	//driver.enumerateBusDevices();

        	
        	System.out.println("finished");
        	
        } catch ( Exception e ) {
            e.printStackTrace();
        } finally {
        	driver.close();
        }
    }
    
	private void DS18B20singleDeviceReadAddress() throws IOException {
		
		System.out.println(String.format("reading single device address")); 

		// assume in command mode
		sendCheckedReset(Speed.REGULAR);
		
		sendByte(0xE1); // data mode
		sendByteCheckedReply(0x33, 0x33); // read rom
		byte[] address = new byte[9];
		readByteArrayFully(address, 0xFF);
		// check CRC (byte 8)
		
		// back to command mode
		sendByte(0xE3);
		// return bytes 0-7
		
	}

	private void readByteArrayFully(byte[] address, int dummy) throws IOException {
		for (int x = 0; x < address.length; x++) {
			sendByte(dummy);
			address[x] = this.input.readByte();
		}
		System.out.println("Read buffer " + Utils.byteArrayToHexString(address));
	}

	private void readByteForever() throws IOException {
		while (true) {
			sendByte(0xFF);
			this.input.read();
		}
	}

	private void DS18B20requestConversion(String device) throws IOException {
		assert(device.length() == 16);
		byte[] address = Utils.hexStringToByteArray(device);
		System.out.println(String.format("requesting conversion for %s", device)); 
				
		// sendByte(0xE3); // cmd mode
		// sendByteCheckedReply(0x39, 0x38); // configure pullup duration 524ms
		sendByteCheckedReply(0x3B, 0x3A); // configure pullup duration 1048ms
		
		sendCheckedReset(Speed.REGULAR);
		
		sendByte(0xE1); // data mode
		if ( device == null) {
			sendByte(0xCC); // skip rom
		} else {			
			sendByteCheckedReply(0x55, 0x55); // match rom
			for (int i = 0; i < 8; i++) 
				sendByteCheckedReply(address[i], 0x00, 0x00);
		}
		sendByte(0xE3); // cmd mode
		sendByteCheckedReply(0xEF, 0xEC, 0xFC); // arm strong pullup, last 2 bits undefined

		// sendByte(0xF1); // terminate on pulse
		// reply = input.read();
		// if (reply != 0x28) throw new RuntimeException("lost sync " + Integer.toHexString(reply));
		
		sendByte(0xE1); // data mode
		sendByteCheckedReply(0x44, 0x44); // convert
		
		// read end of pullup code MSB(44)== 0 so expect 76 (76: MSB==0  F6: MSB==1)
		int reply = input.read();
		if (reply != 0x76) throw new RuntimeException("lost sync " +Integer.toHexString(reply));
				
		sendByte(0xE3); // cmd mode
		sendByteCheckedReply(0xED, 0xEC, 0xFC); // disable strong pullup, last 2 bits undefined
		// sendByte(0xF1); // restore terminate on pulse
		
		sendCheckedReset(Speed.REGULAR);
		
	}

	private int sendByteCheckedReply(int b, int expect) throws IOException {
		return sendByteCheckedReply(b, expect, 0xFF);
	}
	
	private int sendByteCheckedReply(int b, int expect, int mask) throws IOException {
		expect = expect & 0x000000FF;
		sendByte(b);
		int reply = input.read();
		int mreply = reply & (mask & 0x000000FF);
		if (expect != mreply) throw new RuntimeException(String.format("lost sync, expecting %s, reply %s with mask %s was %s",
					Utils.intsToHexString(expect),
					Utils.intsToHexString(reply),
					Utils.intsToHexString(mask),
					Utils.intsToHexString(mreply)
					));
		return reply;		
	}

	private double DS18B20readTemperature(String device) throws IOException {
		assert(device.length() == 16);
		byte[] address = Utils.hexStringToByteArray(device);
		System.out.println(String.format("reading temperature for %s", device)); 
		
		sendCheckedReset(Speed.REGULAR);

		sendByte(0xE1); // data mode
		sendByteCheckedReply(0x55, 0x55); // match rom
		for (int i = 0; i < 8; i++) sendByteCheckedReply(address[i], address[i]); //send address
		sendByteCheckedReply(0xBE, 0xBE); // read scratchpad

		System.out.println("reading scratchpad bytes");
		byte[] scratch = new byte[9];
		for (int i = 0; i < scratch.length; i++) {
			sendByte(0xFF);
			scratch[i] = this.input.readByte();
		}
		
		// output values
		System.out.println("scatchpad " + Utils.byteArrayToHexString(scratch));
		System.out.println("crc " + Utils.byteArrayToHexString(scratch[8]));
		
		sendByte(0xE3); // cmd mode
		sendCheckedReset(Speed.REGULAR);
		
		int t = scratch[1] + (scratch[0] << 8);
		System.out.println(t);
		// System.exit(0);
		
		return ((double)t) / 1000.0;
		
	}

	private void sendCheckedReset(Speed speed) throws IOException {
		System.out.println("resetting...");
		ResetStatus status = sendReset(speed);
		System.out.println("Reset, state: " + status.toString());
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
