package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import client.Shape;

public class Server{
	
	private static int port = 12306;
	//	private ArrayList<Thread>
	private ArrayList<Shape> shapes = new ArrayList<Shape>();
	public static ArrayList<ServerThread> socketThreadList = new ArrayList<ServerThread>();
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = new ServerSocket(port);
		while(true){
			Socket socket = serverSocket.accept();
			new Thread(new ServerThread(socket)).start();
			System.out.println("New Client connected!");
		}
	}
	
}
