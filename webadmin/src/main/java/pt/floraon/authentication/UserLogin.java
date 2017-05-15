package pt.floraon.authentication;

import java.io.IOException;

import javax.servlet.ServletException;

import pt.floraon.driver.FloraOnException;
import pt.floraon.authentication.entities.User;
import pt.floraon.server.FloraOnServlet;

public class UserLogin extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnPost(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
		if(thisRequest.getParameterAsString("logout") != null) {
			if(thisRequest.request.getSession(false) != null) {
				thisRequest.request.getSession().removeAttribute("user");
				thisRequest.request.getSession().invalidate();
				thisRequest.response.sendRedirect("main");
				return;
			}
		} else {
			String username=thisRequest.getParameterAsString("username");
			char[] password=thisRequest.getParameterAsString("password").toCharArray();
			User user = driver.getAdministration().authenticateUser(username, password);

			if(user == null) {
				thisRequest.response.sendRedirect("main?w=login&reason=notfound");
			} else {
				user.clearPassword();
				user.resetEffectivePrivileges();
				thisRequest.request.getSession().setAttribute("user", user);
				thisRequest.request.getSession().setAttribute("userName", user.getName());
				thisRequest.response.sendRedirect("main");
			}
		}
	}
}
