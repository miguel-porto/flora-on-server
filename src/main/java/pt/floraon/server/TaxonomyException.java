package pt.floraon.server;

public class TaxonomyException extends FloraOnException {
	private static final long serialVersionUID = 1L;
    public TaxonomyException () {}
    public TaxonomyException (String message) {
       super(message);
    }

}