package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Board extends JPanel{
	
	Dimension dmColor = new Dimension(30,30), dmShape = new Dimension(70,30);
	
	public static void main(String[] args){
		Board board=new Board(); 
		board.initUI();
	}
	
	public void initUI() {

		String[] btns = {"Line", "Circle", "Oval", "Rect", "Text"};
		
//		black, white, gray, silver, maroon, red, purple, fushsia, green, lime, olive, yellow, navy, blue, teal, aqua
		Color[] colors = {new Color(0, 0, 0), new Color(255, 255, 255), new Color(128, 128, 128), new Color(192, 192, 192),
						new Color(128, 0, 0), new Color(255, 0, 0), new Color(128, 0, 128), new Color(255, 0, 255),
						new Color(0, 128, 0), new Color(0, 255, 0), new Color(128, 128, 0), new Color(255, 255, 0),
						new Color(0, 0, 128), new Color(0, 0, 255), new Color(0, 128, 128), new Color(0, 255, 255)};
		
		JFrame frame = new JFrame();
		frame.setTitle("Distributed Whiteboard");
		frame.setDefaultCloseOperation(3);
		frame.setLocationRelativeTo(null);
		frame.setSize(1200,600);
		frame.setLayout(new BorderLayout());
		this.setBackground(Color.white);
		
//		Listener listener = new Listener(this);
		Listener listener = new Listener();
		
		JPanel controls = new JPanel();
		
		for (int i = 0; i < btns.length; i++) {
			JButton btn = new JButton(btns[i]);
			btn.setPreferredSize(dmShape);
			btn.addActionListener(listener);
			controls.add(btn);
		}
		
		for (int i = 0; i < colors.length; i++) {
			JButton btn = new JButton();
			btn.setBackground(colors[i]);
			btn.setPreferredSize(dmColor);
			btn.addActionListener(listener);
			controls.add(btn);
		}
		
//		JButton btn = new JButton("Clear");
//		btn.setPreferredSize(dmShape);
//		btn.addActionListener(listener);
//		controls.add(btn);
		
		frame.add(controls, BorderLayout.NORTH);
		frame.add(this, BorderLayout.CENTER);
		frame.setVisible(true);
		this.addMouseListener(listener);
		this.addMouseMotionListener(listener);

		listener.setBoard((Graphics2D) this.getGraphics());
		listener.setJp(this);
	}
}
