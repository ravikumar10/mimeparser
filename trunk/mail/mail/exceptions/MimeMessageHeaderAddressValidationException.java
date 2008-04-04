package mail.exceptions;

public class MimeMessageHeaderAddressValidationException extends RuntimeException {
	
	String reason;
	
	public MimeMessageHeaderAddressValidationException(String reason) {
		this.reason = reason;
	}
	
	public MimeMessageHeaderAddressValidationException(String originalAddress, String reason, int position) {
		
		String highlihter = "";
		for (int i=0; i<position; i++) {
			highlihter+=" ";
		}
		highlihter+="^";
		//reason+="\n" + originalAddress + "\n" + highlihter;
		this.reason=reason+ " : \n" + originalAddress + "\n" + highlihter;
	}
	
	@Override
	public String toString() {
		return reason;
	}
	
}
