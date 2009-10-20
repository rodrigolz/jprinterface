package org.cscs.interfaces;
	import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashSet;
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
			this.port.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
			this.port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
						
			// flashRTS();			
			
			input = new DataInputStream(new DebugInputStream(this.port.getInputStream()));
			output = new DataOutputStream(new DebugOutputStream(this.port.getOutputStream()));
			
			// send initial reset byte
			output.write(Integer.parseInt("11000001", 2));
			output.flush();
			Thread.sleep(50);
			// int read = input.read();
			
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
    

    private long[] enumerateBusDevices() {
    	try {
    		System.out.println("enumerating bus devices");
			
    		byte[] id_tx_buf = new byte[16];
			byte[] id_rx_buf = new byte[16];
			boolean busy = true;
			int highestDiscrepancy = 0;
			
			while (busy) {
    			System.out.println(sendReset(Speed.REGULAR));
				System.out.println("switch data mode");
				output.write(0xE1);
				output.flush();
				System.out.println("search bus");
				output.write(0x0F);
				output.flush();
				output.write(0xE3);
				output.flush();
				output.write(0xB1);
				output.flush();
				output.write(0xE1);
				System.out.println("sending 16-byte search address");
				output.write(id_tx_buf);
				input.readFully(id_rx_buf);
				int t;
				busy = false;
				for (int d = 0; d < 64; d++) {
					int rd = (id_rx_buf[d/4] >> ((d%4)*2)) & 3;
					System.out.println(String.format(" bit %d  reads %d", d, rd));
					if (rd == 0) {
						// no descrepancy, chose 0
						
					} else if (rd == 1) { 
						// discrepancy, chose 1/0
						System.out.println(String.format("discrepancy at %d", d));
					} else if (rd == 2) {
						// no discrepancy, chose 1
					} else {
						System.out.println(String.format("search cancelled at %d, no device", d));
						// set bit r(d-1) to 1
						t = d - 1;
						id_tx_buf[t/4] = (byte) (id_tx_buf[t/4] | 0x03 << ((t%4)*2));
						busy = true;
						break;
					}
				}
				output.write(0xE3);  // command mode
				output.flush();
				output.write(0xA1);  // search accell off
				output.flush();
								
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

    private void close() {
		
    	this.port.close();
		
	}
	
    
    public static void main ( String[] args ) {
        try {
        	
        	Set<CommPortIdentifier> ports = getAvailableSerialPorts();
        	for (CommPortIdentifier port:ports) {
        		System.out.println(port.getName());
        	}
            
        	CommDriverDS2480B driver = new CommDriverDS2480B(CommPortIdentifier.getPortIdentifier("/dev/ttyUSB0"));
        	driver.enumerateBusDevices();
        	//driver.enumerateBusDevices();
        	//driver.enumerateBusDevices();
        	//driver.enumerateBusDevices();
        	//driver.enumerateBusDevices();
        	//driver.enumerateBusDevices();

        	
        	driver.close();
        	System.out.println("finished");
        	
        } catch ( Exception e ) {
            e.printStackTrace();
        }
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
