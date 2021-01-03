package pt.floraon.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import pt.floraon.authentication.Privileges;
import pt.floraon.driver.*;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.interfaces.IListDriver;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.interfaces.INodeWorker;
import pt.floraon.geometry.IPolygonTheme;
import pt.floraon.geometry.gridmaps.ISquare;
import pt.floraon.geometry.gridmaps.ListOfTaxa;
import pt.floraon.geometry.gridmaps.SquareData;
import pt.floraon.redlistdata.GridMapExporter;
import pt.floraon.redlistdata.entities.RedListSettings;

public class FloraOnServlet extends HttpServlet {
	protected static final int PAGESIZE=200;
//	protected String territory=null;
	protected INodeWorker NWD;
	protected IListDriver LD;
	protected IFloraOn driver;
	private static final long serialVersionUID = 2390926316338894377L;

	@Override
	public final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setLocale(Locale.getDefault());
//		response.setLocale(request.getLocale());

		if(this.driver == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			request.setAttribute("error", "Some unexpected error occurred, check the TomCat logs.");
			request.getRequestDispatcher("/error.jsp").forward(request, response);
			return;
		}

		if(this.driver.hasFailed()) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			request.setAttribute("error", this.driver.getErrorMessage());
			request.getRequestDispatcher("/error.jsp").forward(request, response);
			return;
		}

		ThisRequest thisRequest = new ThisRequest(request, response);

//		thisRequest.getUser().resetEffectivePrivileges();
		request.setAttribute("user", thisRequest.getUser());
		request.setAttribute("uuid", "bk24");
		request.setAttribute("contextPath", driver.getContextPath());
		request.setAttribute("offline", false);

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
			request.setAttribute("error", "Some unexpected error occurred, check the TomCat logs.");
			request.getRequestDispatcher("/error.jsp").forward(request, response);
			return;
		}

		if(this.driver.hasFailed()) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			request.setAttribute("error", this.driver.getErrorMessage());
			request.getRequestDispatcher("/error.jsp").forward(request, response);
			return;
		}

		ThisRequest thisRequest = new ThisRequest(request, response);
