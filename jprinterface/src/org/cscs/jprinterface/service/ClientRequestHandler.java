package org.cscs.jprinterface.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.cscs.jprinterface.lpd.PrintJob;
import org.cscs.jprinterface.queue.QueueListener;
import org.cscs.jprinterface.queue.QueueManager;

/** Handle a client connection. Once protocol negotiation completes,
 * we register a QueueListener on behalf of the client at the QueueManager
 * so that we can send events to the client. We then send an initial image
 * of jobs and queues.
 * 
 * Clients may perform actions on jobs. Not sure how this will work yet. 
 * 
 * @author chris
 */
public class ClientRequestHandler implements Runnable {

	private class ClientProtocolException extends Exception {
		public ClientProtocolException(String msg) {
			super(msg);
		}
		private static final long serialVersionUID = 1L; 
	}
	
	private static final int PROTOCOL_VERSION = 2;
	private final Socket cs;
	private final QueueManager manager;
	private final Lock protocolMessageLock = new ReentrantLock();
	
	private static final int PROTOCOL_SERVER_SEND_QUEUES = 3;
	private static final int PROTOCOL_SERVER_SEND_JOB = 4;
	
	
	public ClientRequestHandler(Socket clientSocket, QueueManager queueManager) {
		this.cs = clientSocket;
		this.manager = queueManager;
	}

	public void run() {
		QueueListener listener = null;
		try {
			final DataInputStream dis = new DataInputStream(cs.getInputStream());
			final DataOutputStream dos = new DataOutputStream(cs.getOutputStream());
			
			try {
			
				// protocol begins synchronously				
				// read an initial byte from the client, which should be 1
				int version = dis.read();
				if (version != PROTOCOL_VERSION) throw new ClientProtocolException("bad version");
				
				// send version back to client
				dos.write(PROTOCOL_VERSION);
				
				
				// register handler with QueueManager to start receiving changes
				// note callbacks here on another thread. They indicate we need to 
				// tell the client something
				listener = new QueueListener() {
					
					public void jobChange(String queue, PrintJob job,ChangeEvent event) {
						try {
							if (protocolMessageLock.tryLock(100, TimeUnit.MILLISECONDS)) {
								try {
									sendJob(dos,queue, job);
								} catch (IOException e) {
									e.printStackTrace();
								} finally {
									protocolMessageLock.unlock();
								}
							}						
						} catch (InterruptedException e) {
							e.printStackTrace();
						}					
					}					
					
					public void queueChange(String queue, ChangeEvent event) {
						try {
							if (protocolMessageLock.tryLock(100, TimeUnit.MILLISECONDS)) {
								try {
									sendQueues(dos,manager);
								} catch (IOException e) {
									e.printStackTrace();
								} finally {
									protocolMessageLock.unlock();
								}
							}						
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				};
				
				// Communication is about to become asynchronous on the 
				// socket, so aquire the transaction lock
				protocolMessageLock.lock();
				try {				
					manager.addListener(listener);
	
					// send initial image
					sendQueues(dos, manager);					
				} finally {
					protocolMessageLock.unlock();
				}
				
				while (true) {
					try {
						int command = dis.readUnsignedByte();
						
						throw new ClientProtocolException(String.format("unknown command %d", command));					
					} catch (java.io.EOFException eof) {
						// legitimate to disconnect here
						break;
					}
				}
				
		 	} catch (ClientProtocolException e) {
			 	// try and tell the client they are in error (if they are listening)
		 		// - socket closed in finally block
			 	e.printStackTrace();			
				dos.write(254);
				dos.writeUTF(e.getMessage());
		 	}	

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (cs != null) 
				try { cs.close(); } catch (IOException io) { };
			if (listener != null)
				manager.removeListener(listener);
		}
		
	}

	private void sendQueues(DataOutputStream dos, QueueManager queueManager) throws IOException {
		dos.write(PROTOCOL_SERVER_SEND_QUEUES);
		Set<String> queues = queueManager.getQueueNames();
		dos.write(queues.size());
		for (String queue: queues) dos.writeUTF(queue);
	}
	
	private void sendJob(DataOutputStream dos, String queue, PrintJob job) throws IOException {
		dos.write(PROTOCOL_SERVER_SEND_JOB);
		dos.writeUTF(queue);
		dos.writeLong(job.id);
		
	}

}
