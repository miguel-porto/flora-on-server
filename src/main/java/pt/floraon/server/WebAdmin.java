package pt.floraon.server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

import com.arangodb.ArangoException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import pt.floraon.driver.FloraOnGraph;

import static pt.floraon.server.ServerDispatch.*;
/**
 * Handles the request to the web admin pages... a hacky implementation of a dynamic web server!
 * @author miguel
 *
 */
public final class WebAdmin {
	static void processRequest(URI url,OutputStream ostr,FloraOnGraph graph) throws FileNotFoundException, IOException, ArangoException {
		PrintWriter out=null;
    	String command=url.getPath();
    	String[] path=command.split("/");
		if(path.length<3) path=new String[] {"","admin","index.html"};
		HttpResponse res=new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP",1,1),200,""));
		Pattern ext=Pattern.compile("\\.([a-zA-Z]+)$",Pattern.CASE_INSENSITIVE);
		Matcher match=ext.matcher(path[path.length-1].trim());
		boolean processFile=false;
		if(match.find()) {
			switch(match.group(1)) {
			case "html":
			case "htm":
				out = new PrintWriter(new OutputStreamWriter(ostr, StandardCharsets.UTF_8), true);
				res.addHeader(new BasicHeader("Content-Type:","text/html"));
				processFile=true;
				break;
			case "png":
				out = new PrintWriter(ostr);
				res.addHeader(new BasicHeader("Content-Type:","image/png"));
				break;
			case "js":
				out = new PrintWriter(ostr);
				res.addHeader(new BasicHeader("Content-Type:","application/javascript"));
				break;
			case "css":
				out = new PrintWriter(ostr);
				res.addHeader(new BasicHeader("Content-Type:","text/css"));
				break;
			case "csv":
				out = new PrintWriter(ostr);
				res.addHeader(new BasicHeader("Content-Type:","text/csv; charset=Windows-1252"));
				res.addHeader(new BasicHeader("Content-Disposition:","attachment;Filename=\"Checklist_flora_Portugal.csv\""));
				break;
			default:
				out = new PrintWriter(new OutputStreamWriter(ostr, StandardCharsets.UTF_8), true);
				res.addHeader(new BasicHeader("Content-Type:","text/html"));
				break;
			}
		} else {
			out = new PrintWriter(new OutputStreamWriter(ostr, StandardCharsets.UTF_8), true);
			res.addHeader(new BasicHeader("Content-Type:","text/html"));
		}
		// get querystring
		List<NameValuePair> params=URLEncodedUtils.parse(url,Charset.defaultCharset().toString());
		out.print(res.toString()+"\r\n");
		out.print("\r\n");
		out.flush();
		File page;
		String address;
		if(path.length==3)
			address="web/"+path[2];
		else
			address="web/"+path[2]+"/"+path[3];
		page=new File(address);
		
		if(processFile) {
			BufferedReader br=new BufferedReader(new FileReader(page));
			String line;
			while((line=br.readLine())!=null) {
				if(line.contains("<!-- CONTENT -->")) {
					line=line.replace("<!-- CONTENT -->", makePageContent(params,graph));
				}
				out.println(line);
			}
			br.close();
		} else {
			try {
				IOUtils.copy(new FileInputStream(page), ostr);
			} catch (FileNotFoundException e) {
				if(address.equals("web/checklist.csv")) {
					ByteArrayOutputStream  baos=new ByteArrayOutputStream();
					try {
						ServerDispatch.processCommand("lists/checklist?fmt=csv", graph, baos);
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
		out.close();
		return;
	}
	
	static String makePageContent(List<NameValuePair> params,FloraOnGraph graph) throws ArangoException, IOException {
		String what=getQSValue("w",params);
		String fmt=getQSValue("fmt",params);
		StringBuilder out=new StringBuilder();
		ByteArrayOutputStream baos;
		try {
			switch(what) {		// the 'w' parameter of the URL querystring
			case "main":
				baos=new ByteArrayOutputStream();
				ServerDispatch.processCommand("lists/checklist?fmt=html", graph, baos);
				baos.close();
				
				out.append("<div id=\"main\" class=\"checklist\">");
				out.append(baos.toString());
				out.append("</div>");
				return out.toString();

			case "species":
				out.append("<div id=\"main\" class=\"species\">");
				out.append("<div class=\"togglevis\"><h3>Add new taxon</h3><div class=\"content\">");
				out.append("<p>Taxon name: <input type=\"text\" name=\"taxonname\"/> Authority: <input type=\"text\" name=\"taxonauth\"/> Rank: ");
				baos=new ByteArrayOutputStream();
				ServerDispatch.processCommand("reference/ranks", graph, baos);
				out.append(baos.toString(StandardCharsets.UTF_8.name()));
				baos.close();
				out.append(" <input type=\"button\" value=\"Add\"/></p>");
				out.append("</div></div>");
				
				baos=new ByteArrayOutputStream();
				ServerDispatch.processCommand("lists/species?fmt=json", graph, baos);
				baos.close();
				out.append("<table>");
				JsonObject res=(new JsonParser().parse(baos.toString())).getAsJsonObject(),tmp;
				JsonArray spp=res.get("msg").getAsJsonArray();
				for(JsonElement je : spp) {
					tmp=je.getAsJsonObject();
					out.append("<tr><td><i>")
						.append(tmp.get("name").getAsString())
						.append("</i></td><td>")
						.append(tmp.get("author").getAsString())
						.append("</td><td><div class=\"button round\">x</div><div class=\"button\">add synonym</div><div class=\"button\">add infrataxon</div></td></tr>");
				}
				out.append("</table></div>");
				return out.toString();
	
			default:
				return "<p>Page not found.</p>";
			}
		} catch (FloraOnException e) {
			return "<p>Flora-On error: "+e.getMessage()+"</p>";
		} catch (URISyntaxException e) {
			return "<p>Error processing URL.</p>";
		}
	}
}
