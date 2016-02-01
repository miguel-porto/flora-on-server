package pt.floraon.server;

public class User {
	private String username, role;

	public String getUsername() {
		return username;
	}

	public String getRole() {
		return role;
	}
	
	public User(String username, String role) {
		this.username=username;
		this.role=role;
	}
}
