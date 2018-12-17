/**
 ** ShareQ Server
 ** thread pool with a shared queue // Suspension
 **/

import java.io.*;
import java.util.*;
import java.net.*;


// **USAGE**: java <servername> -config <config_file_name>

class ShareQSuspension extends Server{
    private  Thread[] threads;
    public final static int THREAD_COUNT = 2; // temp

    /* Constructor: starting all threads at once */
    public static void main(String args[]) throws Exception {
    	ShareQSuspension serv = new ShareQSuspension();
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
	        ShareQSuspensionHandler sqh = new ShareQSuspensionHandler(null, map, defaultServer, cache, cacheSize, cacheTimes, connSockPool); 
	        Thread t = new Thread(sqh);
	        threads[i] = t;
	        threads[i].start();

        }

	    while (true) {
	        try {
		        // accept connection from connection queue
		        Socket connSock = listenSocket.accept();
		        System.out.println("Main thread retrieve connection from " 
				                   + connSock);

		        synchronized (connSockPool) {
		        	System.out.println("HERE");
		            connSockPool.add(connSock);
		            connSockPool.notifyAll();
		        } // end of sync

	        } catch (Exception e) {
	        	System.out.println("Accept thread failed.");
	        } 
	    } 

    } // end of run

} // end of class SequentialServer