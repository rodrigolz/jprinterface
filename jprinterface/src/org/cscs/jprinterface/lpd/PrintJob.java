package org.cscs.jprinterface.lpd;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** LPD representation of a PrintJob. The somewhat arbitrary collection of fields are those 
 * required to implement the methods defined by RFC2659.
 * @author shuckc
 *
 */
public class PrintJob implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Build an immutable job starting from only the ID
	 * @author shuckc
	 *
	 */
	public static class JobBuilder {
		public final Map<String, byte[]> control = new HashMap<String, byte[]>();
		public final Map<String, byte[]> data = new HashMap<String, byte[]>();	
		private final long id;
		private int copies;
		private String host;
		private String owner;
		public JobBuilder(long jobid) {
			this.id = jobid;
		}
		public JobBuilder addControlFile(String name, byte[] data) {
			this.control.put(name, data);
			
			System.out.println(new String(data));
			return this;
		}
		public JobBuilder addDataFile(String name, byte[] data) {
			this.data.put(name, data);
			return this;
		}
		public PrintJob build() {
			return new PrintJob(control, data, owner,id,copies,host);
		}
		public void setCopies(int copies) {
			this.copies = copies;
		}
		public void setOwner(String owner) {
			this.owner = owner;
		}
		public void setHost(String host) {
			this.host = host;
		}		
	}

	public final Map<String, byte[]> control;
	public final Map<String, byte[]> data;
	public final String owner;
	public final long id;
	public final int copies;
	public final String host;
	
	public PrintJob(Map<String, byte[]> control, Map<String, byte[]> data,
			String owner, long id, int copies, String host) {
		this.control = control;
		this.data = data;
		this.owner = owner;
		this.id = id;
		this.copies = copies;
		this.host = host;
	}
	
	
}
