/**
 * Zhuolun Wu, 954465
 *
 * 25/05/2021
 * description: Client GUI listener and Graphic2D drawing
 **/
package client;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("serial")
public class Listener extends JPanel implements ActionListener, MouseListener, MouseMotionListener, WindowListener {
	
	private ClientThread clientThread;
	
	private Board jp = null;
	private Shape shape = new Shape("Line", new Color(0, 0, 0));

	public Graphics2D board = null;
	protected ArrayList<Shape> shapes = new ArrayList<Shape>();
	
	private JSONParser parser = new JSONParser();
	
	public Listener() {
	}
	
	public void setupBoard(Graphics2D board, Board jp) {
		this.board = board;
		this.board.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		this.jp = jp;
		this.clientThread = ClientThread.getCT();
		clientThread.setupBoard(this, this.jp);
		this.clientThread.connect();
	}
	
	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}
	
	@Override
	public void mouseMoved(MouseEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {}
	
	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
	

	@Override
	public void windowClosing(WindowEvent e) {
		sendClose();
		System.exit(1);
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		shape.x1 = e.getX();
		shape.y1 = e.getY();
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		if(!shape.shapeName.equals("Text")) {
			shape.x2 = e.getX();
			shape.y2 = e.getY();
			shape.calculate();
			sendShape();
		} else {
			shape.text = JOptionPane.showInputDialog(jp, "Please enter text:", "Text", 1);
			if (!(shape.text==null)) {
				sendShape();
			}
		}
		shape.x1 = shape.x2;
		shape.y1 = shape.y2;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {

		switch (e.getActionCommand()) {
		
//			Changing colors
			case "":
				JButton button = (JButton) e.getSource();
				shape.color = button.getBackground();
				board.setPaint(shape.color);
				break;
			
//			Chats
			case "Send":
				String msg = jp.texting.getText();
				jp.texting.setText("");
				if (msg.length() != 0) {
					JSONObject newMsg = new JSONObject();
					newMsg.put("header", "chat");
					newMsg.put("name", jp.name);
					newMsg.put("msg", msg);
					clientThread.sendMsg(newMsg);
				}
				break;
				
//			Clean current painting and create a new one
			case "New":
				if (jp.isManager) {
					if (0 == JOptionPane.showConfirmDialog(jp,"If you create a new whiteboard, all changes would be gone. Please save before you creeate a new one.","Create new whiteboard",0)) {
						sendNew();
					}
				} else {
					JOptionPane.showMessageDialog(jp,"Sorry, you can not use this feature","No access ",0);
				}
				
				break;
				
//			Open a painting from file
			case "Open":
				if (jp.isManager) {
					JFileChooser chooser = new JFileChooser(".");
					chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
//					chooser.addChoosableFileFilter(new FileNameExtensionFilter("json"));
					int response = chooser.showOpenDialog(jp);
					
					if (response == JFileChooser.APPROVE_OPTION) {
						
						File file = chooser.getSelectedFile();
						
						try {
							@SuppressWarnings("resource")
							Scanner fileIn = new Scanner(file);
							
							if (file.isFile()) {
								JSONArray jsons = new JSONArray();
								
								while (fileIn.hasNextLine()) {
									String line = fileIn.nextLine();
									
									if (line.contains("shapeName")) {
										JSONObject newShape = parseJson(line);
										
										if (newShape == null ) {
											JOptionPane.showMessageDialog(jp,"Unsupported JSON format.","File format error",0); return;
										} else {
											jsons.add(newShape);
										}
									} else {
										JOptionPane.showMessageDialog(jp,"Unsupported JSON format.","File format error",0); return;
									}
								}
								
								sendNew();
								for (int i = 0; i < jsons.size(); i++) {
									clientThread.sendMsg((JSONObject) jsons.get(i));
								}
								
							} else {
								JOptionPane.showMessageDialog(jp,"Unsupported JSON format.","File format error",0);
							}
							
						} catch (FileNotFoundException e1) {
							JOptionPane.showMessageDialog(jp,"Failed to open File Scanner.","File Scanner error",0);
						}
						
//						while
					}
					
				} else {
					JOptionPane.showMessageDialog(jp,"Sorry, you can not use this feature","No access ",0);
				}
				break;
				
//			Save current painting
			case "Save":
				if (jp.isManager) {
					saveJson("new-painting.json");
				} else {
					JOptionPane.showMessageDialog(jp,"Sorry, you can not use this feature","No access ",0);
				}
				
				break;
				
//			Save current painting as
			case "Save as":
				if (jp.isManager) {
					JFileChooser chooser = new JFileChooser(".");
					chooser.setDialogType(JFileChooser.SAVE_DIALOG);
					int response = chooser.showSaveDialog(jp);
					if (response == JFileChooser.APPROVE_OPTION) {
						File file = chooser.getSelectedFile();
						String fileLocation = file.getAbsolutePath();
						if (fileLocation.endsWith("png")) {
							savePng(fileLocation);
						} else if (fileLocation.endsWith("json")) {
							saveJson(fileLocation);
						} else {
							JOptionPane.showMessageDialog(jp,"You can only save as JSON or PNG","No access ",0);
						}
					}
				} else {
					JOptionPane.showMessageDialog(jp,"Sorry, you can not use this feature","No access ",0);
				}
				break;
			
//			Kick user
			case "comboBoxChanged":
				
				JComboBox<String> cb = (JComboBox<String>) e.getSource();
			    String name = (String)cb.getSelectedItem();
			    if (name != null && !name.equals("--Select--")) {
			    	if (jp.isManager) {
			    		if (0 == JOptionPane.showConfirmDialog(jp,"Are you sure you want to kick " + name + "?","Kick user",0)) {
			    			kickUser(name);
			    		}
			    		cb.setSelectedIndex(0);
			    	} else {
			    		JOptionPane.showMessageDialog(jp,"Sorry, you can not use this feature","No access ",0);
			    		cb.setSelectedIndex(0);
			    	}
			    } 
				break;
				
//			Close the board
			case "Close":
				if (jp.isManager) {
					if (0 == JOptionPane.showConfirmDialog(jp,"If you close this whiteboard, all changes would be gone and the server would stop. Please save before you close.","Close whiteboard",0)) {
						sendClose();
						System.exit(1);
					}
				} else {
					JOptionPane.showMessageDialog(jp,"Sorry, you can not use this feature","No access ",0);
				}
				break;
				
//			Change paint shape
			default:
				shape.shapeName = e.getActionCommand();
				break;
		}
	}

	//	Draw the shape on board
	private void drawShape(Graphics2D target, Shape shape) {
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
	
	// Send new shapes
	@SuppressWarnings("unchecked")
	private void sendShape() {
		
		JSONObject newMsg = new JSONObject();
		newMsg.put("header", "shape");
		
		newMsg.put("shapeName", shape.shapeName);
		newMsg.put("color", shape.color.getRGB());
		newMsg.put("x1", shape.x1);
		newMsg.put("y1", shape.y1);
		if (shape.shapeName.equals("Text")) {
			newMsg.put("text", shape.text);
		} else {
			newMsg.put("x2", shape.x2);
			newMsg.put("y2", shape.y2);
		}
		
		clientThread.sendMsg(newMsg);
	}
	
	@SuppressWarnings("unchecked")
	private void sendClose() {
		JSONObject newMsg = new JSONObject();
		newMsg.put("header", "close");
		clientThread.sendMsg(newMsg);
	}
	
	@SuppressWarnings("unchecked")
	private void sendNew() {
		JSONObject newMsg = new JSONObject();
		newMsg.put("header", "new");
		clientThread.sendMsg(newMsg);
	}
	
	@SuppressWarnings({ "unchecked" })
	private void kickUser(String name) {
		JSONObject newMsg = new JSONObject();
		newMsg.put("header", "kick");
		newMsg.put("name", name);
		clientThread.sendMsg(newMsg);
	}
	
	public void clear() {
		board.setPaint(Color.white);
		board.fillRect(0, 0, getSize().width, getSize().height);
		board.setPaint(Color.black);
		shapes.clear();
		jp.repaint();
	}
	
	private void savePng(String fileLocation) {
		BufferedImage bi = new BufferedImage(jp.getWidth(), jp.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D image = (Graphics2D) bi.getGraphics();
		image.fillRect(0, 0, jp.getWidth(), jp.getHeight());
		for (int i = 0; i < shapes.size(); i++) {
			image.setPaint(shapes.get(i).color);
			drawShape(image, shapes.get(i));
		}
		try {
			ImageIO.write(bi, "PNG", new File(fileLocation));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void saveJson(String fileLocation) {
		try (FileWriter file = new FileWriter(fileLocation)) {
            for (Shape shape : shapes) {
            	JSONObject shapeJsonObject = new JSONObject();
            	shapeJsonObject.put("header", "shape");
        		
            	shapeJsonObject.put("shapeName", shape.shapeName);
            	shapeJsonObject.put("color", shape.color.getRGB());
            	shapeJsonObject.put("x1", shape.x1);
            	shapeJsonObject.put("y1", shape.y1);
        		if (shape.shapeName.equals("Text")) {
        			shapeJsonObject.put("text", shape.text);
        		} else {
        			shapeJsonObject.put("x2", shape.x2);
        			shapeJsonObject.put("y2", shape.y2);
        		}
        		file.write(shapeJsonObject.toJSONString() +"\n"); 
                file.flush();
            }
 
        } catch (IOException e) {
        	JOptionPane.showMessageDialog(jp,"Error when creating file.","File creating error",0);
        }
		
	}
	
//	Parse incoming message to JSONObject
	public JSONObject parseJson(String msg) {
		
		JSONObject JMsg = null;
		
		try {
			JMsg = (JSONObject) parser.parse(msg);
		} catch (ParseException e) {
			return null;
		}
		
		return JMsg;
		
	}

}
