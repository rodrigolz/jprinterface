package org.cscs.interfaces;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class EcoLuxDriver {
	
    final private static int TIMEOUT_OPEN_MILLIS = 2000;
    
	final private SerialPort port;
	
	final private DataInputStream input;
	final private DataOutputStream output;
	private AtomicBoolean debug = new AtomicBoolean(true);
	
	public EcoLuxDriver(CommPortIdentifier identifier) throws PortInUseException {
    
		System.out.println(String.format("Opening %s", identifier.getName()));
		
    	if (identifier.isCurrentlyOwned())
    		throw new IllegalStateException("Comm port owned by someone else");
    	
		CommPort unknownPort = identifier.open(this.getClass().getName(), TIMEOUT_OPEN_MILLIS);
			
		if (!(unknownPort instanceof SerialPort)) 
			throw new IllegalStateException("not a serial port");
			
		this.port = (SerialPort) unknownPort;
		try {
			this.port.setSerialPortParams(115200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
			this.port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
						
			AtomicBoolean debugIn = new AtomicBoolean(false);
			// hookup debugging streams();			
			input = new DataInputStream(new DebugInputStream(debugIn , this.port.getInputStream()));
			output = new DataOutputStream(new DebugOutputStream(debug, this.port.getOutputStream()));

			// sendByteCheckedReply(0x01, 0x02);
			
		} catch (UnsupportedCommOperationException e) {
			throw new IllegalStateException("port did not accept params", e);
		} catch (IOException e) {
			throw new IllegalStateException("port did not accept params", e);
		}
	}



    private void close() {
		
    	this.port.close();
		
	}
	
    
    public static void main ( String[] args ) {
        System.setProperty("gnu.io.rxtx.SerialPorts" ,"/dev/ttyACM0:/dev/ttyUSB0");
    	EcoLuxDriver driver = null;
    	try {
        	
        	
        	driver = new EcoLuxDriver(CommPortIdentifier.getPortIdentifier("/dev/ttyUSB0"));
        	
        	driver.sayHello();
        	        	
        	System.out.println("finished");
        	
        } catch ( Exception e ) {
            e.printStackTrace();
        } finally {
        	driver.close();
        }
    }
    
	private void sayHello() throws IOException {
		System.out.println("waiting for data...\n");
		while (true) {
			String s  = readLinefeedTerminatedString();
			System.out.println(s);
			
		}	
		
	}


	private CharBuffer cb = CharBuffer.allocate(1024);
	private String readLinefeedTerminatedString() throws IOException {
		cb.clear();
		int b;
		while ((b = this.input.read()) > 0) {
			if (b == 10 || b == 13) break;
			cb.put((char) b);			
		}
		if ( b == -1) throw new IOException("end of stream");
		cb.flip();
		return cb.toString();		
	}

//	private void readByteArrayFully(byte[] address, int dummy) throws IOException {
//		for (int x = 0; x < address.length; x++) {
//			sendByte(dummy);
//			address[x] = this.input.readByte();
//		}
//		System.out.println("Read buffer " + Utils.byteArrayToHexString(address));
//	}

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

	private void sendByte(int b) throws IOException {
		this.output.write(b); //command mode
		this.output.flush();
	}

}
