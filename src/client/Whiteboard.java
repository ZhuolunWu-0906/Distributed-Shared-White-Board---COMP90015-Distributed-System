package client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
 
import javax.swing.JComponent;

// COLORS
// black	(0, 0, 0)
// white	(255, 255, 255)
// gray		(128, 128, 128)
// silver	(192, 192, 192)
// maroon	(128, 0, 0)
// red		(255, 0, 0)
// purple	(128, 0, 128)
// fushsia	(255, 0, 255)
// green	(0, 128, 0)
// lime		(0, 255, 0)
// olive	(128, 128, 0)
// yellow	(255, 255, 0)
// navy		(0, 0, 128)
// blue		(0, 0, 255)
// teal		(0, 128, 128)
// aqua		(0, 255, 255)

@SuppressWarnings("serial")
public class Whiteboard extends JComponent {
	
	private Image image;
	private Graphics2D board;
	private String shapeName = "line";
	private Color color = new Color(0, 0, 0);
//	private String text;
	private Shape shape = new Shape(shapeName, color);
	
	public Whiteboard() {
		setDoubleBuffered(false);
		
		addMouseListener(new MouseAdapter() {
			
			public void mousePressed(MouseEvent e) {
				shape.x1 = e.getX();
				shape.y1 = e.getY();
			};
			
//			public void mouseReleased(MouseEvent e) {
//				
//				x2 = e.getX();
//				y2 = e.getY();
//				
//				if (board != null) {
//					board.drawLine(x1, y1, x2, y2);
//					repaint();
//					x1 = x2;
//					y1 = y2;
//				}
//			}
			
		});
		
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				switch (shapeName) {
					case "line":
						shape.x2 = e.getX();
						shape.y2 = e.getY();
						
						if (board != null) {
							drawShape();
							repaint();
							shape.x1 = shape.x2;
							shape.y1 = shape.y2;
						}
					
				}
				
			}
		});
	}

	protected void paintComponent(Graphics g) {
		if (image == null) {
			image = createImage(getSize().width, getSize().height);
			board = (Graphics2D) image.getGraphics();
			board.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			clear();
		}
		
		g.drawImage(image, 0, 0, null);
	}
	
	public void clear() {
		board.setPaint(Color.white);
		board.fillRect(0, 0, getSize().width, getSize().height);
		board.setPaint(Color.black);
		repaint();
	}

	public void drawShape() {
		switch (shape.shapeName) {
			case "line":
				board.drawLine(shape.x1, shape.y1, shape.x2, shape.y2);
				break;
			case "circle":
				board.drawOval(shape.x1, shape.y1, Math.min(shape.w, shape.h), Math.min(shape.w, shape.h));
				break;
			case "oval":
				board.drawOval(shape.xMin, shape.yMin, shape.w, shape.h);
				break;
			case "Rectangle":
				board.drawRect(shape.xMin, shape.yMin, shape.w, shape.h);
				break;
			case "Text":
				board.drawString(shape.text, shape.x1, shape.y1);
				break;
		}
	}
	
	public void changeColor(Color changeTo) {
		color = changeTo;
		shape.color = changeTo;
		board.setPaint(changeTo);
	}
	
//	public void drawShape() {
//		board.setPaint(color);
//		switch (shapeName) {
//			case "line":
//				board.drawLine(x1, y1, x2, y2);
//				break;
//			case "circle":
//				board.drawOval(x1, y1, Math.min(Math.abs(x1 - x2), Math.abs(y1 - y2)), Math.min(Math.abs(x1 - x2), Math.abs(y1 - y2)));
//				break;
//			case "oval":
//				board.drawOval(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
//				break;
//			case "Rectangle":
//				board.drawRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
//				break;
//			case "Text":
//				board.drawString(text, x1, y1);
//				break;
//		}
//	}
	
	public Shape getShape() {
		return shape;
	}
 
}