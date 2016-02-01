package pt.floraon.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class UserLogin extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getParameter("logout")!=null) {
			request.getSession().removeAttribute("user");
			response.sendRedirect("admin?w=login");
		} else {
			String username=request.getParameter("username");
			String password=request.getParameter("password");
			Document userDB=null;
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				userDB = dBuilder.parse(this.getClass().getResourceAsStream("/users.xml"));
				userDB.getDocumentElement().normalize();
			} catch (ParserConfigurationException | SAXException | IOException e) {
				e.printStackTrace();
				response.sendRedirect("admin");
				return;
			}
			
			NodeList users=userDB.getElementsByTagName("user");
			for(int i=0;i<users.getLength();i++) {
				if(((Element)users.item(i)).getAttribute("name").equals(username)
						&& ((Element)users.item(i)).getAttribute("password").equals(password)) {
					request.getSession().setAttribute("user", new User(username, ((Element)users.item(i)).getAttribute("role")));
					//request.getSession().setAttribute("message", ((Element)users.item(i)).getAttribute("name"));
					break;
				}
			}
			response.sendRedirect("admin?w=login&reason=notfound");
		}
		//request.getRequestDispatcher("/main.jsp").forward(request, response);
	}
}
