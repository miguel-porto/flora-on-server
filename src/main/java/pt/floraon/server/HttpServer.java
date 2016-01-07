package pt.floraon.server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import pt.floraon.driver.FloraOnDriver;
import pt.floraon.driver.FloraOnException;
/**
 * Handles the request to the web admin pages... a hacky implementation of a dynamic web server!
 * @author miguel
 *
 */
public final class HttpServer {
	static void processRequest(URI url,OutputStream outputStream,ServerDispatch server, Boolean includeHeaders) throws FileNotFoundException, IOException {
		PrintWriter out=null;
    	String command=url.getPath();
    	String[] path=command.split("/");
		if(path[path.length-1].equals("admin")) path=new String[] {"","admin","index.html"};
		HttpResponse res=new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP",1,1),200,""));
		Pattern ext=Pattern.compile("\\.([a-zA-Z]+)$",Pattern.CASE_INSENSITIVE);
		Matcher match=ext.matcher(path[path.length-1].trim());
		boolean processFile=false;
		if(match.find()) {
			switch(match.group(1)) {
			case "html":
			case "htm":
				out = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
				res.addHeader(new BasicHeader("Content-Type","text/html; charset=utf-8"));
				processFile=true;
				break;
			case "png":
				out = new PrintWriter(outputStream);
				res.addHeader(new BasicHeader("Content-Type","image/png"));
				break;
			case "js":
				out = new PrintWriter(outputStream);
				res.addHeader(new BasicHeader("Content-Type","application/javascript"));
				break;
			case "css":
				out = new PrintWriter(outputStream);
				res.addHeader(new BasicHeader("Content-Type","text/css"));
				break;
			case "csv":
				out = new PrintWriter(outputStream);
				res.addHeader(new BasicHeader("Content-Type","text/csv; charset=Windows-1252"));
				res.addHeader(new BasicHeader("Content-Disposition","attachment;Filename=\"Checklist_flora_Portugal.csv\""));
				break;
			default:
				out = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
				res.addHeader(new BasicHeader("Content-Type","text/html; charset=utf-8"));
				break;
			}
		} else {
			out = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
			res.addHeader(new BasicHeader("Content-Type","text/html; charset=utf-8"));
		}
		//res.addHeader("Set-Cookie","c1=adsfg; path=/; domain=\".app.localhost:9000\"");
		// get querystring
		List<NameValuePair> params=URLEncodedUtils.parse(url,Charset.defaultCharset().toString());
		if(includeHeaders) {
			out.print(res.toString()+"\r\n");
			out.print("\r\n");
			out.flush();
		}

		File page;
		StringBuilder address=new StringBuilder();
		for(int i=0;i<path.length;i++) {
			if(path[i].equals("admin")) {
				address.append("web");
				for(int j=i+1;j<path.length;j++) {
					address.append("/").append(path[j]);
				}
			}
		}
		page=new File(address.toString());
		
		if(processFile) {	// it's an HTML file, look for dynamic content <!-- CONTENT:/address -->
			Pattern cnt=Pattern.compile("<!-- CONTENT:([a-zA-Z0-9_/?=%+-]+) -->");
			BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(page), StandardCharsets.UTF_8));	//new BufferedReader(new FileReader(page));
			String line;
			while((line=br.readLine())!=null) {	// scan the file for dynamic content
				Matcher mat=cnt.matcher(line);
				if(mat.find()) {	// this line has dynamic content
					//ByteArrayOutputStream  baos=new ByteArrayOutputStream();
					out.flush();
					try {
						URI urlcnt=new URI(mat.group(1).trim());	// this is the dynamic content URL
						List<NameValuePair> paramscnt=URLEncodedUtils.parse(urlcnt,Charset.defaultCharset().toString());	// this is the content query variables
						List<NameValuePair> mergeparams=new ArrayList<NameValuePair>(paramscnt);
						mergeparams.addAll(params);	// merge the content query variables with the parent query variables
						// NOTE: if there are duplicate keys, the content query variables take precedence!
						server.processCommand(urlcnt, mergeparams, outputStream, false);
						//ServerDispatch.processCommand(urlcnt, mergeparams, graph, baos, false);
					} catch (FloraOnException e) {
						out.println("Error: "+e.getMessage());
					} catch (URISyntaxException e) {
						out.println("Error: "+e.getMessage());
						e.printStackTrace();
					}
					/*baos.close();
					line=mat.replaceFirst(baos.toString());*/
					
				} else out.println(line);
			}
			br.close();
		} else {	// it's a static file
			try {
				IOUtils.copy(new FileInputStream(page), outputStream);
			} catch (FileNotFoundException e) {		// file does not exist? add exceptions here!
				if(address.equals("web/checklist.csv")) {
					ByteArrayOutputStream  baos=new ByteArrayOutputStream();
					try {
						server.processCommand("lists/checklist?fmt=csv", baos,false);
					} catch (URISyntaxException e1) {
						out.println("Error");
					} catch (FloraOnException e1) {
						out.println("Error: "+e1.getMessage());
					}
					baos.close();
					out.write(baos.toString());
				}
			}
		}
		return;
	}
	/*
	static String makePageContent(List<NameValuePair> params,FloraOnDriver graph) throws IOException {
		String what=getQSValue("w",params);
		String fmt=getQSValue("fmt",params);
		StringBuilder out=new StringBuilder();
		ByteArrayOutputStream baos;
		
		if(what==null) return "<p>Page not found.</p>";
		
		try {
			switch(what) {		// the 'w' parameter of the URL querystring
			case "species":
				out.append("<div id=\"main\" class=\"species\">");
				out.append("<div class=\"togglevis\"><h3>Add new taxon</h3><div class=\"content\">");
				out.append("<form class=\"poster\" data-path=\"/nodes/add/taxent\"><p>Taxon name: <input type=\"text\" name=\"name\"/> Authority: <input type=\"text\" name=\"author\"/> Rank: ");
				baos=new ByteArrayOutputStream();
				ServerDispatch.processCommand("reference/ranks", graph, baos, false);
				out.append(baos.toString(StandardCharsets.UTF_8.name()));
				baos.close();
				out.append(" <input type=\"submit\" value=\"Add\"/></p></form>");
				out.append("</div></div>");
				
				baos=new ByteArrayOutputStream();
				ServerDispatch.processCommand("lists/species?fmt=json", graph, baos, false);
				baos.close();
				out.append("<table>");
				JsonObject res=(new JsonParser().parse(baos.toString())).getAsJsonObject(),tmp;
				JsonArray spp=res.get("msg").getAsJsonArray();
				for(JsonElement je : spp) {
					//SimpleNameResult snr=EntityFactory.createEntity(je.getAsString(), SimpleNameResult.class);
					tmp=je.getAsJsonObject();
					out.append("<tr><td data-key=\""+tmp.get("_key").getAsString()+"\"><i>")
						.append(tmp.get("name").getAsString())
						.append("</i></td><td>")
						.append(tmp.get("author").getAsString())
						.append("</td><td><form class=\"poster\" data-path=\"/nodes/delete\"><input type=\"hidden\" name=\"id\" value=\"taxent/"+tmp.get("_key").getAsString()+"\"/><input type=\"submit\" value=\"x\"/></form>");
						//.append("<div class=\"button\">add synonym</div><div class=\"button\">add infrataxon</div></td></tr>");
				}
				out.append("</table></div>");
				break;
				
			}
			return out.toString();
		} catch (FloraOnException e) {
			return "<p>Flora-On error: "+e.getMessage()+"</p>";
		} catch (URISyntaxException e) {
			return "<p>Error processing URL.</p>";
		}
	}*/
}
