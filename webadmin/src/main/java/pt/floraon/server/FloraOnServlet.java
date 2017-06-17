package pt.floraon.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import pt.floraon.driver.*;
import pt.floraon.driver.IFloraOn;
import pt.floraon.authentication.entities.User;

public class FloraOnServlet extends HttpServlet {
	protected static final int PAGESIZE=200;
	protected String territory=null;
	protected INodeWorker NWD;
	protected IListDriver LD;
	protected IFloraOn driver;
	private static final long serialVersionUID = 2390926316338894377L;

	public final void init() throws ServletException {
		this.driver = (IFloraOn) this.getServletContext().getAttribute("driver");
		if(this.driver != null) {
			this.NWD = this.driver.getNodeWorkerDriver();
			this.LD = this.driver.getListDriver();
		}
	}
	
	protected void errorIfAnyNull(Object... pars) throws FloraOnException {
		for(Object o : pars) {
			if(o == null) throw new FloraOnException("Missing parameter.");
		}
	}

	protected void errorIfAllNull(Object... pars) throws FloraOnException {
		Boolean resp=true;
		for(Object o : pars) {
			resp = resp && (o==null);
		}
		if(resp) throw new FloraOnException("Missing parameter.");
	}


	@Override
	public final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setLocale(Locale.getDefault());
//		response.setLocale(request.getLocale());

		if(this.driver == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			request.getRequestDispatcher("/error.html").forward(request, response);
			return;
		}

		ThisRequest thisRequest = new ThisRequest(request, response);

		request.setAttribute("user", thisRequest.getUser());
		request.setAttribute("uuid", "sk51");

