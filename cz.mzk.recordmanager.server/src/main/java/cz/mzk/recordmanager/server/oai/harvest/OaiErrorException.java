package cz.mzk.recordmanager.server.oai.harvest;

public class OaiErrorException extends Exception {

	private static final long serialVersionUID = 1L;

	public OaiErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	public OaiErrorException(String message) {
		super(message);
	}
}
