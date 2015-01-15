package cz.mzk.recordmanager.server.marc;

public class InvalidMarcException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidMarcException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidMarcException(String message) {
		super(message);
	}

	public InvalidMarcException(Throwable cause) {
		super(cause);
	}	

}
