package org.cscs.jprinterface.queue;

import org.cscs.jprinterface.lpd.PrintJob;

public interface QueueListener {

	public void newJob(PrintJob job);
	
	public void stateChange(PrintJob job, Object state);
	
}
