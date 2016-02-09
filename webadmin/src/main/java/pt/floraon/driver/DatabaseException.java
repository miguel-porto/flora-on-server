package pt.floraon.driver;

public class DatabaseException extends FloraOnException {
	private static final long serialVersionUID = 1L;
    public DatabaseException () {}
    public DatabaseException (String message) {
       super(message);
    }
}
