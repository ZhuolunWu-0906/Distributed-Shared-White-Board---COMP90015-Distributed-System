package client;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ClientThread {

	private Board jp;
	private Listener listener;
	private Shape shape;
	
	private boolean isStopped = false;
	
	private JSONParser parser = new JSONParser();
	
//	Singleton creation
	private ClientThread() {}
	private static final ClientThread ct = new ClientThread();
	public static ClientThread getCT() {
		return ct;
	}
	
	public void setupBoard(Listener board, Board jp) {
		this.listener = board;
		this.jp = jp;
	}
	
	
//	Create socket thread
	public void connect() {
		
		new Thread() {
			
			@SuppressWarnings("unchecked")
			public void run() {
				
				String msg = null;
				JSONObject JMsg = null;
				
				try {
					Thread.sleep(200);
					JMsg = new JSONObject();
					JMsg.put("header", "initialize");
					jp.output.writeUTF(JMsg.toJSONString());
				} catch (IOException | InterruptedException e) {
					
				}
				
				while (!isStopped) {
					msg = null;
					JMsg = null;
					
					try {
						if (jp.input.available() > 0) {
							
							msg = jp.input.readUTF();
							JMsg = parseJson(msg);
							
//							System.out.println(msg);
							
							switch (JMsg.get("header").toString()) {
								case "shape":
									String shapeName = JMsg.get("shapeName").toString();
									Color color = new Color(Integer.parseInt(JMsg.get("color").toString()));
									int x1 = Integer.parseInt(JMsg.get("x1").toString());
									int y1 = Integer.parseInt(JMsg.get("y1").toString());
									if (shapeName.equals("Text")) {
										String text = JMsg.get("text").toString();
										shape = new Shape(shapeName, color, text, x1, y1);
									} else {
										int x2 = Integer.parseInt(JMsg.get("x2").toString());
										int y2 = Integer.parseInt(JMsg.get("y2").toString());
										shape = new Shape(shapeName, color, x1, y1, x2, y2);
									}
									drawShape(listener.board, shape);
									listener.shapes.add(shape);
									break;
									
								case "connect":
									JMsg.put("header", "connect reply");
									if (JOptionPane.showConfirmDialog(jp, JMsg.get("name")+" wants to join your whiteboard", "New user", 0) == 0) {
										JMsg.put("status", "approve");
									} else {
										JMsg.put("status", "decline");
									}
									sendMsg(JMsg);
									break;
									
								case "users":
									String[] names = JMsg.get("users").toString().split(",");
									String allNames = "";
									
									jp.userList.clear();
									
									jp.kickUser.removeAllItems();
									jp.kickUser.addItem("--Select--");
									for (String name : names) {
										jp.userList.add(name);
										if (!name.equals(jp.name)) {
											jp.kickUser.addItem(name);
										}
										allNames = name + "\n" + allNames;
									}
									jp.users.setText(allNames);
									
									break;
								
								case "chat":
									String name = JMsg.get("name").toString();
									String chatMsg = JMsg.get("msg").toString();
									jp.chatArea.append(name + ": " + chatMsg + "\n");
									break;
									
								case "new":
									listener.clear();
									listener.shapes.clear();
									break;
								
								case "close":
									JOptionPane.showMessageDialog(jp,"Whiteboard is closed by manager","Whiteboard closed",0);
									System.exit(1);
									
								case "kick":
									JOptionPane.showMessageDialog(jp,"Sorry, you are kicked by the manager","Kicked",0);
									System.exit(1);
								
								default:
									System.out.println("Received unexpected message from server: "+ msg);
									
							}
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
				}
			}
		}.start();
	}
	
//	Stop the thread
	public void stopThread() {
		this.isStopped = true;
	}
	
	
//	Send message manually (from listener)
	public void sendMsg(JSONObject toSend) {
		try {
			jp.output.writeUTF(toSend.toJSONString());
			jp.output.flush();
		} catch (IOException e2) {
			JOptionPane.showMessageDialog(jp,"Server Closed","Server closed",1);
		}
	}

//	Draw the shape on board
	public void drawShape(Graphics2D target, Shape shape) {
		listener.board.setPaint(shape.color);
		switch (shape.shapeName) {
			case "Pencil":
				target.drawLine(shape.x1, shape.y1, shape.x2, shape.y2);
				break;
			case "Line":
				target.drawLine(shape.x1, shape.y1, shape.x2, shape.y2);
				break;
			case "Circle":
				target.drawOval(shape.xMin, shape.yMin, Math.max(shape.w, shape.h), Math.max(shape.w, shape.h));
				break;
			case "Oval":
				target.drawOval(shape.xMin, shape.yMin, shape.w, shape.h);
				break;
			case "Rect":
				target.drawRect(shape.xMin, shape.yMin, shape.w, shape.h);
				break;
			case "Text":
				target.drawString(shape.text, shape.x1, shape.y1+4);
				break;
		}
	}
	
//	Parse incoming message to JSONObject
	public JSONObject parseJson(String msg) {
		
		JSONObject JMsg = null;
		
		try {
			JMsg = (JSONObject) parser.parse(msg);
		} catch (ParseException e) {
//			e.printStackTrace();
		}
		
		return JMsg;
		
	}
	
}
