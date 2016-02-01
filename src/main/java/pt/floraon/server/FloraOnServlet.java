package pt.floraon.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.arangodb.ArangoException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import pt.floraon.driver.FloraOnDriver;
import pt.floraon.driver.FloraOnException;

public class FloraOnServlet extends HttpServlet {
	protected static final int PAGESIZE=200;
	protected String territory=null;
	protected FloraOnDriver graph;
	private static final long serialVersionUID = 2390926316338894377L;
	private HttpServletResponse thisResponse;
	private HttpServletRequest thisRequest;
	
	protected void error(String obj) throws IOException {
		thisResponse.setContentType("application/json");
		thisResponse.setCharacterEncoding("UTF-8");
		thisResponse.getWriter().println("{\"success\":false,\"msg\":\""+obj+"\"}");
	}
	
	protected boolean errorIfAnyNull(Object... pars) throws IOException {
		for(Object o : pars) {
			if(o == null) {error("Missing parameter.");return true;}
		}
		return false;
	}

	protected boolean errorIfAllNull(Object... pars) throws IOException {
		Boolean resp=true;
		for(Object o : pars) {
			resp = resp && (o==null);
		}
		if(resp) {error("Missing parameter.");return true;}
		return false;
	}

	protected void success(JsonElement obj,JsonObject header) throws IOException {
		JsonObject out;
		if(header==null)
			out=new JsonObject();
		else
			out=header;
		out.addProperty("success", true);
		out.add("msg", obj);

		thisResponse.setContentType("application/json");
		thisResponse.setCharacterEncoding("UTF-8");
		thisResponse.getWriter().println(out.toString());
	}

	protected void success(JsonElement obj) throws IOException {
		thisResponse.setContentType("application/json");
		thisResponse.setCharacterEncoding("UTF-8");
		thisResponse.getWriter().println("{\"success\":true,\"msg\":"+obj.toString()+"}");
	}

	protected void success(String obj) throws IOException {
		thisResponse.setContentType("application/json");
		thisResponse.setCharacterEncoding("UTF-8");
		thisResponse.getWriter().println("{\"success\":true,\"msg\":\""+obj+"\"}");
	}
	
	protected boolean isAuthenticated(HttpServletRequest request) {
		return request.getSession().getAttribute("user")!=null;
	}
	/**
	 * Gets the called path, either if it was jsp:included or requested by browser
	 * @param request
	 * @return
	 */
	protected ListIterator<String> getPathIterator(HttpServletRequest request) {
		List<String> tmp;
		Object tmp1=request.getAttribute("javax.servlet.include.request_uri");
		if(tmp1!=null)
			tmp=Arrays.asList(tmp1.toString().split("/"));
		else
			tmp=Arrays.asList(request.getRequestURI().split("/"));
		/*   		if(tmp.get(2).startsWith("_"))		// check whether we want to set the universe to a given territory or not
   			territory=tmp.get(2).replaceFirst("_", "");
   		else
   			territory=null;*/
		
		ListIterator<String> it=tmp.listIterator();//  subList(territory==null ? 4 : 5, tmp.size()).listIterator();
		return it;
	}

	public void init() throws ServletException {
		this.graph=(FloraOnDriver) this.getServletContext().getAttribute("graph");
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		this.thisResponse=response;
		this.thisRequest=request;
		try {
			doFloraOnGet(request, response);
		} catch (ArangoException | FloraOnException e) {
			error(e.getMessage());
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		this.thisResponse=response;
		this.thisRequest=request;
		try {
			doFloraOnPost(request, response);
		} catch (ArangoException | FloraOnException e) {
			error(e.getMessage());
		}
	}

	public String getParameter(HttpServletRequest request, String name) throws IOException, ServletException {
		String tmp;
		if(thisRequest.getContentType()==null)
			tmp = thisRequest.getParameter(name);
		else if(thisRequest.getContentType().contains("multipart/formdata")) {
			tmp = thisRequest.getPart("rank")==null ? null : IOUtils.toString(thisRequest.getPart("rank").getInputStream(), StandardCharsets.UTF_8);
		} else tmp = thisRequest.getParameter(name);
		return tmp;//URLDecoder.decode(tmp, StandardCharsets.UTF_8.name());
	}
	
	public void doFloraOnGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ArangoException, FloraOnException {}
	public void doFloraOnPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ArangoException, FloraOnException {}
}
