package client;

import java.awt.Graphics2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JPanel;

import org.json.simple.JSONObject;

public class ClientThread {

	private JPanel jp;
	private Graphics2D board;
	
	private Socket socket;
	private DataInputStream input;
	private DataOutputStream output;
	
	
//	Singleton creation
	private ClientThread() {}
	private static final ClientThread ct = new ClientThread();
	public static ClientThread getCT(Socket socket) {
		ct.socket = socket;
		return ct;
	}
	
	public void setJP(JPanel jp) {
		this.jp = jp;
	}
	
	public void setBoard(Graphics2D board) {
		this.board = board;
	}
	
	
//	Create socket thread
	public void connect() {
		
		try {
			this.input = new DataInputStream(socket.getInputStream());
			this.output = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
							msg = input.readUTF();
							System.out.println(msg);
						}
					} catch (IOException e1) {
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

}
