package pt.floraon.server;

public class User {
	public enum Role {
		BASIC(10)
		, INTERMEDIATE(20)
		, ADVANCED(30)
		, ADMINISTRATOR(40);

		private int value;

		Role(int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}
	}

	private String username;
	private Role role;

	public String getUsername() {
		return username;
	}

	public int getRole() {
		return role.getValue();
	}
	
	public User(String username, Role role) {
		this.username=username;
		this.role=role;
	}
}
