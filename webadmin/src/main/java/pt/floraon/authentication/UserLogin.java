package pt.floraon.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import pt.floraon.driver.FloraOnException;
import pt.floraon.authentication.entities.User;
import pt.floraon.server.FloraOnServlet;

public class UserLogin extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnPost(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
		HttpSession session;
		if(thisRequest.getParameterAsString("logout") != null) {
			if((session = thisRequest.request.getSession(false)) != null) {
				session.removeAttribute("user");
				session.invalidate();
				thisRequest.response.sendRedirect("main");
			}
		} else {
			String username=thisRequest.getParameterAsString("username");
			char[] password=thisRequest.getParameterAsString("password").toCharArray();
			User user = driver.getAdministration().authenticateUser(username, password);

			if(user == null) {
				thisRequest.response.sendRedirect("main?w=login&reason=notfound");
			} else {
			    if(driver.getGlobalSettings().isClosedForAdminTasks() && !user.isAdministrator()) {
					thisRequest.error("Site temporarily closed for administrative tasks");
					return;
				}

				user.clearPassword();
				user.resetEffectivePrivileges();
				session = thisRequest.request.getSession();
				session.setAttribute("user", user);
				session.setAttribute("userName", user.getName());
				thisRequest.response.sendRedirect("main");
			}
		}
	}
}
