
import java.io.*;
import java.util.*;
import java.net.*;

// base class 
// **USAGE**: java <servername> -config <config_file_name>

class Server  
{ 
    public static ServerSocket listenSocket;
    public static HashMap<String, String> map = new HashMap<>();
    // public static ArrayList<String> serverList = new ArrayList<String>();

    // Cache should exist in Server Class and not WebRequest Handler
    public static HashMap<String, byte[]> cache; 
    public static HashMap<String,Long> cacheTimes = new HashMap<String,Long>();
    public static int cacheSize = 0;    
    public static String defaultServer;
    // public static HeartbeatMonitor currentHB;

    // Server Class has two methods      
    public void parseConfig(String args[]) { 
        // see if we do not use default server port
        if(args.length != 2 && ! args[0].equals("-config")){
            System.out.println("USAGE: java <servername> -config <config_file_name>");
            return;
        }

        String configfile = args[1]; 
        int numVirtHosts = 0;
        try{
            Scanner s = new Scanner(new File(configfile));
            while (s.hasNext()){
                String curLine = s.nextLine();
                String[] content_str = curLine.split("\\s");

                if (content_str[0].equals("Listen") || content_str[0].equals("CacheSize") || 
                    content_str[0].equals("Monitor") || content_str[0].equals("ThreadPoolSize")){
                    map.put(content_str[0], content_str[1]); 
                }
                else if (content_str[0].equals("<VirtualHost")){
                    numVirtHosts++;
                    String[] docLine = s.nextLine().split("\\s+");
                    String[] servLine = s.nextLine().split("\\s+");

                    map.put(servLine[2], docLine[2]); 
                    // set default server as the very first Virtual host listed
                    if (numVirtHosts == 1)
                        defaultServer = servLine[2]; 
                        System.out.println(defaultServer);
                    s.nextLine(); // skips the "</VirtualHost>" line
                }
            }
            s.close();
        }
        catch(FileNotFoundException ex){
            System.err.println(ex);
            System.exit(0);
        }
    } 
          
    // Server run method
    public void run(String args[]) throws Exception{
        parseConfig(args);
        // create server socket
        this.listenSocket = new ServerSocket(Integer.parseInt(map.get("Listen")));
        this.cache = new HashMap<>(); // pass cache into run function of webrequest handler //TODO

        System.out.println("server listening at: " + listenSocket);
    } 
} 

