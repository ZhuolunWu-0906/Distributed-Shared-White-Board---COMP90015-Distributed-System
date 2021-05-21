package client;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Listener extends JPanel implements ActionListener,MouseListener,MouseMotionListener {
	
//	private Board gui;
	private JPanel jp;
	private Graphics2D board;
	private Shape shape = new Shape("Pencil", new Color(0, 0, 0));
	
//	public Listener(Board gui) {
//		this.gui = gui;
//	}
	
	public void setBoard(Graphics2D board) {
		this.board = board;
		this.board.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}
	
	public void setJp(JPanel j) {
		this.jp = j;
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
			drawShape();
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
			drawShape();
		} else {
			shape.text = JOptionPane.showInputDialog(jp, "Please enter text:", "Text", 1);
			if (!(shape.text==null)) drawShape();
		}
		shape.x1 = shape.x2;
		shape.y1 = shape.y2;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals("")) {
			JButton button = (JButton) e.getSource();
			shape.color = button.getBackground();
			board.setPaint(shape.color);
		} else if (e.getActionCommand().equals("Clear")){
			board.setPaint(Color.white);
			board.fillRect(0, 0, getSize().width, getSize().height);
			board.setPaint(Color.black);
			jp.repaint();
		} else {
			shape.shapeName = e.getActionCommand();
		}
	}
	
	
	public void drawShape() {
		switch (shape.shapeName) {
			case "Pencil":
				board.drawLine(shape.x1, shape.y1, shape.x2, shape.y2);
				break;
			case "Line":
				board.drawLine(shape.x1, shape.y1, shape.x2, shape.y2);
				break;
			case "Circle":
				board.drawOval(shape.xMin, shape.yMin, Math.max(shape.w, shape.h), Math.max(shape.w, shape.h));
				break;
			case "Oval":
				board.drawOval(shape.xMin, shape.yMin, shape.w, shape.h);
				break;
			case "Rect":
				board.drawRect(shape.xMin, shape.yMin, shape.w, shape.h);
				break;
			case "Text":
				board.drawString(shape.text, shape.x1, shape.y1+4);
				break;
		}
	}
	
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

}
