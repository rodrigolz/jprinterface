package org.cscs.jprinterface;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.cscs.jprinterface.lpd.LinePrinterDemonServer;
import org.cscs.jprinterface.lpd.PrintJob;
import org.cscs.jprinterface.lpd.Server;
import org.cscs.jprinterface.queue.DefaultQueueManager;
import org.cscs.jprinterface.queue.FilesystemPersistingListener;
import org.cscs.jprinterface.queue.QueueManager;
import org.cscs.jprinterface.service.ClientSocketServer;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		CountDownLatch shutdownLatch = new CountDownLatch(1); 
		
		//TODO: construct with Spring
		
		QueueManager queueManager = new DefaultQueueManager();
		queueManager.createQueue("test");
		
		// add a dummy job to the queue
		Map<String, byte[]> files = new HashMap<String,byte[]>();
		files.put("phonebook", new byte[500]);
		PrintJob pj = new PrintJob(new HashMap<String,byte[]>(), files, "chris", 990, 1, "desk01");
		queueManager.addJob("test", pj);
		
		ClientSocketServer service = new ClientSocketServer(8081, queueManager, 5);
		service.start();
		
		
		
		FilesystemPersistingListener writer = new FilesystemPersistingListener("/opt/jprinterface-read-only/jobs");
		queueManager.addListener(writer);
		
		Server lpd = new LinePrinterDemonServer();
		lpd.setQueueManager(queueManager);
		
		lpd.start();
		try {
			System.out.println("main thread awaiting shutdown latch");
			shutdownLatch.await();
		} catch (InterruptedException ie) {
			// probably intentional Ctl-C, swallow the Exception and proceed to shutdown
			
		}
		System.out.println("main thread doing ordered shutdown");

		lpd.shutdown();
		service.shutdown();
		
		
		
	}

}
