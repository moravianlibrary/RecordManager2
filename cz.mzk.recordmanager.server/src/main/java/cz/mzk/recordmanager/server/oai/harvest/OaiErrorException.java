package cz.mzk.recordmanager.server.oai.harvest;

public class OaiErrorException extends Exception {

	public OaiErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	public OaiErrorException(String message) {
		super(message);
	}
}
