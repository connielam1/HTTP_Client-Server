/**
 ** ShareQ Server
 ** thread pool with a shared queue // Busy Wait method:
 **/

import java.io.*;
import java.util.*;
import java.net.*;


// **USAGE**: java <servername> -config <config_file_name>

class ShareQ extends Server{

    private  Thread[] threads;

    /* Constructor: starting all threads at once */
    public static void main(String args[]) throws Exception {
    	ShareQ serv = new ShareQ();
		serv.run(args);

    } // end of main

    
    public void run(String args[]) throws Exception{
		super.run(args);

		List<Socket> connSockPool;
    	connSockPool = new Vector<Socket>();
        threads = new Thread[Integer.parseInt(map.get("ThreadPoolSize"))];


        // start all threads
        for (int i = 0; i < threads.length; i++) {

        	// Socket s = null;
	        ShareQHandler sqh = new ShareQHandler(null, map, defaultServer, cache, cacheSize, cacheTimes, connSockPool); 
	        Thread t = new Thread(sqh);
	        threads[i] = t;
	        threads[i].start();

        }

	    while (true) {
	        try {
		        // accept connection from connection queue
		        Socket connSock = listenSocket.accept();
		        System.out.println("Main thread retrieve connection from " + connSock);

		        // how to assign to an idle thread?
		        synchronized (connSockPool) {
		            connSockPool.add(connSock);
		        } // end of sync

	        } catch (Exception e) {
	        	System.out.println("server run failed.");
	        } 
	    } 

		
    } // end of run

} // end of class SequentialServer