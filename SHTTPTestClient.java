import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.lang.*;
import java.awt.Toolkit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// HTTP Protocol and Web Server

// Your test client should be multi-threaded. The client can generate test requests to the server with the following command line:
// java SHTTPTestClient -server <server> -servname <server name> -port <server port> -parallel <# of threads> -files <file name> -T <time of test in seconds>


// GET <URL> HTTP/1.0
// Host: <ServerName>
// CRLF


public class SHTTPTestClient{

	private static class StatsCollector{

		private int numDownloaded;
		private int numBytesReceived;
		private long totalWaitTime;
		private Object lock1;
		private Object lock2;
		private Object lock3;

		public StatsCollector(){
			this.numDownloaded = 0;
			this.numBytesReceived = 0; 
			this.totalWaitTime = 0;
			this.lock1 = new Object();
			this.lock2 = new Object();
			this.lock3 = new Object();
		}

		// should these methods be synchronized? or use volatile?
		public void incr_numDownloaded(){
			synchronized(lock1){
				numDownloaded++;
			}
		}

		public void incr_numBytesReceived(int toAdd){
			synchronized(lock2){
				numBytesReceived += toAdd; 
			}
		}

		public void incr_totalWaitTime(long toAdd){
			synchronized(lock3){
				totalWaitTime += toAdd;
			}
		}				
	}

	private static class ThreadRequest implements Runnable {
		private InetAddress serverIPAddress;
		private int port;
		private ArrayList<String> files;
		private long timeout;
		private String servname;
		private DataOutputStream outFromClient;
		private BufferedReader inFromServer;
		private StatsCollector allStats;
		private Pattern contLenPattern = Pattern.compile("Content-Length:.*");

		ThreadRequest(StatsCollector allStats, InetAddress serverIPAddress, int port, 
										String servname, String filename, long timeout)
		{
			this.serverIPAddress = serverIPAddress;
			this.port = port;
			this.servname = servname; // because multiple virtual servers can map to same IP
			this.timeout = timeout;
			this.files = new ArrayList<String>();
			this.allStats = allStats;

			try{
				Scanner s = new Scanner(new File(filename));
				while (s.hasNext()){
				    files.add(s.next());
				}
				s.close();
			}
			catch(FileNotFoundException ex){
				System.err.println(ex);
				System.exit(0);
			}
		}
		public void run() {
			// pass in start time, check end time in while loop
			// timeout situation, keep running while loop until time runs out
			Thread t = Thread.currentThread();
			System.out.println(t.getName());

			int counter = 0;			
			// get input and output streams from socket, create a socket for each thread
			// each thread loops through all the files 
			while(timeout > System.currentTimeMillis()){
				for(int i = 0; i < files.size(); i++)
				{
					if (timeout <= System.currentTimeMillis()){
						System.out.println("Number Downloaded in this thread: " + counter);
						System.out.println("Wait time for this thread: " + allStats.totalWaitTime);
						return;
					} 

					try{

						Socket clientSocket = new Socket(serverIPAddress, port); // one socket per thread?
						outFromClient = new DataOutputStream(clientSocket.getOutputStream());

						//String tempDate = "";

						long time_sent = System.currentTimeMillis();
						outFromClient.writeBytes("GET /" + files.get(i) + " HTTP/1.0\r\n");
	    				outFromClient.writeBytes("Host: " + servname + "\r\n");

	    				// Fake date to test
	    				//outFromClient.writeBytes("If-Modified-Since: "+ "Thu, 18 Oct 2018 16:16:21 -0400\r\n");
						outFromClient.writeBytes("\r\n");

						inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

						String requestMessageLine = inFromServer.readLine();
						System.out.println(requestMessageLine);
						String[] request = requestMessageLine.split("\\s");
						String response_code = request[1];

						// Collect Statistics
						String line;
						if (response_code.equals("200")){
							allStats.incr_totalWaitTime(System.currentTimeMillis() - time_sent);
							allStats.incr_numDownloaded(); // number is slightly off? maybe just for Sequential?
							while((line = inFromServer.readLine())!= null){
								System.out.println(line);
								allStats.incr_numBytesReceived(line.getBytes().length); 
								
								Matcher m = contLenPattern.matcher(line);
								// Found Content-Length // Then we break
								if (m.matches()){
									String[] content_str = line.split("\\s");
									allStats.incr_numBytesReceived(Integer.parseInt(content_str[1])); 
									break;
								}	
							}
							counter = counter + 1;
						} 

						if(clientSocket!= null)
							clientSocket.close();
					    // pause by waiting for keyboard // netstat -rn -p tcp | more

					} catch(IOException ex){
						System.err.println(ex);
					} 					
				}

			}
			System.out.println("Number Downloaded in this thread: " + counter);
		}			 
	}