//		thisRequest.getUser().resetEffectivePrivileges();
		request.setAttribute("contextPath", driver.getContextPath());
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

	public final void init() {
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
		boolean resp = true;
		for(Object o : pars) {
			resp = resp && (o==null);
		}
		if(resp) throw new FloraOnException("Missing parameter.");
	}


	protected class ThisRequest {
		public final HttpServletResponse response;
		public final HttpServletRequest request;

		public ThisRequest(HttpServletRequest request, HttpServletResponse response) {
			this.response = response;
			this.request = request;
		}

		public void setOption(String optionName, Object value) throws FloraOnException {
			HttpSession session = request.getSession(false);
			if(session != null) {
//                    System.out.println("SET " + "option-" + optionName + " to "+thisRequest.getParameterAsBooleanNoNull("v"));
				session.setAttribute("option-" + optionName, value);
			} else
				throw new FloraOnException("Not logged in");

		}

		public boolean isOptionSet(String optionName) throws FloraOnException {
			HttpSession session = request.getSession(false);
			if(session != null)
				return session.getAttribute("option-" + optionName) != null;
			else
				throw new FloraOnException("Not logged in");
		}

		public Object getOption(String optionName) throws FloraOnException {
            HttpSession session = request.getSession(false);
			if(session != null)
            	return session.getAttribute("option-" + optionName);
			else
				throw new FloraOnException("Not logged in");
        }

		public boolean isOptionTrue(String optionName) throws FloraOnException {
			HttpSession session = request.getSession(false);
			if(session != null)
				return session.getAttribute("option-" + optionName) != null
						&& (Boolean) session.getAttribute("option-" + optionName);
			else
				throw new FloraOnException("Not logged in");
		}

		public boolean isQueryParameterEqualTo(String name, String value) throws IOException, ServletException {
			if(value == null && getParameterAsString(name) == null)
				return true;
			return value != null && value.equals(getParameterAsString(name));
		}

		public boolean isQueryParameterSet(String name) throws IOException, ServletException {
			return getParameterAsString(name) != null;
		}

		/**
		 * @param name
		 * @return The parameter as a String or null if the parameter is not present. If the parameter is present but empty, returns an empty string.
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

		public Integer getParameterAsInteger(String name,Integer nullValue) throws IOException, ServletException {
			try {
				return Integer.parseInt(getParameterAsString(name));
			} catch (NumberFormatException e) {
				return nullValue;
			}
		}

		public Float getParameterAsFloat(String name, Float nullValue) throws IOException, ServletException {
			try {
				return Float.parseFloat(getParameterAsString(name));
			} catch (NumberFormatException e) {
				return nullValue;
			}
		}

		public Long getParameterAsLong(String name, Long nullValue) throws IOException, ServletException {
			try {
				return Long.parseLong(getParameterAsString(name));
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

		public Date getParameterAsDate(String name) throws IOException, ServletException {
			try {
				return Constants.dateFormatYMD.get().parse(getParameterAsString(name));
			} catch (ParseException e) {
				return null;
			}
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
				User user = driver.getNodeWorkerDriver().getDocument(driver.asNodeKey(getUser().getID()), User.class);
				user.clearPassword();
				user.resetEffectivePrivileges();
				request.getSession().setAttribute("user", user);
                request.setAttribute("user", user);
				return user;
			} catch (FloraOnException e) {
				return null;
			}
		}

		public User getUser() {
			HttpSession session = request.getSession(false);

			if(session == null || session.getAttribute("user") == null) {
				return User.guest();
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

		public void errorServer(String msg) throws IOException {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
		}

		protected void errorHTML(String obj) throws IOException {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			PrintWriter pw = response.getWriter();
			pw.println("<html><body>" + obj + "</body></html>");
			pw.flush();
		}

		public void setCacheHeaders(int minutes) {
			Calendar time = Calendar.getInstance();
			time.add(Calendar.MINUTE, minutes);
			response.setDateHeader("Expires", time.getTimeInMillis());
			response.setHeader("Cache-Control", "max-age=" + (minutes * 60));
		}

		public void setNoCache() {
			response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");
			response.setHeader("Pragma", "no-cache");
		}

		public void setDownloadFileName(String fileName) {
			response.addHeader("Content-Disposition"
					, String.format("attachment;Filename=\"%s\"", fileName));
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
			response.setStatus(HttpServletResponse.SC_OK);
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
			response.setStatus(HttpServletResponse.SC_OK);
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
				response.setStatus(HttpServletResponse.SC_OK);
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

		public void forbidden(String s) {
			try {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, s);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void ensurePrivilege(Privileges privilege) throws FloraOnException {
			ensurePrivilege(privilege, "You don't have privileges for this operation");
		}

		public void ensurePrivilege(Privileges privilege, String message) throws FloraOnException {
			if(!this.getUser().hasPrivilege(privilege))
				throw new FloraOnException(message);
		}

		public void ensureAdministrator() throws FloraOnException {
			ensureAdministrator("Reserved for administrators.");
		}

		public void ensureAdministrator(String message) throws FloraOnException {
			if(!this.getUser().isAdministrator())
				throw new FloraOnException(message);
		}

		/**
		 * Forwards the request to the SVG map creator.
		 * @param processor
		 * @param territory
		 * @param showBaseMap
		 * @param borderWidth Ignored if standAlone is false
		 * @param showShadow
		 * @param protectedAreas
		 * @param standAlone
		 * @param showOccurrences
		 * @param scaleStroke TRUE to use absolute values, in viewBox units, for stroke-width.
		 * @throws ServletException
		 * @throws IOException
		 */
		public void includeSVGMap(GridMapExporter processor, String territory, boolean showBaseMap, int borderWidth
				, boolean showShadow, IPolygonTheme protectedAreas, boolean standAlone, boolean showOccurrences
                , boolean scaleStroke, String squareFill) throws ServletException, IOException {
			setSVGMapVariables(processor, territory, showBaseMap, borderWidth, showShadow, protectedAreas, standAlone
                    , showOccurrences, scaleStroke, squareFill);
            this.request.getRequestDispatcher("/fragments/frag-basemapsvg.jsp")
                    .include(this.request, this.response);
        }

		/**
		 * Export a map in CSV, aggregated by MGRS squares
		 * @param writer
		 * @param processor
		 * @throws ServletException
		 * @throws IOException
		 */
		public void exportSVGMapAsCSV(PrintWriter writer, GridMapExporter processor) throws ServletException, IOException {
			CSVPrinter csvp = new CSVPrinter(writer, CSVFormat.DEFAULT);
			csvp.printRecord("MGRS", "WKT", "Taxa", "NrTaxa");
			for (Map.Entry<? extends ISquare, SquareData> sq : processor.squares()) {
				csvp.printRecord(sq.getKey().getMGRS(), sq.getKey().toWKT(), ((ListOfTaxa) sq.getValue()).getText("+"), sq.getValue().getNumber());
			}
			csvp.close();
		}

		/**
		 * Exports an SVG map to the given writer.
		 * @param writer
		 * @param processor
		 * @param territory
		 * @param showBaseMap
		 * @param borderWidth
		 * @param showShadow Show a drop shadow in the map border
		 * @param protectedAreas
		 * @param standAlone
		 * @param showOccurrences
		 * @throws ServletException
		 * @throws IOException
		 */
        public void exportSVGMap(PrintWriter writer, GridMapExporter processor, String territory, boolean showBaseMap
				, float borderWidth, boolean showShadow, IPolygonTheme protectedAreas, boolean standAlone
				, boolean showOccurrences, boolean scaleStroke, String squareFill) throws ServletException, IOException {
			setSVGMapVariables(processor, territory, showBaseMap, borderWidth, showShadow, protectedAreas, standAlone
                    , showOccurrences, scaleStroke, squareFill);
			HttpServletResponse2Writer customResponse = new HttpServletResponse2Writer(this.response, writer);
			this.request.getRequestDispatcher("/fragments/frag-basemapsvg.jsp").forward(this.request, customResponse);
//			return customResponse.getOutput();
		}

        /**
         * Prepare request attributes for rendering SVG map.
         * @param processor
         * @param territory
         * @param showBaseMap
         * @param borderWidth
         * @param showShadow
         * @param protectedAreas
         * @param standAlone
         * @param showOccurrences
         */
		private void setSVGMapVariables(GridMapExporter processor, String territory, boolean showBaseMap, float borderWidth
				, boolean showShadow, IPolygonTheme protectedAreas, boolean standAlone, boolean showOccurrences, boolean scaleStroke
                , String squareFill) {
			RedListSettings redListSettings = driver.getRedListSettings(territory);
			this.request.setAttribute("mapBounds", redListSettings.getMapBounds());
			this.request.setAttribute("svgDivisor", redListSettings.getSvgMapDivisor());
			this.request.setAttribute("baseMap", redListSettings.getBaseMapPathString());
			if(processor != null) this.request.setAttribute("squares", processor.squares());
			this.request.setAttribute("showBaseMap", showBaseMap);
			this.request.setAttribute("showShadow", showShadow);
			this.request.setAttribute("standAlone", standAlone);
			this.request.setAttribute("borderWidth", borderWidth);
			this.request.setAttribute("scaleStroke", scaleStroke);
			this.request.setAttribute("showOccurrences", showOccurrences);
			this.request.setAttribute("squareFill", squareFill);
			if(protectedAreas != null) this.request.setAttribute("protectedAreas", protectedAreas.iterator());
		}
	}
}
