package org.cscs.jprinterface.service;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientRequestHandler implements Runnable {

	private final Socket cs;
	
	public ClientRequestHandler(Socket clientSocket) {
		this.cs = clientSocket;
	}

	@Override
	public void run() {
		DataInputStream dis;
		try {
			dis = new DataInputStream(cs.getInputStream());
			dis.read();
			
			cs.close();

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
