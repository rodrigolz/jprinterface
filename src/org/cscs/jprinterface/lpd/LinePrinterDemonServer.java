package org.cscs.jprinterface.lpd;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
		
		while (running ) {
			
			
			Socket clientSocket = null;
			try {
			    clientSocket = serverSocket.accept();
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
		lpd.start();
		
	}

	public List<PrintJob> getQueue(String name) {
		return printQueues.get(name);
	}
	
	
	
}
