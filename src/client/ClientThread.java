package client;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ClientThread {

	private JPanel jp;
	private Graphics2D board;
	private Shape shape;
	
//	private Socket socket;
	private DataInputStream input;
	private DataOutputStream output;
	
	private JSONParser parser = new JSONParser();
	
//	Singleton creation
	private ClientThread() {}
	private static final ClientThread ct = new ClientThread();
	public static ClientThread getCT(DataInputStream input, DataOutputStream output) {
//		ct.socket = socket;
		ct.input = input;
		ct.output = output;
		return ct;
	}
	
	public void setupBoard(Graphics2D board, JPanel jp) {
		this.board = board;
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
					JMsg = new JSONObject();
					JMsg.put("header", "initialize");
					output.writeUTF(JMsg.toJSONString());
				} catch (IOException e) {
					
				}
				
				while (true) {
					msg = null;
					JMsg = null;
//					try {
//						Thread.sleep(10);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
					
					try {
						if (input.available() > 0) {
							
							msg = input.readUTF();
							JMsg = parseJson(msg);
							
							switch (JMsg.get("header").toString()) {
								case "shape":
									String shapeName = JMsg.get("shapeName").toString();
									Color color = new Color(Integer.parseInt(JMsg.get("color").toString()));
									int x1 = Integer.parseInt(JMsg.get("x1").toString());
									int y1 = Integer.parseInt(JMsg.get("y1").toString());
									if (shapeName == "Text") {
										String text = JMsg.get("text").toString();
										shape = new Shape(shapeName, color, text, x1, y1);
									} else {
										int x2 = Integer.parseInt(JMsg.get("x2").toString());
										int y2 = Integer.parseInt(JMsg.get("y2").toString());
										shape = new Shape(shapeName, color, x1, y1, x2, y2);
									}
									drawShape(board, shape);
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
							}
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
				}
			}
		}.start();
	}
	
	
//	Send message manually (from listener)
	public void sendMsg(JSONObject toSend) {
		try {
			output.writeUTF(toSend.toJSONString());
			output.flush();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}

//	Draw the shape on board
	public void drawShape(Graphics2D target, Shape shape) {
		board.setPaint(shape.color);
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
			e.printStackTrace();
		}
		
		return JMsg;
		
	}
	
}
