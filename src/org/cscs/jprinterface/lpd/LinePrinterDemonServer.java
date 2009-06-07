package org.cscs.jprinterface.lpd;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
/**
 * Implement a Line Printer Demon as specified in RFC 1179 and clarified by RFC 2569.
 * The demon must have one or more printQueues added to be useful, and will then manage collection
 * of jobs to those queues and serving the status of the queues. To actually spool/process Jobs
 * deposited by clients, provide a JobListener implementation. The default implementation stalls
 * Jobs, ie. leaves them on the Queue. 
 * 
 * @author shuckc
 *
 */
public class LinePrinterDemonServer implements Server {
	private static final Logger logger = Logger.getLogger(LinePrinterDemonServer.class.getName());

	private int port = 515;
	private ServerSocket serverSocket;
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private volatile boolean running = true;
	private HashMap<String, List<PrintJob>> printQueues = new HashMap<String, List<PrintJob>>();
	
	public void start() {
		
		try {
		    serverSocket = new ServerSocket(port);
		} catch (IOException e) {
		    System.out.println("Could not listen on port");
		    System.exit(-1);
		}
		logger.info(String.format("Listening on %d, printqueues %s", port, printQueues));
		
		while (running ) {
			
			
			Socket clientSocket = null;
			try {
			    clientSocket = serverSocket.accept();
				logger.info(String.format("Handling connection from %s", clientSocket.getRemoteSocketAddress()));
				RequestHandler handler = new RequestHandler(clientSocket, this);
				executor.execute(handler);
					    
			} catch (IOException e) {
			    System.out.println("Accept failed");
			    System.exit(-1);
			}
		}
		
	    try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void addPrinterQueue(String name) {
		printQueues.put(name, new LinkedList<PrintJob>());
	}
	
	
	public static void main(String[] args) {
		
		Server lpd = new LinePrinterDemonServer();
		lpd.addPrinterQueue("test");
		
		Map<String, byte[]> files = new HashMap<String,byte[]>();
		files.put("phonebook", new byte[500]);
		PrintJob pj = new PrintJob(new HashMap<String,byte[]>(), files, "chris", 990, 1, "desk01");
		lpd.getQueue("test").add(pj);
		lpd.start();
		
	}

	public List<PrintJob> getQueue(String name) {
		return printQueues.get(name);
	}
	
	
	
}
