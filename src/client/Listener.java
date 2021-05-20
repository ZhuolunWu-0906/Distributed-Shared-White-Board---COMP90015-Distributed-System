package client;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class Listener extends JPanel implements ActionListener,MouseListener,MouseMotionListener {
	
//	private Board gui;
	private JPanel jp;
	private Shape shape = new Shape("Line", new Color(0, 0, 0));
	private Graphics2D board;
	private boolean isTextfinished = true;
	
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
		if (shape.shapeName.equals("Line")) {
			shape.x2 = e.getX();
			shape.y2 = e.getY();
			drawShape();
			repaint();
			shape.x1 = shape.x2;
			shape.y1 = shape.y2;
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		shape.x2 = e.getX();
		shape.y2 = e.getY();
		if(!shape.shapeName.equals("Text")) {
			shape.calculate();
			drawShape();
			repaint();
		} else {
			isTextfinished = false;
//			jp.removeAll();
//			jp.updateUI();
			JTextField tf = new JTextField();
			jp.add(tf);
			tf.setBounds(shape.x2, shape.y2, 60, 30);
			tf.addFocusListener(new FocusListener() {
				public void focusLost(FocusEvent e) {
					System.out.println("Lost");
					isTextfinished = true;
					shape.text = tf.getText();
					tf.setEnabled(false);
					jp.remove(tf);
//					jp.updateUI();
					drawShape();
					repaint();
				}
				@Override
				public void focusGained(FocusEvent e) {
					// Do nothing
					System.out.println("Gained");
				}
			});
		}
		shape.x1 = shape.x2;
		shape.y1 = shape.y2;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
//		if (isTextfinished == false) {
//			jp.removeAll();
//			jp.updateUI();
//		}
		
		if (e.getActionCommand().equals("")) {
			JButton button = (JButton) e.getSource();
			shape.color = button.getBackground();
			board.setPaint(shape.color);
		} else {
			shape.shapeName = e.getActionCommand();
		}
	}
	
	
	public void drawShape() {
		switch (shape.shapeName) {
			case "Line":
				board.drawLine(shape.x1, shape.y1, shape.x2, shape.y2);
				break;
			case "Circle":
				board.drawOval(shape.xMin, shape.yMin, Math.min(shape.w, shape.h), Math.min(shape.w, shape.h));
				break;
			case "Oval":
				board.drawOval(shape.xMin, shape.yMin, shape.w, shape.h);
				break;
			case "Rect":
				board.drawRect(shape.xMin, shape.yMin, shape.w, shape.h);
				break;
			case "Text":
				board.drawString(shape.text, shape.x2, shape.y2);
				break;
		}
	}
	
//	public void clear() {
//		board.setPaint(Color.white);
//		board.fillRect(0, 0, getSize().width, getSize().height);
//		board.setPaint(Color.black);
//		repaint();
//	}

}
