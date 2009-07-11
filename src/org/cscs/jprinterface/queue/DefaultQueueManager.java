package org.cscs.jprinterface.queue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.cscs.jprinterface.lpd.PrintJob;

public class DefaultQueueManager implements QueueManager {
	
	public final HashMap<String, List<PrintJob>> printQueues;
	public final AtomicLong jobIdSeed;
	public final List<QueueListener> listeners;

	public DefaultQueueManager() {
		this.printQueues = new HashMap<String, List<PrintJob>>();
		
		this.listeners = new CopyOnWriteArrayList<QueueListener>();
		
		// token attempt to set the jobIds to something unique
		Calendar c = Calendar.getInstance();
		long datecode = c.get(Calendar.YEAR) * 10000 +
						(c.get(Calendar.MONTH)+1) * 100 +
						c.get(Calendar.DAY_OF_MONTH);
		
		jobIdSeed = new AtomicLong(datecode * 100);
		
		System.out.println(String.format("Starting queuemaneger with seed %d", jobIdSeed.get()));		
	}
	
	@Override
	public Set<String> getQueueNames() {
		return printQueues.keySet();
	}
	
	public void createQueue(String name) {
		printQueues.put(name, new LinkedList<PrintJob>());
	}
	
	public List<PrintJob> getQueueContent(String name) {
		return new ArrayList<PrintJob>(printQueues.get(name));
	}
	
	public void addJob(String queueName, PrintJob job) {
		List<PrintJob> queue = printQueues.get(queueName);
		queue.add(job);
		
		fireNewJob(queueName, job);
		
	}
	
	void fireNewJob(String queueName, PrintJob job) {
		for (QueueListener l : listeners) 
			l.newJob(job);
	}
	
	@Override
	public long getNextJobId() {
		return jobIdSeed.incrementAndGet();
	}

	@Override
	public void addListener(QueueListener listener) {
		listeners.add(listener);
	}	
}