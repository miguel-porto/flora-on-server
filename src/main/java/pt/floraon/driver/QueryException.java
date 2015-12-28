package pt.floraon.driver;

public class QueryException extends FloraOnException {
	private static final long serialVersionUID = 1L;
    public QueryException() {}
    public QueryException(String message) {
       super(message);
    }
}