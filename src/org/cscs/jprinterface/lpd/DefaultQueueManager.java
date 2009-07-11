package org.cscs.jprinterface.lpd;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultQueueManager implements QueueManager {
	
	public final HashMap<String, List<PrintJob>> printQueues;
	public final AtomicLong jobIdSeed;

	public DefaultQueueManager() {
		this.printQueues = new HashMap<String, List<PrintJob>>();
		
		// token attempt to set the jobIds to something unique
		Calendar c = Calendar.getInstance();
		long datecode = c.get(Calendar.YEAR) * 10000 +
						(c.get(Calendar.MONTH)+1) * 100 +
						c.get(Calendar.DAY_OF_MONTH);
		
		jobIdSeed = new AtomicLong(datecode * 100);
		
		System.out.println(String.format("Starting queuemaneger with seed %d", jobIdSeed.get()));		
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
	public long getNextJobId() {
		return jobIdSeed.incrementAndGet();
	}	
}