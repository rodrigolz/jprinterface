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
	public final String owner;
	public final int id;
	public final int copies;
	public final String host;
	
	public PrintJob(Map<String, byte[]> control, Map<String, byte[]> data,
			String owner, int id, int copies, String host) {
		this.control = control;
		this.data = data;
		this.owner = owner;
		this.id = id;
		this.copies = copies;
		this.host = host;
	}
	
	
}
