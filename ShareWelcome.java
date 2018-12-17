/**
 ** ShareWelcome Server
 ** thread pool with service threads competing on welcome socket;
 **/

import java.io.*;
import java.util.*;
import java.net.*;


// **USAGE**: java <servername> -config <config_file_name>
// distribute the requests from the welcome socket to the thread workers

// ThreadPoolSize ** need this parameter in the config file

public class ShareWelcome extends Server{
    private Thread[] threads;

	public static void main(String args[]) throws Exception {
		ShareWelcome serv = new ShareWelcome();
		serv.run(args);
	} // end of main

	public void run(String args[]) throws Exception{
		super.run(args);
	    
        threads = new Thread[Integer.parseInt(map.get("ThreadPoolSize"))];

        // start all threads
        for (int i = 0; i < threads.length; i++) {
	        ShareWelcomeHandler swh = new ShareWelcomeHandler(null, map, defaultServer, 
	        													cache, cacheSize, cacheTimes, listenSocket); // listenSocket = welcomeSocket 
	        threads[i] = new Thread(swh);
	        threads[i].start();
		}


        for (int i = 0; i < threads.length; i++) {
		    threads[i].join();
        }
        System.out.println("All threads finished. Exit");

	    
	}

} // end of class