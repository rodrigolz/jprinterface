package org.cscs.jprinterface.lpd;

import org.cscs.jprinterface.queue.QueueManager;


public interface Server {

	public void start();

	public void setQueueManager(QueueManager queue);


	
}