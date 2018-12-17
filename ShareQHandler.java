

// Busy Wait method:


import java.io.*;
import java.net.*;
import java.util.*;

public class ShareQHandler extends WebRequestHandler {

    private List<Socket> pool;

    public ShareQHandler(Socket connectionSocket, HashMap<String, String> map,
    							String defaultServer, HashMap<String, byte[]> cache, int cacheSize, 
    							HashMap<String,Long> cacheTimes, List<Socket> pool) throws Exception{

    	super(connectionSocket, map, defaultServer, cache, cacheSize, cacheTimes);  // invoke base class constructor
	    this.pool = pool;
    }
  
    public void run() {
    	
	    while (true) {
	        // get a new request connection
	       //Worker thread continually spins (busy wait) until cond
	    	connSocket = null;
	        while (connSocket == null) {
		        synchronized (pool) {         
		            if (!pool.isEmpty()) {
			           // remove the first request
			           connSocket = (Socket) pool.remove(0); 
			           System.out.println("Thread " + this + " process request " + connSocket);
		            } 
		        } 
	        } 
	        super.processRequest();	
	    } 

    } // end method run

} // end ServiceThread