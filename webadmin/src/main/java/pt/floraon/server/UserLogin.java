package pt.floraon.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import pt.floraon.driver.DatabaseException;
import pt.floraon.driver.FloraOnException;
import pt.floraon.entities.User;

public class UserLogin extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnPost() throws ServletException, IOException, FloraOnException {
		if(getParameterAsString("logout") != null) {
			request.getSession().removeAttribute("user");
			response.sendRedirect("main");
		} else {
			String username=getParameterAsString("username");
			char[] password=getParameterAsString("password").toCharArray();
			User user = driver.getAdministration().authenticateUser(username, password);

			if(user == null) {
				response.sendRedirect("main?w=login&reason=notfound");
			} else {
				user.clearPassword();
				request.getSession().setAttribute("user", user);
				response.sendRedirect("main");
			}
		}
	}
}
