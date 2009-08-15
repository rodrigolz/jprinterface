package org.cscs.jprinterface.queue;


import org.cscs.jprinterface.lpd.PrintJob;

public interface QueueListener {

	enum ChangeEvent {
		ADD,
		REMOVE,
		CHANGED
	};
		
	public void queueChange(String queue, ChangeEvent event);
	
	public void jobChange(String queue, PrintJob job, ChangeEvent event);

	
}
