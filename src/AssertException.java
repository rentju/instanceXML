public class AssertException extends RuntimeException {
	private static final long serialVersionUID = 33007388563244401L;

	public AssertException(String s) {
		super(s);
	}

	public AssertException(String s, Throwable t) {
		super(s, t);
	}
}