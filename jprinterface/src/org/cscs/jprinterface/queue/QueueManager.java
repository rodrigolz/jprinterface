package org.cscs.jprinterface.queue;

import java.util.List;
import java.util.Set;

import org.cscs.jprinterface.lpd.PrintJob;

public interface QueueManager {

	public long getNextJobId();
	public void addListener(QueueListener listener);
	public void removeListener(QueueListener listener);
	
	public void createQueue(String name);
	public Set<String> getQueueNames();	
	public List<PrintJob> getQueueContent(String name);
	
	public void addJob(String queueName, PrintJob job);
	
	
}