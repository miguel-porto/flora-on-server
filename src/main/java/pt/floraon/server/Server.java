package pt.floraon.server;

import java.text.ParseException;
import com.arangodb.ArangoException;

import pt.floraon.driver.FloraOnDriver;

public class Server 
{
    public static void main( String[] args ) throws ParseException {
    	FloraOnDriver graph;
    	try {
    		graph=new FloraOnDriver("flora");
		} catch (ArangoException e2) {
			e2.printStackTrace();
			return;
		}

    	System.out.println("Starting Flora-On server on port 9000...");
		MultiThreadedServer server = new MultiThreadedServer(9000,graph);
		server.run();

/*    	FloraOnGraph fog;
    	
		int maxAttemps=100;
		
		if(args.length<1) {
			System.out.println("Expected arguments start | stop | direct");
			return;
		}
		
		if(args[0].equals("direct")) {
			System.out.println("Starting server directly. Press Ctrl+C to quit.");
			StartServer(System.getProperty("user.home")+"/lixotax");
		}
		if(args[0].equals("start")) {
			String DBPATH;
			if(args.length==1) {
				DBPATH=System.getProperty("user.home")+"/lixotax";
				System.out.println("No database path given. Using "+DBPATH);
			} else DBPATH=args[1];
			System.out.println("Starting server on "+DBPATH);
			System.out.print("\nWaiting for server to be ready");
			Runtime.getRuntime().exec("java -jar fluidTaxonomy.jar startserver "+DBPATH);
			Socket server=null;
			
		    int attempts = 0;
	        while(attempts < maxAttemps) {
				try {
					server = new Socket("localhost", 9000);
				} catch (IOException e) {
					System.out.print(".");
				}
				if(server!=null) break;
	            attempts++;
	            Thread.sleep(1000);
	        }
	        if(attempts==maxAttemps)
	        	System.out.println("\nServer not ready...");
	        else
	        	System.out.println("\nServer ready on port 9000");
	        
		}
		
		if(args[0].equals("stop")) {
			System.out.println("Opening socket...");
		    Socket server = new Socket("localhost", 9000);
		    PrintWriter out = new PrintWriter(server.getOutputStream(), true);
		    java.io.InputStream instream=server.getInputStream();
		    
		    // send stop message to server
		    out.println("STOP");
		    
		    System.out.println("Waiting for server to stop");
		    BufferedReader in = new BufferedReader(new InputStreamReader(instream));
		    String a;
		    int attempts = 0;
	        while(attempts < maxAttemps) {
	        	a=in.readLine();
	        	if(a==null)
	        		System.out.print(".");
	        	else if(a.equals("STOPPED"))
	        		break;
   				else
	        		System.out.println(a);
	            attempts++;
	            Thread.sleep(1000);
	        }
	        
	        out.close();
			in.close();
			server.close();
		}
		
		if(args[0].equals("startserver")) {
			String DBAPTH=args[1];
			StartServer(DBAPTH);
		}

    	try {
			fog=new FloraOnGraph("flora");
		} catch (ArangoException e2) {
			e2.printStackTrace();
			return;
		}
    	*/
    	
    }
}
