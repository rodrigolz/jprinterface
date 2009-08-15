package org.cscs.jprinterface.queue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.cscs.jprinterface.lpd.PrintJob;

/**
 * Listen for job change/additions and respond by asynchronously 
 * writing the job to disk. Jobs are stored under a directory
 * tree filed by jobid.
 * @author chris
 */
public class FilesystemPersistingListener implements QueueListener {
	private static final Logger logger = Logger.getLogger(FilesystemPersistingListener.class.getName());

	private final File directoryRoot;
	private final ExecutorService executor;
	
	public FilesystemPersistingListener(String outputDirectory) {	
		executor = Executors.newSingleThreadExecutor();
		directoryRoot = new File(outputDirectory);
		directoryRoot.mkdirs();		
		logger.info("Create writer: " + directoryRoot.getAbsolutePath());
	}
	
	private class PersistJob implements Runnable {
		final PrintJob job;
		
		public PersistJob(PrintJob job) {
			this.job = job;
		}
		public void run() {			
			File f = new File(String.format("%s/%d/rawjob.serialised", directoryRoot, job.id));
			f.getParentFile().mkdirs();
			logger.info("Wrote: " + f.getAbsolutePath());	
			ObjectOutputStream oos;
			try {
				oos = new ObjectOutputStream(new FileOutputStream(f));
				oos.writeObject(job);
				oos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
					
		}		
	}

	public void shutdown() {
		// allow pending writes to complete (holding back the shutdown handler)
		executor.shutdown();
	}

	@Override
	public void jobChange(String queue, PrintJob job, ChangeEvent event) {
		executor.submit(new PersistJob(job));		
	}

	@Override
	public void queueChange(String queue, ChangeEvent event) {
		
	}
	
}
