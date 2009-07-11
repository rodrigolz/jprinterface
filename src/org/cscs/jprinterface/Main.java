package org.cscs.jprinterface;

import java.util.HashMap;
import java.util.Map;

import org.cscs.jprinterface.lpd.LinePrinterDemonServer;
import org.cscs.jprinterface.lpd.PrintJob;
import org.cscs.jprinterface.lpd.Server;
import org.cscs.jprinterface.queue.DefaultQueueManager;
import org.cscs.jprinterface.queue.FilesystemPersistingListener;
import org.cscs.jprinterface.queue.QueueManager;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		QueueManager queueManager = new DefaultQueueManager();
		queueManager.createQueue("test");
		
		// add a dummy job to the queue
		Map<String, byte[]> files = new HashMap<String,byte[]>();
		files.put("phonebook", new byte[500]);
		PrintJob pj = new PrintJob(new HashMap<String,byte[]>(), files, "chris", 990, 1, "desk01");
		queueManager.addJob("test", pj);
				
		FilesystemPersistingListener writer = new FilesystemPersistingListener("/opt/jprinterface-read-only/jobs");
		queueManager.addListener(writer);
		
		Server lpd = new LinePrinterDemonServer();
		lpd.setQueueManager(queueManager);
		
		lpd.start();
		
		
	}

}
