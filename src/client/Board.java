package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class Board extends JPanel{
	
	Dimension dmColor = new Dimension(30,30), dmShape = new Dimension(70,30), dmFile = new Dimension(90, 40);
	
	public static void main(String[] args){
		Board board=new Board(); 
		board.initUI();
	}
	
	public void initUI() {

		String[] drawBtns = {"Pencil", "Line", "Circle", "Oval", "Rect", "Text"};
		String[] opeBtns = {"New", "Open", "Save", "Save as", "Close", "Leave"};
		
//		black, white, gray, silver, maroon, red, purple, fushsia, green, lime, olive, yellow, navy, blue, teal, aqua
		Color[] colors = {new Color(0, 0, 0), new Color(255, 255, 255), new Color(128, 128, 128), new Color(192, 192, 192),
						new Color(128, 0, 0), new Color(255, 0, 0), new Color(128, 0, 128), new Color(255, 0, 255),
						new Color(0, 128, 0), new Color(0, 255, 0), new Color(128, 128, 0), new Color(255, 255, 0),
						new Color(0, 0, 128), new Color(0, 0, 255), new Color(0, 128, 128), new Color(0, 255, 255)};
		
		JFrame frame = new JFrame();
		frame.setTitle("Distributed Whiteboard");
		frame.setDefaultCloseOperation(3);
		frame.setLocationRelativeTo(null);
		frame.setSize(1300,800);
		frame.setLayout(new BorderLayout());
		this.setBackground(Color.white);
		
//		Listener listener = new Listener(this);
		Listener listener = new Listener();
		
//		Drawing control panels
		JPanel drawControls = new JPanel();
		for (int i = 0; i < drawBtns.length; i++) {
			JButton btn = new JButton(drawBtns[i]);
			btn.setPreferredSize(dmShape);
			btn.addActionListener(listener);
			drawControls.add(btn);
		}
		
		for (int i = 0; i < colors.length; i++) {
			JButton btn = new JButton();
			btn.setBackground(colors[i]);
			btn.setPreferredSize(dmColor);
			btn.addActionListener(listener);
			drawControls.add(btn);
		}
		
//		Chats and users panel
		JPanel chats = new JPanel();
		
		JLabel userListLabel = new JLabel("Connected users");
		JLabel chatWindowLabel = new JLabel("Chats");
		
		JTextField userList = new JTextField();
		userList.setEditable(false);
		userList.setPreferredSize(new Dimension(245, 150));
		
		JTextArea chatArea = new JTextArea();
		chatArea.setLineWrap(true);
		chatArea.setWrapStyleWord(true);
		JScrollPane chatWindow = new JScrollPane(chatArea,javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		chatWindow.setPreferredSize(new Dimension(245, 400));
		
		JTextArea texting = new JTextArea();
		texting.setPreferredSize(new Dimension(245, 70));
		
		JButton sendBtn = new JButton("Send");
		sendBtn.setPreferredSize(new Dimension(245, 30));
		
		chats.add(userListLabel);
		chats.add(userList);
		chats.add(chatWindowLabel);
		chats.add(chatWindow);
		chats.add(texting);
		chats.add(sendBtn);
		chats.setPreferredSize(new Dimension(260, this.getHeight() - drawControls.getHeight() - 2));
		
//		File operation panel
		JPanel fileControls = new JPanel();
		
		for (int i = 0; i < opeBtns.length; i++) {
			JButton btn = new JButton(opeBtns[i]);
			btn.setPreferredSize(dmFile);
			btn.addActionListener(listener);
			fileControls.add(btn);
		}
		
		fileControls.setPreferredSize(new Dimension(100, this.getHeight() - drawControls.getHeight() - 2));
		
		
//		Finalizing
		frame.add(drawControls, BorderLayout.NORTH);
		frame.add(this, BorderLayout.CENTER);
		frame.add(chats, BorderLayout.EAST);
		frame.add(fileControls, BorderLayout.WEST);
		frame.setVisible(true);
		frame.setResizable(false);
		this.addMouseListener(listener);
		this.addMouseMotionListener(listener);
		listener.setBoard((Graphics2D) this.getGraphics());
		listener.setJp(this);
	}
}
