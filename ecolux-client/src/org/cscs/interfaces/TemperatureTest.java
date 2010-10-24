package org.cscs.interfaces;

import gnu.io.CommPortIdentifier;

import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class TemperatureTest {

	
	public static void main(String[] args) {
		
		CommDriverDS2480B driver = null;
    	try {
        	
        	Set<CommPortIdentifier> ports = CommDriverDS2480B.getAvailableSerialPorts();
        	for (CommPortIdentifier port:ports) {
        		System.out.println(port.getName());
        	}
            String port = "/dev/ttyUSB0";
            //String port = "COM17";
        	driver = new CommDriverDS2480B(CommPortIdentifier.getPortIdentifier(port));
        	
        	double temp = 0;
        	DateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
        	String fileName = "/home/chris/temps" + df.format(new Date()) + ".csv";
        	System.out.println("Writing to " + fileName);
        	new File(fileName).getParentFile().mkdirs();
			PrintWriter pw = new PrintWriter(fileName);
        	DateFormat precision = new SimpleDateFormat("yyyyMMdd HHmmss");
        	
        	while (true) {
	        	List<String> devices = new ArrayList<String>(driver.enumerateBusDevices());
	        	System.out.println(String.format("Found %d devices: %s", devices.size(), devices));
	        	Collections.shuffle(devices);
	        	driver.DS18B20requestConversion(null);
	        	for (String device: devices) {
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
