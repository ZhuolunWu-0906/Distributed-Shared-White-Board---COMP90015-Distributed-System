package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class ServerThread implements Runnable {
	
	private Socket socket;
	public String name;
	
	private boolean isStopped = false;
	
	DataInputStream input;
	DataOutputStream output;
	
	private ArrayList<ServerThread> disconnected = new ArrayList<ServerThread>();
	public ArrayList<ServerThread> requestList = new ArrayList<ServerThread>();
	public ArrayList<String> requestListNames = new ArrayList<String>();
	
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
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		
		while (!isStopped) {
			
//			try {
//				Thread.sleep(10);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			
			String msg = null;
			JSONObject JMsg = null;	
			
			
			try {
				if (input.available() > 0) {
					
					msg = input.readUTF();
					JMsg = parseJson(msg);
					
					switch (JMsg.get("header").toString()) {
					
						case "connect":
							JSONObject reply = new JSONObject();
							name = JMsg.get("name").toString();
							reply.put("header", "connect");
							reply.put("name", name);
							
							if (Server.count == 0) {
								reply.put("status", "success");
								reply.put("role", "manager");
								Server.manager = this;
								Server.names.add(name);
								Server.socketThreadList.add(this);
								Server.count++;
							} else if (Server.names.contains(name)) {
								reply.put("status", "failure");
								this.stopThread();
							} else {
								reply.put("status", "success");
								reply.put("role", "user");
								try {
									Server.manager.output.writeUTF(msg);
									Server.manager.output.flush();
								} catch (IOException e) {
									
								}
								Server.manager.requestList.add(this);
								Server.manager.requestListNames.add(name);
							}
							
							try {
								output.writeUTF(reply.toJSONString());
								output.flush();
							} catch (IOException e) {
								this.stopThread();
							}
							
							break;
							
						case "connect reply":
							
							String requestName = JMsg.get("name").toString();
							int index = requestListNames.indexOf(requestName);
							ServerThread requestST = requestList.get(index);
							
							if (JMsg.get("status").equals("approve")) {
								Server.socketThreadList.add(requestST);
								Server.names.add(requestName);
							} else {
								requestListNames.remove(index);
								requestList.remove(index);
								requestST.stopThread();
							}
							
							try {
								requestST.output.writeUTF(msg);
								requestST.output.flush();
							} catch (IOException e) {
								if (JMsg.get("status").equals("approve")) {
									requestListNames.remove(index);
									requestList.remove(index);
									requestST.stopThread();
								}
							}
							
							break;
							
						case "initialize":
							sendAllShapes();
							break;
							
						case "shape":
							Server.shapes.add(msg);
							for (ServerThread st : Server.socketThreadList) {
								try {
									st.output.writeUTF(msg);
									st.output.flush();
								} catch (IOException e) {
									disconnected.add(st);
								}
							}
							break;
							
						case "close":
							break;
							
						case "clear":
							break;
							
					}
					
					// Process disconnected clients
					if (disconnected.size() != 0) {
						for (ServerThread st : disconnected) {
							Server.names.remove(st.name);
							Server.socketThreadList.remove(st);
							st.stopThread();
						}
						disconnected.clear();
					}
					sendAllUsers();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
				
		}
		
	}
	
	
//	Stop the thread
	public void stopThread() {
		this.isStopped = true;
	}
	
	
//	Parse incoming message to JSONObject
	public JSONObject parseJson(String msg) {
		
		JSONObject JMsg = null;
		
		try {
			JMsg = (JSONObject) parser.parse(msg);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return JMsg;
		
	}
	
//	Send all stored shapes to a newly connected client
	private void sendAllShapes() {
		try {
			if (Server.shapes.size() > 0) {
				for (String shape : Server.shapes) {
					output.writeUTF(shape);
					output.flush();
				}
			}
		} catch(IOException e) {
			
		}
	}
	
	@SuppressWarnings("unchecked")
	private void sendAllUsers() {
		JSONObject newMsg = new JSONObject();
		String names = "";
		for (String name : Server.names) {
			names = name + "," + names;
		}
		newMsg.put("header", "users");
		newMsg.put("users", names);
		
		
		for (ServerThread st : Server.socketThreadList) {
			try {
				st.output.writeUTF(newMsg.toJSONString());
				st.output.flush();
			} catch (IOException e) {}
		}
	}

}
