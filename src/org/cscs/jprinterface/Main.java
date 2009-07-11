package org.cscs.jprinterface;

import java.util.HashMap;
import java.util.Map;

import org.cscs.jprinterface.lpd.DefaultQueueManager;
import org.cscs.jprinterface.lpd.LinePrinterDemonServer;
import org.cscs.jprinterface.lpd.PrintJob;
import org.cscs.jprinterface.lpd.QueueManager;
import org.cscs.jprinterface.lpd.Server;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		QueueManager queue = new DefaultQueueManager();
		queue.addPrinterQueue("test");
		
		// add a dummy job to the queue
		Map<String, byte[]> files = new HashMap<String,byte[]>();
		files.put("phonebook", new byte[500]);
		PrintJob pj = new PrintJob(new HashMap<String,byte[]>(), files, "chris", 990, 1, "desk01");
		queue.getQueue("test").add(pj);
		
		
		Server lpd = new LinePrinterDemonServer();
		lpd.setQueueManager(queue);
		
		lpd.start();
		
		
	}

}
