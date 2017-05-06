package pt.floraon.authentication;

import java.io.IOException;

import javax.servlet.ServletException;

import pt.floraon.driver.FloraOnException;
import pt.floraon.authentication.entities.User;
import pt.floraon.server.FloraOnServlet;

public class UserLogin extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnPost() throws ServletException, IOException, FloraOnException {
		if(getParameterAsString("logout") != null) {
			if(request.getSession(false) != null) {
				request.getSession().removeAttribute("user");
				request.getSession().invalidate();
				response.sendRedirect("main");
				return;
			}
		} else {
			String username=getParameterAsString("username");
			char[] password=getParameterAsString("password").toCharArray();
			User user = driver.getAdministration().authenticateUser(username, password);

			if(user == null) {
				response.sendRedirect("main?w=login&reason=notfound");
			} else {
				user.clearPassword();
				user.resetEffectivePrivileges();
				request.getSession().setAttribute("user", user);
				request.getSession().setAttribute("userName", user.getName());
				response.sendRedirect("main");
			}
		}
	}
}
