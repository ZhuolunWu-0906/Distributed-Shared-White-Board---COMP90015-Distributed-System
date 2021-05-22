package client;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public class Listener extends JPanel implements ActionListener,MouseListener,MouseMotionListener {
	
//	private Socket socket;
	private ClientThread ct;
	private DataInputStream input;
	private DataOutputStream output;
	
	private Board jp = null;
	private Graphics2D board = null;
	private Shape shape = new Shape("Pencil", new Color(0, 0, 0));
	private ArrayList<Shape> shapes = new ArrayList<Shape>();
	
	public Listener(Socket socket, DataInputStream input, DataOutputStream output) {
//		this.socket = socket;
		this.input = input;
		this.output = output;
	}
	
	public void setupBoard(Graphics2D board, Board jp) {
		this.board = board;
		this.board.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		this.jp = jp;
		this.ct = ClientThread.getCT(input, output);
		ct.setupBoard(this.board, this.jp);
		this.ct.connect();
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {}
	
	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
	
	@Override
	public void mousePressed(MouseEvent e) {
		shape.x1 = e.getX();
		shape.y1 = e.getY();
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if (shape.shapeName.equals("Pencil")) {
			shape.x2 = e.getX();
			shape.y2 = e.getY();
//			drawShape(this.board, shape);
			ct.sendMsg(writeMessage("shape"));
			shapes.add(shapeCopy());
			shape.x1 = shape.x2;
			shape.y1 = shape.y2;
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		if(!shape.shapeName.equals("Text")) {
			shape.x2 = e.getX();
			shape.y2 = e.getY();
			shape.calculate();
//			drawShape(this.board, shape);
			ct.sendMsg(writeMessage("shape"));
			shapes.add(shapeCopy());
		} else {
			shape.text = JOptionPane.showInputDialog(jp, "Please enter text:", "Text", 1);
			if (!(shape.text==null)) {
//				drawShape(this.board, shape);
				ct.sendMsg(writeMessage("shape"));
				shapes.add(shapeCopy());
			}
		}
		shape.x1 = shape.x2;
		shape.y1 = shape.y2;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		switch (e.getActionCommand()) {
		
//			Changing colors
			case "":
				JButton button = (JButton) e.getSource();
				shape.color = button.getBackground();
				board.setPaint(shape.color);
				break;
				
//			Clean current painting and create a new one
			case "New":
				clear();
				break;
				
//			Open a painting from file
			case "Open":
				
				break;
				
//			Save current painting
			case "Save":
				try {
					savePng();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
				
//			Save current painting as
			case "Save as":
				
				break;
				
//			Close the board
			case "Close":
				
				break;
				
//			Change paint shape
			default:
				shape.shapeName = e.getActionCommand();
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
	
//	Make a copy of current drawing shape
	public Shape shapeCopy() {
		switch (shape.shapeName) {
			case "Pencil":
				return new Shape(shape.shapeName, shape.color, shape.x1, shape.y1, shape.x2, shape.y2);
			case "Line":
				return new Shape(shape.shapeName, shape.color, shape.x1, shape.y1, shape.x2, shape.y2);
			case "Circle":
				return new Shape(shape.shapeName, shape.color, shape.x1, shape.y1, shape.x2, shape.y2);
			case "Oval":
				return new Shape(shape.shapeName, shape.color, shape.x1, shape.y1, shape.x2, shape.y2);
			case "Rect":
				return new Shape(shape.shapeName, shape.color, shape.x1, shape.y1, shape.x2, shape.y2);
			case "Text":
				return new Shape(shape.shapeName, shape.color, shape.text, shape.x1, shape.x2);
		}
		return null;
	}
	
	// Write JSON message for sending
	@SuppressWarnings("unchecked")
	public JSONObject writeMessage(String header) {
		
		JSONObject newMsg = new JSONObject();
		newMsg.put("header", header);
		
		switch (header) {
			case "shape":
				newMsg.put("shapeName", shape.shapeName);
				newMsg.put("color", shape.color.getRGB());
				newMsg.put("x1", shape.x1);
				newMsg.put("y1", shape.y1);
				if (! shape.shapeName.equals("Text")) {
					newMsg.put("text", shape.text);
					newMsg.put("x2", shape.x2);
					newMsg.put("y2", shape.y2);
				}
				break;
			case "":
				break;
		}
		
		return newMsg;
	}
	
	public void clear() {
		board.setPaint(Color.white);
		board.fillRect(0, 0, getSize().width, getSize().height);
		board.setPaint(Color.black);
		shapes.clear();
		jp.repaint();
	}
	
	public void savePng() throws IOException {
		BufferedImage bi = new BufferedImage(jp.getWidth(), jp.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D image = (Graphics2D) bi.getGraphics();
		image.fillRect(0, 0, jp.getWidth(), jp.getHeight());
		for (Shape i: shapes) {
			image.setPaint(i.color);
			drawShape(image, i);
		}
		ImageIO.write(bi, "PNG", new File("nihao.png"));
	}

}
