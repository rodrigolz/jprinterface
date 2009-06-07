package org.cscs.jprinterface.lpd;

import java.util.HashMap;
import java.util.Map;



/** LPD representation of a PrintJob. The somewhat arbitrary collection of fields are those 
 * required to implement the methods defined by RFC2659.
 * @author shuckc
 *
 */
public class PrintJob {
	
	public static class JobBuilder {
		public final Map<String, byte[]> control = new HashMap<String, byte[]>();
		public final Map<String, byte[]> data = new HashMap<String, byte[]>();	
		private int id;
		private int copies;
		private String host;
		private String owner;
		public JobBuilder() {
		
		}
		public JobBuilder addControlFile(String name, byte[] data) {
			this.control.put(name, data);
			return this;
		}
		public JobBuilder addDataFile(String name, byte[] data) {
			this.data.put(name, data);
			return this;
		}
		public PrintJob build() {
			return new PrintJob(control, data, owner,id,copies,host);
		}
	
	}

	public final Map<String, byte[]> control;
	public final Map<String, byte[]> data;
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
