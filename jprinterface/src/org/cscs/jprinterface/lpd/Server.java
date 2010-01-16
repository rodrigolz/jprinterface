package org.cscs.jprinterface.lpd;

import org.cscs.jprinterface.queue.QueueManager;

/** A server requires a QueueManager to which it will submit jobs. 
 * Lifecycle control methods provided
 * 
 * @author chris
 */
public interface Server {

	public void start();

	public void setQueueManager(QueueManager queue);

	public void shutdown();


	
}