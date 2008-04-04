package mail.exceptions;

public class MimeMessageHeaderException extends RuntimeException {
	
	/**
	 * serialization uid
	 */
	private static final long serialVersionUID = 3786534009557180011L;
	String reason;
	
	public MimeMessageHeaderException(String reason) {
		this.reason = reason;
	}
	
	@Override
	public String toString() {
		return reason;
	}
	
	
}
