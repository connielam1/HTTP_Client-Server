

// Suspension


import java.io.*;
import java.net.*;
import java.util.*;

public class ShareQSuspensionHandler extends WebRequestHandler {

    private List<Socket> pool;

    public ShareQSuspensionHandler(Socket connectionSocket, HashMap<String, String> map,
    							String defaultServer, HashMap<String, byte[]> cache, int cacheSize, 
    							HashMap<String,Long> cacheTimes, List<Socket> pool) throws Exception{

    	super(connectionSocket, map, defaultServer, cache, cacheSize, cacheTimes); // invoke base class constructor
	    this.pool = pool;
    }
  
    public void run() {
    	
	    while (true) {
	        // get a new request connection
	        //Worker thread continually spins (busy wait) until cond
	    	//Suspension --Put thread to sleep to avoid busy spin
		    connSocket = null;
			synchronized (pool) {         
				while (pool.isEmpty()) {
					try {
						System.out.println("Thread " + this + " sees empty pool.");
						pool.wait();
					}
					catch (InterruptedException ex) {
						System.out.println("Waiting for pool interrupted.");
					} // end of catch
				} // end of while
				
				// remove the first request
				connSocket = (Socket) pool.remove(0); 
				System.out.println("Thread " + this 
								   + " process request " + connSocket);
			} // end of extract a request

	        super.processRequest();	
	    } 

    } // end method run

} // end ServiceThread