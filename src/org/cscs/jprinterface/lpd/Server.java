package org.cscs.jprinterface.lpd;

import java.util.List;

public interface Server {

	public void start();

	public void addPrinterQueue(String name);
	
	public List<PrintJob> getQueue(String name);

}