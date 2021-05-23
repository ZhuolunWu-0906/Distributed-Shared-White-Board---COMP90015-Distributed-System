package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server{
	
	private static int port = 12306;
	//	private ArrayList<Thread>
	public static ArrayList<String> shapes = new ArrayList<String>();
	public static ArrayList<ServerThread> socketThreadList = new ArrayList<ServerThread>();
	public static ArrayList<String> names = new ArrayList<String>();
	public static int count = 0;
	public static ServerThread manager = null;
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = new ServerSocket(port);
		while(true){
			Socket socket = serverSocket.accept();
			new Thread(new ServerThread(socket)).start();
			System.out.println("New Client connected!");
		}
	}
	
	public synchronized static void addCount() {
		count++;
	}
	
	public synchronized static int getCount() {
		return count;
	}
	
	public synchronized static void addShape(String shape) {
		shapes.add(shape);
	}
	
	public synchronized static void clearShape() {
		shapes.clear();
	}
	
	public synchronized static int getIndex(String name) {
		return names.indexOf(name);
	}
	
	public synchronized static int getIndex(ServerThread st) {
		return socketThreadList.indexOf(st);
	}
	
	public synchronized static boolean nameExist(String name) {
		return names.contains(name);
	}
	
	public synchronized static String getName(int index) {
		return names.get(index);
	}
	
	public synchronized static String getName(ServerThread st) {
		return names.get(socketThreadList.indexOf(st));
	}
	
	public synchronized static ServerThread getServerSocket(int index) {
		return socketThreadList.get(index);
	}
	
	public synchronized static ServerThread getServerSocket(String name) {
		return socketThreadList.get(names.indexOf(name));
	}
	
	public synchronized static void remove(String name) {
		int index = names.indexOf(name);
		names.remove(index);
		socketThreadList.get(index).stopThread();
		socketThreadList.remove(index);
	}
	
	public synchronized static void remove(ServerThread st) {
		int index = socketThreadList.indexOf(st);
		st.stopThread();
		socketThreadList.remove(index);
		names.remove(index);
	}
	
	public synchronized static void remove(ArrayList<ServerThread> sts) {
		for (ServerThread st : sts) {
			int index = socketThreadList.indexOf(st);
			st.stopThread();
			socketThreadList.remove(index);
			names.remove(index);
		}
	}
	
	public synchronized static void addClient(String name, ServerThread st) {
		socketThreadList.add(st);
		names.add(name);
	}
	
}