		try {
			doFloraOnGet(thisRequest);
		} catch (FloraOnException e) {
			e.printStackTrace();
			thisRequest.error(e.getMessage());
		}
	}

	@Override
	public final void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		if(this.driver == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			request.getRequestDispatcher("/error.html").forward(request, response);
			return;
		}
		ThisRequest thisRequest = new ThisRequest(request, response);
		request.setAttribute("user", thisRequest.getUser());
		try {
			doFloraOnPost(thisRequest);
		} catch (FloraOnException e) {
			e.printStackTrace();
			thisRequest.error(e.getMessage());
		}
	}


	public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {}
	public void doFloraOnPost(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {}

	protected class ThisRequest {
		public final HttpServletResponse response;
		public final HttpServletRequest request;

		public ThisRequest(HttpServletRequest request, HttpServletResponse response) {
			this.response = response;
			this.request = request;
		}

		/**
		 * Gets the parameter as a String or null if the parameter is not present. If the parameter is present but empty, returns an empty string.
		 * @param name
		 * @return
		 * @throws IOException
		 * @throws ServletException
		 */
		public String getParameterAsString(String name) throws IOException, ServletException {
			String tmp;
			if(request.getContentType()==null)
				tmp = request.getParameter(name);
			else if(request.getContentType().contains("multipart/formdata")) {
				tmp = request.getPart(name)==null ? null : IOUtils.toString(request.getPart(name).getInputStream(), StandardCharsets.UTF_8);
			} else tmp = request.getParameter(name);
			return tmp;//URLDecoder.decode(tmp, StandardCharsets.UTF_8.name());
		}

		/**
		 * Gets the parameter as a String array or null if the parameter is not present.
		 * @param name
		 * @return
		 * @throws IOException
		 * @throws ServletException
		 */
		public String[] getParameterAsStringArray(String name) throws IOException, ServletException {
			String[] tmp;
			if(request.getContentType()==null)
				tmp = request.getParameterValues(name);
			else if(request.getContentType().contains("multipart/formdata")) {
				tmp = request.getPart(name)==null ? new String[0] : new String[] {IOUtils.toString(request.getPart(name).getInputStream(), StandardCharsets.UTF_8)};
			} else tmp = request.getParameterValues(name);
			return tmp;
		}

		/**
		 * Gets the parameter value, or defaultvalue if parameter is absent
		 * @param name
		 * @param defaultValue
		 * @return
		 * @throws IOException
		 * @throws ServletException
		 */
		public String getParameterAsString(String name, String defaultValue) throws IOException, ServletException {
			String tmp = getParameterAsString(name);
			return tmp == null ? defaultValue : tmp;
		}

		public INodeKey getParameterAsKey(String name) throws IOException, ServletException, FloraOnException {
			return driver.asNodeKey(getParameterAsString(name));
		}

		public Integer getParameterAsInteger(String name,Integer nullValue) throws IOException, ServletException, FloraOnException {
			try {
				return Integer.parseInt(getParameterAsString(name));
			} catch (NumberFormatException e) {
				return nullValue;
			}
		}

		public int getParameterAsInt(String name) throws IOException, ServletException, FloraOnException {
			try {
				return Integer.parseInt(getParameterAsString(name));
			} catch (NumberFormatException | NullPointerException e) {
				throw new FloraOnException("Invalid parameter value");
			}
		}

		public boolean getParameterAsBooleanNoNull(String name) throws IOException, ServletException, FloraOnException {
			String tmp=getParameterAsString(name);
			if(tmp==null) return false;				// parameter absent
			if(tmp.trim().length()==0) return true;	// present
			if(tmp.equals("on")) return true;		// for HTML checkboxes
			try {
				if(Boolean.parseBoolean(tmp) || Integer.parseInt(tmp)!=0) return true;	// true or not zero
			} catch (NumberFormatException e) {
				return false;		// any other case
			}
			return false;
		}

		public Boolean getParameterAsBoolean(String name) throws IOException, ServletException, FloraOnException {
			String tmp=getParameterAsString(name);
			if(tmp==null) return null;				// parameter absent
			return getParameterAsBooleanNoNull(name);
		}

		public boolean getParameterAsBoolean(String name, boolean nullValue) throws IOException, ServletException, FloraOnException {
			String tmp=getParameterAsString(name);
			if(tmp==null) return nullValue;				// parameter absent
			return getParameterAsBooleanNoNull(name);
		}

		public <T extends Enum<T>> T getParameterAsEnum (String name, Class<T> T) throws IOException, ServletException, FloraOnException {
			T out;
			String value=getParameterAsString(name);
			if(value==null || value.toUpperCase().equals("NULL")) return null;
			try {
				out=Enum.valueOf(T, value);
			} catch (IllegalArgumentException e) {
				throw new FloraOnException("Illegal value: "+value);
			}
			return out;
		}

		/**
		 * Gets the called path, either if it was jsp:included or requested by browser
		 * @return
		 */
		public ListIterator<String> getPathIterator() {
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
		public ListIterator<String> getPathIteratorAfter(String part) throws FloraOnException {
			ListIterator<String> partIt=this.getPathIterator();
			try {
				while(!partIt.next().equals(part));
			} catch(NoSuchElementException e) {
				throw new FloraOnException("Invalid path");
			}
			if(!partIt.hasNext()) throw new FloraOnException("Incomplete path, expecting more options");
			return partIt;
		}

		/**
		 * Reloads user data from the database
		 */
		public User refreshUser() {
			try {
				User user = driver.getNodeWorkerDriver().getNode(driver.asNodeKey(getUser().getID()), User.class);
				user.clearPassword();
				user.resetEffectivePrivileges();
				request.getSession().setAttribute("user", user);
				return user;
			} catch (FloraOnException e) {
				return null;
			}
		}

		public User getUser() {
			HttpSession session = request.getSession(false);

			if(session == null || session.getAttribute("user") == null) {
				try {
					return User.guest();
				} catch (DatabaseException e) {
					e.printStackTrace();
					return null;
				}
			} else
				return (User) session.getAttribute("user");
		}

		public void error(String obj) throws IOException {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			PrintWriter pw = response.getWriter();
			JsonObject resp = new JsonObject();
			resp.addProperty("success", false);
			resp.addProperty("msg", obj);
			pw.print(resp.toString());
//		pw.println("{\"success\":false,\"msg\":\""+obj+"\"}");
			pw.flush();
		}

		protected void errorHTML(String obj) throws IOException {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			PrintWriter pw = response.getWriter();
			pw.println("<html><body>" + obj + "</body></html>");
			pw.flush();
		}

		public void success(JsonElement obj, JsonObject header) throws IOException {
			JsonObject out;
			if(header==null)
				out=new JsonObject();
			else
				out=header;
			out.addProperty("success", true);
			out.add("msg", obj);

			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			PrintWriter pw = response.getWriter();
			pw.println(out.toString());
			pw.flush();
		}

		public void success(Object obj) throws IOException {
			if(JsonObject.class.isAssignableFrom(obj.getClass())) {
				success((JsonObject) obj);
				return;
			}
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			PrintWriter pw = response.getWriter();
			JsonObject resp = new JsonObject();
			resp.addProperty("success", true);
			resp.addProperty("msg", obj.toString());
			pw.print(resp.toString());
			pw.flush();
		}

		protected void success(JsonObject obj) throws IOException {
			success((JsonElement) obj);
		}

		protected void success(JsonElement obj) throws IOException {
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			PrintWriter pw = response.getWriter();
			JsonObject resp = new JsonObject();
			resp.addProperty("success", true);
			resp.add("msg", obj);
			pw.print(resp.toString());
			pw.flush();
		}

		public void success(String obj, boolean alert) throws IOException {
			if(!alert)
				success(obj);
			else {
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				JsonObject resp = new JsonObject();
				JsonObject obj1 = new JsonObject();
				resp.addProperty("success", true);
				obj1.addProperty("alert", true);
				obj1.addProperty("text", obj);
				resp.add("msg", obj1);

				PrintWriter pw = response.getWriter();
				pw.println(resp.toString());
				pw.flush();
			}
		}

	}
}
