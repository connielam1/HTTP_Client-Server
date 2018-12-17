/**
 ** PerThreadServer
 ** Turn WebServer into a multithreaded server by creating a thread for each accepted request
 **/

import java.io.*;
import java.util.*;
import java.net.*;


// **USAGE**: java <servername> -config <config_file_name>

class PerThreadServer extends Server{

	public static void main(String args[]) throws Exception {
		PerThreadServer serv = new PerThreadServer();
		serv.run(args);
	} // end of main

	public void run(String args[]) throws Exception{
		super.run(args);

        while(true){
            Socket connSocket = listenSocket.accept();
            
            // process a request
            WebRequestHandler wrh = new WebRequestHandler(connSocket, map, defaultServer, cache, cacheSize, cacheTimes);
            Thread t = new Thread(wrh); // process request
            t.start();
        }
	}
} // end of class PerThreadServer




