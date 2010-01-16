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
import org.cscs.jprinterface.web.Webserver;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Server lpd = null;
		ClientSocketServer service = null;
		FilesystemPersistingListener writer = null;
		Webserver web = null;
		try {
			
			CountDownLatch shutdownLatch = new CountDownLatch(1); 
			
			//TODO: construct with Spring
			
			QueueManager queueManager = new DefaultQueueManager();
			queueManager.createQueue("test");
			
			// add a dummy job to the queue
			Map<String, byte[]> files = new HashMap<String,byte[]>();
			files.put("phonebook", new byte[500]);
			PrintJob pj = new PrintJob(new HashMap<String,byte[]>(), files, "chris", 990, 1, "desk01");
			queueManager.addJob("test", pj);
			
			service = new ClientSocketServer(8081, queueManager);
			service.start();
			
			writer = new FilesystemPersistingListener("/opt/jprinterface-read-only/jobs");
			queueManager.addListener(writer);
			
			lpd = new LinePrinterDemonServer();
			lpd.setQueueManager(queueManager);			
			lpd.start();
			
			web = new Webserver(queueManager);
			web.start();
			
			try {
				System.out.println("startup completed, main thread awaiting shutdown latch");
				shutdownLatch.await();
			} catch (InterruptedException ie) {
				// probably intentional Ctl-C, swallow the Exception and proceed to shutdown
				
			}
			System.out.println("main thread doing ordered shutdown");

		} finally {
			if (lpd != null) lpd.shutdown();
			if (service != null) service.shutdown();
			if (writer != null) writer.shutdown();
			if (web != null) web.shutdown();
			System.out.println("main thread exited finally block");

		}
		
		
		
		
		
	}

}
