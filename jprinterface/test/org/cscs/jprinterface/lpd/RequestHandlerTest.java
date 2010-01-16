package org.cscs.jprinterface.lpd;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class RequestHandlerTest extends TestCase {
	public void testRander() {
		
		PrintWriter pw = new PrintWriter(System.out);
		RequestHandler.renderQueueStatus(pw, "queue", new ArrayList<PrintJob>() );
		pw.flush();
		
		Map<String, byte[]> files = new HashMap<String,byte[]>();
		files.put("phonebook", new byte[500]);
		PrintJob pj = new PrintJob(new HashMap<String,byte[]>(), files, "chris", 990, 1, "desk01");
		List<PrintJob> jobs = Arrays.asList(pj);
		
		RequestHandler.renderQueueStatus(pw, "queue", jobs );
		pw.flush();
		
		RequestHandler.renderQueueVerboseStatus(pw, "queue", jobs );
		pw.flush();
		
		files.put("phonebook2", new byte[100]);
		jobs = Arrays.asList(pj, pj);
		
		RequestHandler.renderQueueStatus(pw, "queue", jobs );
		pw.flush();
		
		RequestHandler.renderQueueVerboseStatus(pw, "queue", jobs );
		pw.flush();		
	}
}
