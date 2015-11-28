package pt.floraon.dbworker;

public class FloraOnException extends Exception {
	private static final long serialVersionUID = 1L;
    public FloraOnException() {}
    public FloraOnException(String message) {
       super(message);
    }
}