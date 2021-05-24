package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("serial")
public class Board extends JPanel{
	

	private String ip;
	private int port;
	private Socket socket = null;
	
	protected DataInputStream input;
	protected DataOutputStream output;

	protected String name;
	protected ArrayList<String> userList = new ArrayList<String>();
	protected boolean isManager = false;
	protected boolean socketCreated = false;
	
	protected JTextArea users;
	protected JTextArea chatArea;
	protected JTextArea texting;
	protected JComboBox<String> kickUser;
	private Dimension dmColor = new Dimension(30,30), dmShape = new Dimension(70,30), dmFile = new Dimension(90, 40);
	
	public static void main(String[] args){
		Board board=new Board();
		board.initUI();
	}
	
	private void initUI() {
		
		connectionUI();
		
		while (!this.socketCreated) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};
		
		boolean approved = false;
		
		if (!isManager) {
			
			while (!approved) {
				String msg = null;
				JSONObject JMsg = null;
				try {
					if (input.available() > 0) {
						
						msg = input.readUTF();
						JMsg = parseJson(msg);
						if (JMsg.get("header").toString().equals("connect reply") && JMsg.get("status").toString().equals("approve")) {
							approved = true;
							break;
						} else {
							JOptionPane.showMessageDialog(null,"Sorry, you are not permitted to access the whiteboard.","Error",0);
						}
						
					}
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null,"Net error","Error",0);
					System.exit(1);
				}
			}
		}
		
		if (isManager)
		JOptionPane.showMessageDialog(null,"You are manager.","Notice", 1);
		
		if (approved || isManager) {
			String[] drawBtns = {"Line", "Circle", "Oval", "Rect", "Text"};
			String[] opeBtns = {"New", "Open", "Save", "Save as", "Close"};
			
//			black, white, gray, silver, maroon, red, purple, fushsia, green, lime, olive, yellow, navy, blue, teal, aqua
			Color[] colors = {new Color(0, 0, 0), new Color(255, 255, 255), new Color(128, 128, 128), new Color(192, 192, 192),
							new Color(128, 0, 0), new Color(255, 0, 0), new Color(128, 0, 128), new Color(255, 0, 255),
							new Color(0, 128, 0), new Color(0, 255, 0), new Color(128, 128, 0), new Color(255, 255, 0),
							new Color(0, 0, 128), new Color(0, 0, 255), new Color(0, 128, 128), new Color(0, 255, 255)};
			
			JFrame frame = new JFrame();
			frame.setTitle("Distributed Whiteboard:  " + (isManager ? "Manager ":"") + name);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLocationRelativeTo(null);
			if (isManager) {
				frame.setSize(1200,800);
			} else {
				frame.setSize(1100,800);
			}
			frame.setLayout(new BorderLayout());
			this.setBackground(Color.white);
			Listener listener = new Listener();
			
//			Drawing control panels
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
			
//			Chats and users panel
			JPanel chats = new JPanel();
			
			JLabel userListLabel = new JLabel("Connected users");
			JLabel chatWindowLabel = new JLabel("Chats");
			
			users = new JTextArea();
			users.setLineWrap(true);
			users.setWrapStyleWord(true);
			JScrollPane userList = new JScrollPane(users,javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			userList.setPreferredSize(new Dimension(245, 150));
			users.setEditable(true);
			
			chatArea = new JTextArea();
			chatArea.setLineWrap(true);
			chatArea.setWrapStyleWord(true);
			JScrollPane chatWindow = new JScrollPane(chatArea,javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			chatWindow.setPreferredSize(new Dimension(245, 400));
			chatArea.setEditable(true);
			
			texting = new JTextArea();
			texting.setPreferredSize(new Dimension(245, 70));
			
			JButton sendBtn = new JButton("Send");
			sendBtn.setPreferredSize(new Dimension(245, 30));
			sendBtn.addActionListener(listener);
			
			chats.add(userListLabel);
			chats.add(userList);
			chats.add(chatWindowLabel);
			chats.add(chatWindow);
			chats.add(texting);
			chats.add(sendBtn);
			chats.setPreferredSize(new Dimension(260, this.getHeight() - drawControls.getHeight() - 2));
			
//			File operation panel
			JPanel fileControls = new JPanel();
			
			for (int i = 0; i < opeBtns.length; i++) {
				JButton btn = new JButton(opeBtns[i]);
				btn.setPreferredSize(dmFile);
				btn.addActionListener(listener);
				fileControls.add(btn);
			}
			
			JLabel kick = new JLabel("Kick");
			kickUser = new JComboBox<String>();
			kickUser.addItem("--Select--");
			kickUser.setPreferredSize(dmFile);
			kickUser.addActionListener(listener);
			
			fileControls.add(kick);
			fileControls.add(kickUser);
			
			fileControls.setPreferredSize(new Dimension(100, this.getHeight() - drawControls.getHeight() - 2));
			
			
//			Finalizing
			frame.add(drawControls, BorderLayout.NORTH);
			frame.add(this, BorderLayout.CENTER);
			frame.add(chats, BorderLayout.EAST);
			if (isManager) {
				frame.add(fileControls, BorderLayout.WEST);
			}
			frame.addWindowListener(listener);
			frame.setVisible(true);
			frame.setResizable(false);
			this.addMouseListener(listener);
			this.addMouseMotionListener(listener);
			listener.setupBoard((Graphics2D) this.getGraphics(), this);
			
		} else {
			JOptionPane.showMessageDialog(null,"Sorry, manager refused your connection request","Error", 0);
		}

		
	}
	
	
	// UI for connection
	private void connectionUI() {
		
		
		JFrame frame = new JFrame();
		frame.setTitle("Connect to server");
		frame.setSize(300,210);
		frame.setLocationRelativeTo(null);
		frame.setLayout(new FlowLayout(FlowLayout.LEADING,20,10));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		
		// Labels
		JLabel ipLabel = new JLabel("IP");
		ipLabel.setPreferredSize(new Dimension(60,30));
		JLabel portLabel = new JLabel("Port");
		portLabel.setPreferredSize(new Dimension(60,30));
		JLabel nameLabel = new JLabel("Name");
		nameLabel.setPreferredSize(new Dimension(60,30));
		
		
		// Text fields
		JTextField ipField = new JTextField();
		ipField.setPreferredSize(new Dimension(160,30));
		JTextField portField = new JTextField();
		portField.setPreferredSize(new Dimension(160,30));
		JTextField nameField = new JTextField();
		nameField.setPreferredSize(new Dimension(160,30));
		
		// Button
		JButton connectBtn = new JButton("Connect");
		connectBtn.setPreferredSize(new Dimension(245, 30));
		
		// Button action listener
		connectBtn.addActionListener(new ActionListener() {
		
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
				
				// Get port
				if (!portField.getText().equals("")) {
					try {
						port = Integer.parseInt(portField.getText());
					} catch (NumberFormatException e1) {
						JOptionPane.showMessageDialog(frame,"Please enter a valid port number.","Error",0); return;
					}
				} else {
					JOptionPane.showMessageDialog(frame,"Please enter a valid port number.","Error",0); return;
				}
				
				// Get ip
				if (!ipField.getText().equals("")) {
					ip = ipField.getText();
				} else {
					JOptionPane.showMessageDialog(frame,"Please enter a valid IP address.","Error",0); return;
				}
				
				// Get name
				if (!nameField.getText().equals("")) {
					name = nameField.getText();
				} else {
					JOptionPane.showMessageDialog(frame,"Please enter a valid name.","Error",0); return;
				}
				
				// Try to create socket and connect to sercer
				try {
					try {
						socket = new Socket(ip, port);
					} catch (UnknownHostException e1) {
						JOptionPane.showMessageDialog(frame,"Invalid IP address, please check again.","Error",0); return;
					} catch (ConnectException e1) {
						JOptionPane.showMessageDialog(frame,"Invalid port number, please check again.","Error",0); return;
					}
					input = new DataInputStream(socket.getInputStream());
					output = new DataOutputStream(socket.getOutputStream());
					
					JSONObject tempMsg = new JSONObject();
					tempMsg.put("header", "connect");
					tempMsg.put("name", name);
					
					output.writeUTF(tempMsg.toJSONString());
					output.flush();
					String msg = input.readUTF();
					JSONObject JMsg = parseJson(msg);
					
					if (JMsg.get("header").toString().equals("connect") && JMsg.get("status").toString().equals("success")) {
						isManager = JMsg.get("role").toString().equals("manager") ? true : false;
						socketCreated = true;
						frame.dispose();
					} else {
						JOptionPane.showMessageDialog(frame,"Username existed. Please enter a new username","Error",0); return;
					}
					
				} catch (IOException e1) {
					
					e1.printStackTrace();
					
				}
				
            }
		});

		
		frame.add(ipLabel);
		frame.add(ipField);
		frame.add(portLabel);
		frame.add(portField);
		frame.add(nameLabel);
		frame.add(nameField);
		frame.add(connectBtn);
		frame.setVisible(true);
	}
	
//	Parse incoming message to JSONObject
	public JSONObject parseJson(String msg) {
		
		JSONObject JMsg = null;
		
		try {
			JMsg = (JSONObject) new JSONParser().parse(msg);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return JMsg;
		
	}
	
}
