
/**
 ** Sequential Server
 **/

import java.io.*;
import java.net.*;
import java.util.*;


// **USAGE**: java <servername> -config <config_file_name>

class SequentialServer extends Server{

	public static void main(String args[]) throws Exception {
		SequentialServer serv = new SequentialServer();
		serv.run(args);
	} // end of main

	// Process one request at a time
	public void run(String args[]) throws Exception{
		super.run(args);

		System.out.println("server: "+ defaultServer + " www root: " + map.get(defaultServer));
		while(true){
			Socket connSocket = listenSocket.accept();

		    System.out.println("\nReceive request from " + connSocket);
	
		    // process a request ... 
		    WebRequestHandler wrh = new WebRequestHandler(connSocket, map, defaultServer, cache, cacheSize, cacheTimes); 
		    wrh.processRequest();
		}
	} 

} // end of class SequentialServer