  	public static void main(String[] args) {
    
		if (args.length != 12) {
			System.out.println("java SHTTPTestClient -server <server> -servname <server name> -port <server port> -parallel <# of threads> -files <file name> -T <time of test in seconds>");
			return;
		}   

		String host = "";
		InetAddress serverIPAddress = null;
		String servname = "";
		int port = 0;
		int parallel = 0;
		long timeTest = 0;
		String filename = "";

        for(int i = 0; i< 12; i++){
            if (args[i].equals("-server") && (i + 1) < args.length){
            	// look at apache file to understand server name?
				host = args[i+1];  
				try{
					serverIPAddress = InetAddress.getByName(host);
				}catch(UnknownHostException uhe){
					System.err.println(uhe);
					System.exit(0);
				}
                i++;
            }
            else if (args[i].equals("-servname") && (i + 1) < args.length){
                servname = args[i+1];
                i++;
            }
            else if (args[i].equals("-port") && (i + 1) < args.length){
                port = Integer.parseInt(args[i+1]);
                System.out.println(port);
                i++;
            }
            else if (args[i].equals("-parallel") && (i + 1) < args.length){
                parallel = Integer.parseInt(args[i+1]);
                i++;
            }
            else if (args[i].equals("-files") && (i + 1) < args.length){
                // make sure it's a double between 0 and 1
                filename = args[i+1];
                i++;
            }
            else if (args[i].equals("-T") && (i + 1) < args.length){
                // make sure it's a double between 0 and 1
                timeTest = Long.parseLong(args[i+1]);
                i++;
            }
            else{
                // Error in Usage 
				System.out.println("java SHTTPTestClient -server <server> -servname <server name> -port <server port> -parallel <# of threads> -files <file name> -T <time of test in seconds>");
                return;
            }
        }
       
		System.out.println(serverIPAddress);
		StatsCollector sharedStats = new StatsCollector();
		
		ArrayList<Thread> threads = new ArrayList<Thread>();

		// simultaneously create all parallel threads at once .. in for loop
		long start_time = System.currentTimeMillis(); 
		long timeout = start_time + timeTest * 1000;

		for(int i = 0; i < parallel ; i ++){
			ThreadRequest rh = new ThreadRequest(sharedStats, serverIPAddress, port, servname, filename, timeout);
			Thread t = new Thread(rh); // each thread loops through all the files 
			t.start(); // dies at the end of the run method
			threads.add(t);
			System.out.println("Thread started");

		}
		try {
			for (Thread t: threads){ 
				t.join();
			}
		} catch (Exception e){
			System.err.println("Join errors");
		}

		/**
		** The client should print out:
		**- the total transaction throughput (# files finished downloading by all threads, averaged over per second), 
		**- data rate throughput (number bytes received, averaged over per second)
		**-  the average of wait time (i.e., time from issuing request to getting first data)
		**/

		System.out.println();
		System.out.println("STATISTICS: ");
		System.out.println("TOTAL DOWNLOADED: " + sharedStats.numDownloaded);
		System.out.println("TOTAL Bytes downloaded: " + sharedStats.numBytesReceived);
		System.out.println("TOTAL Wait Time: " + sharedStats.totalWaitTime);

		System.out.println();
		// do exact time elapsed? or seconds running?
		System.out.println("Total Transaction Throughput: " + ((double)sharedStats.numDownloaded/((double)timeTest)));
		System.out.println("Data Rate Throughput: " + ((double)sharedStats.numBytesReceived/((double)timeTest)));
		System.out.println("Average Wait Time " + (sharedStats.totalWaitTime/(double)sharedStats.numDownloaded));

	}

}


