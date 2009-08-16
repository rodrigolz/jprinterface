package org.cscs.jprinterface.web;

import java.util.logging.Logger;

import org.cscs.jprinterface.queue.QueueManager;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Webserver {

	private static final Logger logger = Logger.getLogger(Webserver.class.getName());

	Server server;
	QueueManager manager;
	
	public Webserver(QueueManager qm) {
		
	}	
	
	public void start() {
		
		server = new Server();
	    SelectChannelConnector connector = new SelectChannelConnector();
	    connector.setPort(8080);
	    server.addConnector(connector);

	    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addServlet(new ServletHolder(new HelloServlet()), "/data");
        	    
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{ "index.html" }); 
        resource_handler.setResourceBase("htdocs");
 
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, context, new DefaultHandler() });
        server.setHandler(handlers);
        try {
        	server.start();
        } catch (Exception e) {
        	throw new RuntimeException("Startup failed", e);
        }
        
		logger.info(String.format("Webserver listening on %d", connector.getPort()));

    }
	
	public void shutdown() {
		try {
			if (server != null) server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
