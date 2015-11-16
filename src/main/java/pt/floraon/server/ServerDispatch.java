package pt.floraon.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.RequestLine;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicLineParser;

import com.arangodb.ArangoException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import pt.floraon.dbworker.FloraOnGraph;
import pt.floraon.dbworker.QueryException;
import pt.floraon.dbworker.TaxonomyException;
import pt.floraon.entities.ChecklistEntry;
import pt.floraon.entities.SimpleTaxonResult;
import pt.floraon.entities.TaxEnt;
import pt.floraon.queryparser.YlemParser;
import static pt.floraon.server.Constants.*; 

public class ServerDispatch implements Runnable{
    protected Socket clientSocket = null;
    protected String serverText   = null;
    protected FloraOnGraph graph=null;
    protected MultiThreadedServer thr;

    public ServerDispatch(Socket clientSocket, String serverText,FloraOnGraph graph,MultiThreadedServer thr) {
        this.clientSocket = clientSocket;
        this.serverText   = serverText;
        this.graph=graph;
        this.thr=thr;
    }
    
	public static String getQSValue(String key,List<NameValuePair> qs) {
		for(NameValuePair i:qs) {
			if(i.getName().equals(key)) return(i.getValue());
		}
		return null;
	}
	
	private static String success(String obj) {
		return "{\"success\":true,\"msg\":\""+obj+"\"}";
	}

	private static String success(JsonElement obj,JsonObject header) {
		JsonObject out;
		if(header==null)
			out=new JsonObject();
		else
			out=header;
		out.addProperty("success", true);
		out.add("msg", obj);
		return out.toString();
	}

	private static String error(String obj) {
		return "{\"success\":false,\"msg\":\""+obj+"\"}";
	}
   
// dispatch a request to port 9000
	public void run() {
		OutputStream ostr=null;
		try {
			ostr = clientSocket.getOutputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        try(PrintWriter out = new PrintWriter(new OutputStreamWriter(ostr, StandardCharsets.UTF_8), true);BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) {
        	String line=in.readLine();
        	if(line.equals("STOP")) {
        		thr.stop(out);
        		return;
        	}
        	BasicLineParser blp=new BasicLineParser();
        	RequestLine requestline=BasicLineParser.parseRequestLine(line,blp);
        	URI url;
        	try {
        		url=new URIBuilder(requestline.getUri()).build();
        	} catch (URISyntaxException e) {
    			out.println(e.getMessage());
    			return;
    		}

        	processCommand(url,this.graph,out);
        	out.close();
        } catch (ArangoException | QueryException | TaxonomyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
	
	public static void processCommand(String command,FloraOnGraph graph,PrintWriter out) throws QueryException, ArangoException, TaxonomyException, URISyntaxException, IOException {
		URI url=new URI("/"+command);
		processCommand(url,graph,out);
	}
	
	public static void processCommand(URI url,FloraOnGraph graph,PrintWriter output) throws QueryException, ArangoException, TaxonomyException, IOException {
		JsonObject jobj;
    	String format,id;
    	JsonObject header;
    	List<NameValuePair> qs=URLEncodedUtils.parse(url,Charset.defaultCharset().toString());
    	String command=url.getPath();

    	String[] path=command.split("/");
    	if(path.length<2) {
    		output.println(error("This is Flora-On.\nMissing parameters."));
    		output.flush();
    		return;
    	}
    	switch(path[1]) {
		case "query":
			String query=getQSValue("q",qs);
			if(query==null || query.length()<1) {
				output.println(error("Missing query."));
				output.flush();
				return;
			}
			Iterator<SimpleTaxonResult> it;
			ResultProcessor<SimpleTaxonResult> rp;
			YlemParser ylem=new YlemParser(graph,query);
			long start = System.nanoTime();
			List<SimpleTaxonResult> res=ylem.execute();
			long elapsedTime = System.nanoTime() - start;
			
			if(res==null) res=new ArrayList<SimpleTaxonResult>();
				
			//out.println(res.size()+" results.");
			header=new JsonObject();
			header.addProperty("time", (double)elapsedTime/1000000000);
			header.addProperty("nresults", res.size());
			it=res.iterator();
			rp=new ResultProcessor<SimpleTaxonResult>(SimpleTaxonResult.class);
			output.println(success(rp.toJSONElement(it),header));

			//out.printf("[%.3f sec]\n", (double)elapsedTime/1000000000);
			break;
			
		case "checklist":
			format=getQSValue("fmt",qs);
			if(format==null) format="json";
			ResultProcessor<ChecklistEntry> rpchk=new ResultProcessor<ChecklistEntry>(ChecklistEntry.class);
			List<ChecklistEntry> chklst=graph.getCheckList();
			Collections.sort(chklst);
			switch(format) {
			case "json":
				header=new JsonObject();
				header.addProperty("nresults", chklst.size());
				output.println(success(rpchk.toJSONElement(chklst.iterator()),header));
				break;
			case "html":
				output.println(rpchk.toHTMLTable(chklst.iterator()));
				break;
			case "csv":
				output.println(rpchk.toCSVTable(chklst.iterator()));
				break;
			}
			break;
			
		case "getneighbors":
			id=getQSValue("id",qs);
			query=getQSValue("q",qs);
			if(id==null && query==null) {
				output.println(error("Missing query."));
				break;
			}
			if(id==null) {
				TaxEnt te=graph.findTaxEnt(query);
				if(te==null)
					output.println(success(graph.getNeighbors("sometthingnomatch")));
				else
					output.println(success(graph.getNeighbors(te.getID())));
			} else
				output.println(success(graph.getNeighbors(id)));
			break;
			
		case "reference":
			jobj=new JsonObject();
			JsonObject ranks=new JsonObject();	// a map to convert rank numbers to names
			StringBuilder rk=new StringBuilder();
			for(TaxonRanks e : Constants.TaxonRanks.values()) {
				ranks.addProperty(e.getValue().toString(), e.toString());
				rk.append("<option value=\""+e.getValue().toString()+"\">"+e.getName()+"</option>");
			}
			jobj.add("rankmap", ranks);
			jobj.addProperty("rankelement", rk.toString());
			output.println(success(jobj.toString()));
			/*
			rk=new StringBuilder();
			
			for(TaxonomyRelTypes rt:TaxonomyRelTypes.values()) {
				rk.append("<option value=\""+rt.name()+"\">"+rt.name()+"</option>");
			}   			
			obj.put("reltypes", rk.toString());
			obj.put("success", true);
			out.println(obj.toJSONString());*/
			break;
			
		case "upload":
			if(path.length<3) {
				output.println(error("Choose one of: taxonomy, attributes or occurrences"));
				output.flush();
				return;
			}
			
			query=getQSValue("file",qs);
			if(query==null || query.length()<1) {
				output.println(error("You must provide the full path to a local text file to upload."));
				output.flush();
				return;
			}

			switch(path[2]) {
			case "taxonomy":
				graph.getDataUploader().uploadTaxonomyListFromFile(query, false);
				break;
				
			default:
				output.println(error("Unrecognized command: "+path[2]));
				break;
			}
			break;
			
		default:	
			output.println(error("Unknown command: "+path[1]));
			break;
		}
    	output.flush();
	}
}