package org.cscs.interfaces;

import gnu.io.CommPortIdentifier;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

public class TemperatureTest {

	
	public static void main(String[] args) {
		
		CommDriverDS2480B driver = null;
    	try {
        	
        	Set<CommPortIdentifier> ports = CommDriverDS2480B.getAvailableSerialPorts();
        	for (CommPortIdentifier port:ports) {
        		System.out.println(port.getName());
        	}
            
        	driver = new CommDriverDS2480B(CommPortIdentifier.getPortIdentifier("/dev/ttyUSB0"));
        	
        	double temp = 0;
        	DateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
        	String fileName = "/home/chris/temps" + df.format(new Date()) + ".csv";
        	System.out.println("Writing to " + fileName);
			PrintWriter pw = new PrintWriter(fileName);
        	DateFormat precision = new SimpleDateFormat("yyyyMMdd HHmmss");
        	
        	while (true) {
	        	Collection<String> devices = driver.enumerateBusDevices();
	        	System.out.println(String.format("Found devices: %s", devices));

	        	for (String device: devices) {
	        		driver.DS18B20requestConversion(device);
	        		temp = driver.DS18B20readTemperature(device);
	        		System.out.println(String.format(" Temperature %8.4f deg-C", temp));
	        		pw.printf("%s,%s,%d,%8.4f\n", device, precision.format(new Date()), System.currentTimeMillis(), temp);	        		
	        	}
	        	
	        	pw.flush();
	        	Thread.sleep(1000);
	        	if (temp > 90.0) break;
        	}
        	
        	
        	System.out.println("finished");
        	
        } catch ( Exception e ) {
            e.printStackTrace();
        } finally {
        	driver.close();
        }
		
		
	}
	
}
