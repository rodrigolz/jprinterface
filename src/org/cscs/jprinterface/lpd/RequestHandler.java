package org.cscs.jprinterface.lpd;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {
	private static final Logger logger = Logger.getLogger(RequestHandler.class.getName());

	public enum Command {
		noop(0), print(1), recvJob(2), queueStatus(3), queueStatusVerbose(4), removeJob(5);

		private byte code;
		Command(int code) {
			this.setCode((byte)code);
		}
		public void setCode(byte code) {
			this.code = code;
		}
		public byte getCode() {
			return code;
		}
	}

	private final Socket clientSocket;
	private final QueueManager queue;
	
	public RequestHandler(Socket clientSocket, QueueManager queue) {
		this.clientSocket = clientSocket;
		this.queue = queue;
	}

	public void run() {
		
		int mode;
		try {
		
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			DataInputStream in = new DataInputStream( new BufferedInputStream(clientSocket.getInputStream()));
			
			mode = in.readUnsignedByte();
			Command c  = Command.values()[mode];
			logger.info(String.format("Client request: %s", c));
			String queueName;
			switch (c) {
			case print:
				// print-waiting-jobs = %x01 printer-name LF
				queueName = readQueue(in);
				break;
			case recvJob:
			    // receive-job = %x02 printer-name LF
				queueName = readQueue(in);
				logger.info(String.format("   recv queue: %s", queueName));
				// now read a sub-command, one of
			    //  abort-job = %x1 LF
				//  receive-control-file = %x2 number-of-bytes SP name-of-control-file LF
				//  receive-data-file = %x03 number-of-bytes SP name-of-data-file LF
				int jobid = queue.getNextJobId();
				PrintJob.JobBuilder jobBuilder = new PrintJob.JobBuilder(jobid);
				out.append((char) 0x00);
				out.flush();
				
				while (true) {
					int submode;
					try {
						submode = in.readUnsignedByte();
					} catch (java.io.EOFException eof) {
						// legitimate to disconnect here
						break;
					}
					logger.info(String.format("recieve job submode %d", submode));
					switch (submode) {
					case 1: // cancelled
						break;
					case 2:
						// receive-control-file = %x2 number-of-bytes SP name-of-control-file LF
	
					case 3:
						// receive-data-file = %x03 number-of-bytes SP name-of-data-file LF
						StringBuilder bufByteCount = new StringBuilder();
						StringBuilder bufFileName = new StringBuilder();
						StringBuilder bufActive = bufByteCount;
						int buf;
						while (0x0A != (buf = in.readUnsignedByte())) {
							if (buf == 0x20) {
								bufActive = bufFileName;
							} else {
								bufActive.append((char)buf);
							}
						}
						int byteCount = Integer.parseInt(bufByteCount.toString());
						String fileName = bufFileName.toString();
						logger.info(String.format("    recieve file '%s' bytes %d", fileName, byteCount));
						
						out.append((char) 0x00);
						out.flush();
						
						byte[] buffer = new byte[byteCount];
						in.readFully(buffer);
						
						logger.info(String.format("    recieve completed - file '%s' bytes %d", fileName, byteCount));

						// then expect a zero and reply with a zero
						int check = in.readUnsignedByte();
						if (check != 0) throw new InputMismatchException("expected zero byte after file");
						
						out.append((char) 0x00);
						out.flush();
						
						if (submode == 2 ) jobBuilder.addControlFile(fileName, buffer);
						if (submode == 3 ) jobBuilder.addDataFile(fileName, buffer);
						
						break;
					
					}
				}
				
				logger.info(String.format(" Job recieve completed, putting on queue"));

				PrintJob jb = jobBuilder.build();
				queue.getQueue(queueName).add(jb);
				break;
			case queueStatus:
			case queueStatusVerbose:
				// send-queue-short = %x03 printer-name *(SP(user-name / job-number)) LF
				// send-queue-long  = %x04 printer-name *(SP(user-name / job-number)) LF

				StringBuilder queueName2 = new StringBuilder();
				StringBuilder filter = new StringBuilder();
				StringBuilder reading = queueName2;
				int buf;
				while (0x0A != (buf = in.read())) {
					if (buf == 0x20) {
						reading = filter;
					} else {
						reading.append((char)buf);
					}
				}
				queueName = queueName2.toString();
				List<PrintJob> jobs = queue.getQueue(queueName);
				if (c == Command.queueStatus) {
					renderQueueStatus(out, queueName, jobs);			
				} else {
					renderQueueVerboseStatus(out, queueName, jobs);
				}	
			}
			
			out.close();
			in.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// close clientSocket
			try {
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		logger.info(String.format("request handler completed"));

		

	}

	public static void renderQueueVerboseStatus(PrintWriter out, String queue, List<PrintJob> jobs) {
		/*
		 * For an printer with no jobs the response is:
		 *      no entries
		 *
		 *   For a printer with jobs, an example of the response is:
		 *      killtree is ready and printing
		 * 
		 *      fred: active                        [job 123 tiger]
		 *              2 copies of stuff           602 bytes
		 *
		 *      smith: 1st                          [job 124 snail]
		 *              2 copies of resume          7088 bytes
		 *              2 copies of foo             10200 bytes
		 *
		 *      fred: 2nd                           [job 125 tiger]
		 *              more                        99 bytes
		 *
		 *      The column numbers of above headings and job entries are:
		 *      |       |                           |
		 *      01      09                          41
		 * 
		 */
		
		if (jobs == null || jobs.size() == 0) {
			out.println("no entries");
			return;
		}
	
		out.println(String.format("%s is ready and printing\n", queue));
		// out.println(String.format("%7s%11s%13s%18s%12s\n", "Rank", "Owner", "Job","Files","Total Size"));
		int rank = 0;
		for (PrintJob job: jobs) {
			out.println(String.format("%-41s[job %d %s]", 
					String.format("%s: %s", job.owner, rank == 0 ? "active" : Integer.toString(rank)), 
					job.id,
					job.host
				));
			for (Map.Entry<String,byte[]> entry: job.data.entrySet()) {
				out.println(String.format("%9s%-32s%d bytes",
						"",
						String.format("%d copies of %s", job.copies, entry.getKey()),
						entry.getValue().length
					));
			}
			out.println("");
		}
		
	}

	public static void renderQueueStatus(PrintWriter out, String queue, List<PrintJob> jobs) {
		/*
		 *  For an printer with no jobs, the response starts in column 1 and is:
		      no entries
		
		   For a printer with jobs, an example of the response is:
		     killtree is ready and printing
		     Rank   Owner      Job          Files             Total Size
		     active fred       123          stuff             1204 bytes
		     1st    smith      124          resume, foo       34576 bytes
		     2nd    fred       125          more              99 bytes
		     3rd    mary       126          mydoc             378 bytes
		     4th    jones      127          statistics.ps     4567 bytes
		     5th    fred       128          data.txt          9 bytes
		
		   The column numbers of above headings and job entries are:
		     |      |          |            |                 |
		     01     08         19           35                63
		 */

			if (jobs == null || jobs.size() == 0) {
				out.println("no entries");
				return;
			}
		
			out.println(String.format("%s is ready and printing", queue));
			out.println(String.format("%-7s%-11s%-13s%-18s%-12s", "Rank", "Owner", "Job","Files","Total Size"));
			int rank = 0;
			for (PrintJob job: jobs) {
				int bytecount = 0;
				StringBuilder sb = new StringBuilder();
				for (Map.Entry<String,byte[]> entry: job.data.entrySet()) {
					sb.append( bytecount == 0 ? "" : ", ").append(entry.getKey());
					bytecount += entry.getValue().length;
				}
				sb.setLength(17);
				out.println(String.format("%-7s%-11s%-13d%-17s %d bytes", 
						rank == 0 ? "active" : Integer.toString(rank), 
								job.owner, 
								job.id,
								sb.toString(),
								bytecount * job.copies
								// String.format("%d bytes", bytecount * job.copies)
				));
			}

	}

	
	
	private String readQueue(InputStream in) throws IOException {
		StringBuilder queueName = new StringBuilder();
		int buf;
		while (0x0A != (buf = in.read())) {
			queueName.append((char)buf);
		}		
		return queueName.toString();
	}

}
