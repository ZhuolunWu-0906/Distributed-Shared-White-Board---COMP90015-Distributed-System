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
			
			String msg = null;
			JSONObject JMsg = null;
			
			try {
				if (input.available() > 0) {
					
					msg = input.readUTF();
					JMsg = parseJson(msg);

//					System.out.println(msg);
					JSONObject reply = new JSONObject();
					
					switch (JMsg.get("header").toString()) {
					
						// New client trying to join
						case "connect":
							name = JMsg.get("name").toString();
							reply.put("header", "connect");
							reply.put("name", name);
							if (Server.getCount() == 0) {
								// Manager
								reply.put("status", "success");
								reply.put("role", "manager");
								Server.addClient(name, this);
								Server.addCount();
								Server.manager = this;
								System.out.println("Manager " + name + " connected.");
							} else if (Server.nameExist(name)) {
								// Duplicate name exist
								reply.put("status", "failure");
								System.out.println("New client trys to connect, but name duplicated.");
								this.stopThread();
							} else {
								// Connected as normal user
								reply.put("status", "success");
								reply.put("role", "user");
								System.out.println("New user " + name + " connected, waiting for manager's approvement.");
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
							
						// Receive manager's reply whether the new client can join or not
						case "connect reply":
							
							String requestName = JMsg.get("name").toString();
							int index = requestListNames.indexOf(requestName);
							ServerThread requestST = requestList.get(index);
							
							if (JMsg.get("status").equals("approve")) {
								Server.addClient(requestName, requestST);
								Server.addCount();
								System.out.println("Manager " + name + " approved new client " + requestName + "\'s request.\nNew client " + name + " successfully connected.");
							} else {
								requestListNames.remove(index);
								requestList.remove(index);
								requestST.stopThread();
								System.out.println("Manager " + name + " declined new client " + requestName + "\'s request.");
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
							
						// Send all shapes to a newly joint client
						case "initialize":
							sendAllShapes();
							break;
							
						// New shape incoming; also works for case open whiteboard from file
						case "shape":
							Server.addShape(msg);
							synchronized(Server.socketThreadList) {
								for (ServerThread st : Server.socketThreadList) {
									try {
										st.output.writeUTF(msg);
										st.output.flush();
									} catch (IOException e) {
										disconnected.add(st);
									}
								}
							}
							break;
						
						// Chat
						case "chat":
							synchronized(Server.socketThreadList) {
								for (ServerThread st : Server.socketThreadList) {
									try {
										st.output.writeUTF(msg);
										st.output.flush();
									} catch (IOException e) {
										disconnected.add(st);
									}
								}
							}
							break;
							
						// Leave the whiteboard
						case "close":
							
							// Remove from Server's socket thread list
							Server.names.remove(this.name);
							Server.socketThreadList.remove(this);
							
							this.stopThread();
							
							// If the left client is manager
							if (this == Server.manager) {
								synchronized(Server.socketThreadList) {
									for (ServerThread st : Server.socketThreadList) {
										try {
											reply.put("header", "close");
											st.output.writeUTF(msg);
											st.output.flush();
											st.stopThread();
										} catch (IOException e) {
											disconnected.add(st);
										}
									}
								}
								System.exit(1);
							}
							break;
						
						// New whiteboard
						case "new":
							try {
								Thread.sleep(300);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
							Server.clearShape();
							synchronized(Server.socketThreadList) {
								for (ServerThread st : Server.socketThreadList) {
									try {
										reply.put("header", "new");
										st.output.writeUTF(msg);
										st.output.flush();
									} catch (IOException e) {
										disconnected.add(st);
									}
								}
							}
							break;
						
						case "kick":
							String userName = JMsg.get("name").toString();
							ServerThread st = Server.getServerThread(userName);
							st.output.writeUTF(msg);
							Server.remove(st);
						
						default:
							System.out.println("Received unexpected message from client " + name + ": "+ msg);
							
					}
					
					// Process disconnected clients
					if (disconnected.size() != 0) {
						Server.remove(disconnected);
						disconnected.clear();
					}
					
					// Send user list to each client
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
	private synchronized void sendAllUsers() {
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
