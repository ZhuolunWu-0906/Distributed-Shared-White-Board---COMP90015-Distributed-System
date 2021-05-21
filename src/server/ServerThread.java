package server;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import client.Shape;

public class ServerThread implements Runnable {
	
	private boolean isStopped = false;
	private Socket socket;
	DataInputStream input;
	DataOutputStream output;
	private ArrayList<ServerThread> disconnected = new ArrayList<ServerThread>();
	
	private JSONParser parser = new JSONParser();
	
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
		
//		Send all saved shapes to new client
		try {
			if (Server.shapes.size() > 0) {
				for (String shape : Server.shapes) {
					output.writeUTF(shape);
				}
			}
		} catch(IOException e) {
			
		}
		
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
//					If the message is about drawing a new shape, add it to server's shapes ArrayList
					if (readMsg((JSONObject) parser.parse(msg))) {
						Server.shapes.add(msg);
					}
					
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
			} catch (IOException | ParseException e1) {
				e1.printStackTrace();
			}
			
		}
		
	}
	
	
//	Stop the thread
	public void stopThread() {
		this.isStopped = true;
	}
	
	
//	Read input message, return true if the message is about drawing a new shape
//	Otherwise process accordingly
	private boolean readMsg(JSONObject msg) {
		
		switch (msg.get("header").toString()) {
			case "shape":
				return true;
			default:
				return false;
		}
		
	}

}
