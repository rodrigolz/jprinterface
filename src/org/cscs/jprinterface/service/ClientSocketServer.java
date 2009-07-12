package org.cscs.jprinterface.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cscs.jprinterface.queue.QueueManager;

public class ClientSocketServer {
	private static final Logger logger = Logger.getLogger(ClientSocketServer.class.getName());
	
	private final int port;
	private final ExecutorService listeningExecutor;
	private final ExecutorService clientExecutor;
	ServerSocket ss;
	
	public ClientSocketServer(int port, QueueManager queueManager, int simultanousClients) {
		this.port = port;
		//TODO: give the rejection policy of this queue some thought
		clientExecutor = Executors.newFixedThreadPool(simultanousClients);
		listeningExecutor = Executors.newSingleThreadExecutor();
	}

	public void start() {
		try {
			ss = new ServerSocket(port);
			
			logger.info(String.format("Listening on %d", port ));

		} catch (IOException io) {
			throw new RuntimeException("failed startup", io);			
		}
		/* submit a runnable that accepts client connections (submitting
		 * further runnable in the process TODO: use two executors 
		 */
		
		listeningExecutor.submit(new Runnable() {
			@Override
			public void run() {
				while (true ) {
					
					Socket clientSocket = null;
					try {
					    clientSocket = ss.accept();
						logger.info(String.format("Handling connection from %s", clientSocket.getRemoteSocketAddress()));
						ClientRequestHandler handler = new ClientRequestHandler(clientSocket);
						clientExecutor.execute(handler);
							    
					} catch (IOException e) {
						logger.log(Level.WARNING, "Error handling connection", e);
					}
				}				
			}
		});
		
		
	}

	public void shutdown() {
		clientExecutor.shutdownNow();
		listeningExecutor.shutdownNow();
	}

}
