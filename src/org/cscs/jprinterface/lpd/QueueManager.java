package org.cscs.jprinterface.lpd;

import java.util.List;
import java.util.Set;

public interface QueueManager {

	public void addPrinterQueue(String name);
	
	public List<PrintJob> getQueue(String name);

	public int getNextJobId();

	public Set<String> getQueues();
	
}