package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread implements Runnable {
	
	private boolean isStopped = false;
	private Socket socket;
	DataInputStream input;
	DataOutputStream output;
	private ArrayList<ServerThread> disconnected = new ArrayList<ServerThread>();
	
	public ServerThread(Socket socket) {
		
		this.socket = socket;
		
		try {
			this.input = new DataInputStream(this.socket.getInputStream());
			this.output = new DataOutputStream(this.socket.getOutputStream());
		} catch (IOException e) {
//			System.exit(1);
			e.printStackTrace();
		}
		
		Server.socketThreadList.add(this);
		
	}
	
	@Override
	public void run() {
		
		while (!isStopped) {
			
//			try {
//				Thread.sleep(10);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			
			// Read message
			String msg = null;
			
			try {
				if (input.available() > 0) {
					
					msg = input.readUTF();
					
					for (ServerThread st : Server.socketThreadList) {
						try {
							st.output.writeUTF(msg);
							st.output.flush();
						} catch (IOException e1) {
							disconnected.add(st);
						}
					}
					
					// Process disconnected clients
					if (disconnected.size() != 0) {
						for (ServerThread st : disconnected) {
							Server.socketThreadList.remove(st);
							st.stopThread();
						}
						disconnected.clear();
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
		
	}
	
	public void stopThread() {
		this.isStopped = true;
	}

}
