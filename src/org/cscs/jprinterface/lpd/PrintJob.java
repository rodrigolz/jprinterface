package org.cscs.jprinterface.lpd;

import java.util.Map;



/** LPD representation of a PrintJob. The somewhat arbitrary collection of fields are those 
 * required to implement the methods defined by RFC2659.
 * @author shuckc
 *
 */
public class PrintJob {
	
	Map<String, byte[]> control;
	Map<String, byte[]> data;
	String owner;
	int id;
	int copies = 1;
	

}
