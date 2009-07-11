package org.cscs.jprinterface.lpd;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultQueueManager implements QueueManager {
	public HashMap<String, List<PrintJob>> printQueues;
	public AtomicInteger jobIdSeed;

	public DefaultQueueManager() {
		this.printQueues = new HashMap<String, List<PrintJob>>();
		
	}
	
	@Override
	public Set<String> getQueues() {
		return printQueues.keySet();
	}
	
	public void addPrinterQueue(String name) {
		printQueues.put(name, new LinkedList<PrintJob>());
	}
	
	public List<PrintJob> getQueue(String name) {
		return printQueues.get(name);
	}
	
	@Override
	public int getNextJobId() {
		return jobIdSeed.incrementAndGet();
	}	
}