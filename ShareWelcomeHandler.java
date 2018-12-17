
import java.io.*;
import java.net.*;
import java.util.*;

public class ShareWelcomeHandler extends WebRequestHandler {

    ServerSocket welcomeSocket;

    public ShareWelcomeHandler(Socket connectionSocket, HashMap<String, String> map,
    							 String defaultServer, HashMap<String, byte[]> cache, int cacheSize, HashMap<String,Long> cacheTimes,
    							ServerSocket welcomeSocket) throws Exception{
    	
    	super(connectionSocket, map, defaultServer, cache, cacheSize, cacheTimes); // invoke base class constructor
	    this.welcomeSocket = welcomeSocket;
    }
  
    public void run() {    	
	    while (true) {
	        // get a new request connection
	        //Socket s = null;
	        synchronized (welcomeSocket) {         
		        try {
		            connSocket = welcomeSocket.accept();
		            System.out.println("Thread " + this  + " process request " + connSocket);
		        } catch (IOException e) {
		        }
	        } // end of extract a request

	        super.processRequest();			
	    } 

    } // end method run

} // end ServiceThread