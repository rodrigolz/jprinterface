package org.cscs.jprinterface.lpd;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.cscs.jprinterface.queue.QueueManager;
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
	
	private QueueManager queue;

	@Override
	public void setQueueManager(QueueManager queue) {
		this.queue = queue;		
	}
	
	public void start() {
		
		try {
		    serverSocket = new ServerSocket(port);
		} catch (IOException e) {
		    System.out.println("Could not listen on port");
		    System.exit(-1);
		}
		logger.info(String.format("Listening on %d, printqueues %s", port, queue.getQueueNames()));
		
		while (running ) {
			
			
			Socket clientSocket = null;
			try {
			    clientSocket = serverSocket.accept();
				logger.info(String.format("Handling connection from %s", clientSocket.getRemoteSocketAddress()));
				RequestHandler handler = new RequestHandler(clientSocket, queue);
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
	
}
