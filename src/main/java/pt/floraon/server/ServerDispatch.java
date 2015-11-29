package pt.floraon.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
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
import com.arangodb.entity.EntityFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import pt.floraon.dbworker.FloraOnException;
import pt.floraon.dbworker.FloraOnGraph;
import pt.floraon.dbworker.QueryException;
import pt.floraon.dbworker.TaxonomyException;
import pt.floraon.entities.GeneralNodeWrapperImpl;
import pt.floraon.entities.TaxEnt;
import pt.floraon.queryparser.YlemParser;
import pt.floraon.results.ChecklistEntry;
import pt.floraon.results.Occurrence;
import pt.floraon.results.ResultProcessor;
import pt.floraon.results.SimpleTaxonResult;

import static pt.floraon.server.Constants.*; 

public class ServerDispatch implements Runnable {
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
		return "{\"success\":true,\"msg\":"+obj+"}";
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
		} catch (FloraOnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	public static void processCommand(String command,FloraOnGraph graph,PrintWriter out) throws ArangoException, URISyntaxException, IOException, FloraOnException {
		URI url=new URI("/"+command.trim());
		processCommand(url,graph,out);
	}
	
	public static void processCommand(URI url,FloraOnGraph graph,PrintWriter output) throws QueryException, ArangoException, TaxonomyException, IOException, FloraOnException {
		JsonObject jobj;
    	String format,id,id2;
    	JsonObject header;
    	List<NameValuePair> qs=URLEncodedUtils.parse(url,Charset.defaultCharset().toString());
    	String command=url.getPath();
    	String name,author,rank,comment,current,description,shortName;
    	
    	String[] path=command.split("/");
    	if(path.length<2) {
    		output.println(error("This is Flora-On.\nMissing parameters."));
    		output.flush();
    		return;
    	}
    	switch(path[1]) {
		case "query":
			String query=getQSValue("q",qs);
			format=getQSValue("fmt",qs);
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
			rp=new ResultProcessor<SimpleTaxonResult>(it);
			switch(format) {
			case "html":
				output.println(rp.toHTMLTable());
				break;
				
			case "json":
			default:
				output.println(success(rp.toJSONElement(),header));
				break;
			}

			//out.printf("[%.3f sec]\n", (double)elapsedTime/1000000000);
			break;
			
		case "checklist":
			format=getQSValue("fmt",qs);
			if(format==null) format="json";
			List<ChecklistEntry> chklst=graph.getCheckList();
			Collections.sort(chklst);
			ResultProcessor<ChecklistEntry> rpchk=new ResultProcessor<ChecklistEntry>(chklst.iterator());
			switch(format) {
			case "json":
				header=new JsonObject();
				header.addProperty("nresults", chklst.size());
				output.println(success(rpchk.toJSONElement(),header));
				break;
			case "html":
				output.println(rpchk.toHTMLTable());
				break;
			case "csv":
				output.println(rpchk.toCSVTable());
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

			String infacets[];
			if(getQSValue("f",qs)==null || getQSValue("f",qs).equals(""))
				infacets=new String[]{"taxonomy"};
			else
				infacets=getQSValue("f",qs).split(",");
			
			Facets[] fac=new Facets[infacets.length];
			for(int i=0;i<infacets.length;i++) fac[i]=Facets.valueOf(infacets[i].toUpperCase());

			if(id==null) {
				TaxEnt te=graph.dbNodeWorker.findTaxEnt(query);
				if(te==null)
					output.println(success(graph.dbNodeWorker.getNeighbors(null,fac).toString()));
				else
					output.println(success(graph.dbNodeWorker.getNeighbors(te.getID(),fac).toString()));
			} else {
				String[] ids=id.split(",");
				if(ids.length==1)
					output.println(success(graph.dbNodeWorker.getNeighbors(ids[0],fac).toString()));
				else
					output.println(success(graph.dbNodeWorker.getRelationshipsBetween(ids,fac).toString()));
			}
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
			
			ranks=new JsonObject();
			for(AllRelTypes art:Constants.AllRelTypes.values()) {
				ranks.addProperty(art.toString(), art.getFacet().toString());
			}
			jobj.add("facets", ranks);
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
				output.println(graph.dbDataUploader.uploadTaxonomyListFromFile(query, false));
				break;
				
			case "attributes":
				output.println(graph.dbDataUploader.uploadMorphologyFromFile(query));
				break;

			case "authors":
				output.println(graph.dbDataUploader.uploadAuthorsFromFile(query));
				break;
				
			case "occurrences":
				output.println(graph.dbDataUploader.uploadRecordsFromFile(query));
				break;
			default:
				output.println(error("Unrecognized command: "+path[2]));
				break;
			}
			break;
		
		case "links":
			if(path.length<3) {
				output.println(error("Choose one of: add, update"));
				output.flush();
				return;
			}
			
			switch(path[2]) {
			case "add":
				id=getQSValue("from",qs);
				id2=getQSValue("to",qs);
				String type=getQSValue("type",qs);
				if(id==null || id.trim().length()<1 || id2==null || id2.trim().length()<1 || type==null) {
					output.println(error("You must provide relationship type and two document handles 'from' and 'to'"));
					output.flush();
					return;
				}

				GeneralNodeWrapperImpl n1=graph.dbNodeWorker.getNodeWrapper(id);
				GeneralNodeWrapperImpl n2=graph.dbNodeWorker.getNodeWrapper(id2);
				if(n1==null) {
					output.println(error("Node "+id+" not found."));
					output.flush();
					return;
				}
				if(n2==null) {
					output.println(error("Node "+id2+" not found."));
					output.flush();
					return;
				}
				try {
					output.println(success(
						n1.createRelationshipTo(n2.getNode(), AllRelTypes.valueOf(type.toUpperCase())).toString()
					));
				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
				
			case "update":
				id=getQSValue("id",qs);
				current=getQSValue("current",qs);
				output.println(success(
					graph.dbNodeWorker.UpdateDocument(id, "current", Integer.parseInt(current)==1).toString()
				));
				break;
			}
			break;

		case "nodes":
			if(path.length<3) {
				output.println(error("Choose one of: delete, add, update"));
				output.flush();
				return;
			}

			switch(path[2]) {
			case "delete":
				id=getQSValue("id",qs);
				if(id==null || id.trim().length()<1) {
					output.println(error("You must provide a document handle as id"));
					output.flush();
					return;
				}

				output.println(success(EntityFactory.toJsonString(graph.dbNodeWorker.deleteNode(id))));
				break;
				
			case "add":
				if(path.length<4) {
					output.println(error("Choose the node type: taxent"));
					output.flush();
					return;
				}
				switch(path[3]) {
				case "taxent":
					name=getQSValue("name",qs);
					author=getQSValue("author",qs);
					rank=getQSValue("rank",qs);
					
					output.println(success(
						graph.dbNodeWorker.createTaxEntNode(name, author, TaxonRanks.getRankFromValue(Integer.parseInt(rank)), null, true).toString()
					));
					break;
					
				case "attribute":
					name=getQSValue("name",qs);
					shortName=getQSValue("shortname",qs);
					description=getQSValue("description",qs);
					
					output.println(success(
						graph.dbNodeWorker.createAttributeNode(name, shortName, description).toString()
					));
					break;
				
				case "character":
					name=getQSValue("name",qs);
					shortName=getQSValue("shortname",qs);
					description=getQSValue("description",qs);
					
					output.println(success(
						graph.dbNodeWorker.createCharacterNode(name, shortName, description).toString()
					));
					break;
					
				default:
					output.println(error("Invalid node type"));
					output.flush();
					return;
				}
				break;
				
			case "update":
				if(path.length<4) {
					output.println(error("Choose the node type: taxent"));
					output.flush();
					return;
				}
				switch(path[3]) {
				case "taxent":
					name=getQSValue("name",qs);
					author=getQSValue("author",qs);
					rank=getQSValue("rank",qs);
					comment=getQSValue("comment",qs);
					current=getQSValue("current",qs);
					
					output.println(success(
						graph.dbNodeWorker.updateTaxEntNode(TaxEnt.fromHandle(graph,getQSValue("id",qs)), name, TaxonRanks.getRankFromValue(Integer.parseInt(rank)), Integer.parseInt(current)==1, author, comment).toString()
					));
					break;

				default:
					output.println(error("Invalid node type"));
					output.flush();
					return;
				}
			}
			break;

		case "occurrences":		// all options must return Occurrence lists!
			ResultProcessor<Occurrence> rpo=new ResultProcessor<Occurrence>(graph.getAllOccurrences());
			output.println(rpo.toCSVTable());
			break;
			
		default:
			output.println(error("Unknown command: "+path[1]));
			break;
		}
    	output.flush();
	}
}