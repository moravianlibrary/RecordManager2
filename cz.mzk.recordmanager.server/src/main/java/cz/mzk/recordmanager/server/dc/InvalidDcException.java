package cz.mzk.recordmanager.server.dc;

public class InvalidDcException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidDcException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidDcException(String message) {
		super(message);
	}

	public InvalidDcException(Throwable cause) {
		super(cause);
	}
	
}
