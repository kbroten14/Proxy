/**
 * 
 * Kevin Broten
 * Networks Spring 2016
 * Homework 3
 * 
 */

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.lang.Thread;

class Connection implements Runnable{
	private Socket client;
	
	public Connection(Socket client){
		this.client = client;
	}
	
	//process client in new thread
	//read from client socket
	//		parse request to obtain origin host and resource
	//		open socket to origin host socket
	//write to the origin host socket
	//		make HTTP 1.1 request:
	//			GET resource
	//			Host: origin host
	//			Connection: close
	//read from the origin host socket
	//write response to the client socket
	
	public void run(){
		try{
			PrintWriter toClient = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
			BufferedReader fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
			
			String request = fromClient.readLine(); //just need first line with GET
			
			String[] hostResource = parseRequest(request);
			String host = hostResource[0];
			String resource = hostResource[1];
			
			//HTTP request and post http response to client
			Socket hostSock = new Socket(host, 80);
			BufferedReader fromHost = new BufferedReader(new InputStreamReader(hostSock.getInputStream()));
			PrintWriter toHost = new PrintWriter(hostSock.getOutputStream());
			
			String post = "GET " + resource + " HTTP/1.1\r\n" +
						  "Host: " + host + "\r\n" +
						  "Connection: close\r\n\r\n";
			
			toHost.print(post);
			toHost.flush();
			
			String line;
			while((line = fromHost.readLine()) != null){
				if (line.length() == 0) break;
				System.out.println(line);
				toClient.println(line);
				toClient.flush();
			}
			
			hostSock.close();
			toClient.close();
			fromClient.close();
			fromHost.close();
			toHost.close();
			
			//close connection
			this.client.close();
		}
		catch(IOException ioe){
			System.err.println("In Connection: " + ioe.getMessage());
			ioe.printStackTrace();
		}
	}
	
	private String[] parseRequest(String req){
		String host = "";
		String res = "";
		if(!req.equals(null)){
			String[] splitArr = req.split(" ");
			String[] urlArr = splitArr[1].split("/");
			host = urlArr[1];
			if(urlArr.length > 2){
				res = getResource(urlArr);
			}
			else{
				res = "/";
			}
		}
		String[] hostResource = {host, res};
		return hostResource;
	}
	
	private String getResource(String[] urlArr){
		StringBuilder res = new StringBuilder();
		for(int i = 2; i < urlArr.length; i++){
			res.append("/" + urlArr[i]);
		}
		return res.toString();
	}
}



public class ProxyServer {
	static final int PORT = 8080;
	
	public static void main(String[] args){
		
		try{
			ServerSocket socket = new ServerSocket(PORT);
			
			while(true){
				Socket client = socket.accept();
				Thread connection = new Thread(new Connection(client));
				connection.start();
			}
		}
		catch (IOException ioe){
			System.err.println("ProxyServer: " + ioe.getMessage());
			ioe.printStackTrace();
		}
	}
}
