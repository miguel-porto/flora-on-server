package pt.floraon.server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.FloraOn;
import pt.floraon.driver.IListDriver;
import pt.floraon.driver.INodeKey;
import pt.floraon.driver.INodeWorker;

public class FloraOnServlet extends HttpServlet {
	protected static final int PAGESIZE=200;
	protected String territory=null;
	//protected FloraOnDriver graph;
	protected INodeWorker NWD;
	protected IListDriver LD;
	protected FloraOn driver;
	//protected NodeWrapperFactoryInt NWrF;
	//protected TaxEntWrapperFactoryInt TEWrF;
	private static final long serialVersionUID = 2390926316338894377L;
	protected HttpServletResponse response;
	protected HttpServletRequest request;

	public final void init() throws ServletException {
		this.driver = (FloraOn) this.getServletContext().getAttribute("driver");
		this.NWD=this.driver.getNodeWorkerDriver();
		this.LD=this.driver.getListDriver();
		//this.NWD=(NodeWorkerDriverInt) this.getServletContext().getAttribute("NodeWorkerDriver");
		//this.NWrF=(NodeWrapperFactoryInt) this.getServletContext().getAttribute("NodeWrapperFactory");
		//this.TEWrF=(TaxEntWrapperFactoryInt) this.getServletContext().getAttribute("TaxEntWrapperFactory");
	}
	
	protected void error(String obj) throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println("{\"success\":false,\"msg\":\""+obj+"\"}");
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

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(out.toString());
	}

	protected void success(JsonElement obj) throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println("{\"success\":true,\"msg\":"+obj.toString()+"}");
	}

	protected void success(String obj) throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println("{\"success\":true,\"msg\":\""+obj+"\"}");
	}
	
	protected boolean isAuthenticated() {
		return request.getSession().getAttribute("user")!=null;
	}
	/**
	 * Gets the called path, either if it was jsp:included or requested by browser
	 * @param request
	 * @return
	 */
	protected ListIterator<String> getPathIterator() {
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
	
	/**
	 * Gets an iterator of the path parts after the given one. It is guaranteed to exist at least one part, otherwise an exception is thrown.
	 * @param part
	 * @return
	 * @throws FloraOnException
	 */
	protected ListIterator<String> getPathIteratorAfter(String part) throws FloraOnException {
		ListIterator<String> partIt=this.getPathIterator();
		try {
			while(!partIt.next().equals(part));
		} catch(NoSuchElementException e) {
			throw new FloraOnException("Invalid path");
		}
		if(!partIt.hasNext()) throw new FloraOnException("Incomplete path, expecting more options");
		return partIt;
	}

	@Override
	public final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		this.response=response;
		this.request=request;
		try {
			doFloraOnGet();
		} catch (FloraOnException e) {
			error(e.getMessage());
		}
	}

	@Override
	public final void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		this.response=response;
		this.request=request;
		try {
			doFloraOnPost();
		} catch (FloraOnException e) {
			error(e.getMessage());
		}
	}

	public String getParameterAsString(String name) throws IOException, ServletException {
		String tmp;
		if(request.getContentType()==null)
			tmp = request.getParameter(name);
		else if(request.getContentType().contains("multipart/formdata")) {
			tmp = request.getPart("rank")==null ? null : IOUtils.toString(request.getPart("rank").getInputStream(), StandardCharsets.UTF_8);
		} else tmp = request.getParameter(name);
		return tmp;//URLDecoder.decode(tmp, StandardCharsets.UTF_8.name());
	}
	
	public INodeKey getParameterAsKey(String name) throws IOException, ServletException, FloraOnException {
		return driver.asNodeKey(getParameterAsString(name));
	}

	public Integer getParameterAsInteger(String name) throws IOException, ServletException, FloraOnException {
		try {
			return Integer.parseInt(getParameterAsString(name));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public Integer getParameterAsIntegerNoNull(String name) throws IOException, ServletException, FloraOnException {
		try {
			return Integer.parseInt(getParameterAsString(name));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 * @throws FloraOnException
	 */
	public boolean getParameterAsBoolean(String name) throws IOException, ServletException, FloraOnException {
		String tmp=getParameterAsString(name);
		if(tmp==null) return false;				// parameter absent
		if(tmp.trim().length()==0) return true;	// present
		try {
			if(Boolean.parseBoolean(tmp) || Integer.parseInt(tmp)!=0) return true;	// true or not zero
		} catch (NumberFormatException e) {
			return false;		// any other case
		}
		return false;
	}

	public void doFloraOnGet() throws ServletException, IOException, FloraOnException {}
	public void doFloraOnPost() throws ServletException, IOException, FloraOnException {}
}
