package client;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JPanel;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ClientThread {

	private JPanel jp;
	private Graphics2D board;
	private Shape shape;
	
	private Socket socket;
	private DataInputStream input;
	private DataOutputStream output;
	
	private JSONParser parser = new JSONParser();
	private JSONObject newMsg;
	
//	Singleton creation
	private ClientThread() {}
	private static final ClientThread ct = new ClientThread();
	public static ClientThread getCT(Socket socket, DataInputStream input, DataOutputStream output) {
		ct.socket = socket;
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
			boolean isStopped = false;
			public void run() {
				
				while (!isStopped) {
					
//					try {
//						Thread.sleep(10);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
					
					String msg = null;
					
					try {
						if (input.available() > 0) {
							newMsg = (JSONObject) parser.parse(input.readUTF());
							readMsg(newMsg);
							drawShape(board, shape);
						}
					} catch (IOException | ParseException e1) {
						e1.printStackTrace();
					}
					
				}
			}
		}.start();
	}
	
	
//	Send message manually (from listener)
	public void sendMsg(JSONObject newMsg) {
		try {
			output.writeUTF(newMsg.toJSONString());
			output.flush();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	
	private void readMsg(JSONObject msg) {
		
		switch (msg.get("header").toString()) {
			case "shape":
				String shapeName = msg.get("shapeName").toString();
				Color color = new Color(Integer.parseInt(msg.get("color").toString()));
				int x1 = Integer.parseInt(msg.get("x1").toString());
				int y1 = Integer.parseInt(msg.get("y1").toString());
				if (shapeName == "Text") {
					String text = msg.get("text").toString();
					shape = new Shape(shapeName, color, text, x1, y1);
				} else {
					int x2 = Integer.parseInt(msg.get("x2").toString());
					int y2 = Integer.parseInt(msg.get("y2").toString());
					shape = new Shape(shapeName, color, x1, y1, x2, y2);
				}
				break;
			case "":
				break;
		}
		
	}

//	Draw the shape on board
	public void drawShape(Graphics2D target, Shape shape) {
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
	
}
