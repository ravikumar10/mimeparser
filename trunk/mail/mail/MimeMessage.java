package mail;

import java.io.InputStream;

import mail.exceptions.ParseException;

/**
 * Class represends the sense of the project mail message
 * according to Mime standards - it contains some 
 * headers and mime parts (it can be of course one mime type
 * f.e. text/plain - without any boundaries)
 * @author zbychu
 *
 */
public class MimeMessage {
	
	final String BOUNDARY = "boundary";
	final String MULTIPART_TYPE = "multipart";
	
	/**
	 * can be single part or multipart
	 * it's some kind of root part
	 */
	Part part;
	
	MimeMessageHeaders headers;
	String boundary;
	
	public MimeMessage() {}
	
	public MimeMessage(InputStream inputStream) throws ParseException {
		createMimeMessage(inputStream);
	}
	
	/**
	 * creates mimeMessage from inputStream by parsing headers and message body
	 * @param inputStream
	 * @throws ParseException 
	 */
	public void createMimeMessage(InputStream inputStream) throws ParseException {
		
		headers = new MimeMessageHeaders(inputStream);
		ContentType ct = headers.getContentType();
		
		if (ct==null) throw new ParseException("No content type in message");
		
		if (ct.getPrimaryType().equals(MULTIPART_TYPE)) {
			getBoundaryLine(ct);
			part = new MimeMultiPart(inputStream, ct, this.boundary);
		} else {
			part = new MimePart(inputStream, ct); 
		}
		
		 
	}
	
	public void getBoundaryLine(ContentType contentType) throws ParseException {
		
		String boundary = null;
		if (contentType!=null) {
			boundary = contentType.getParameter(this.BOUNDARY);
			if (boundary!=null) {
				//TODO!! nie wszystkie maile sa takie fajne ze maja boundary 
				// z -- dodatkowym
				this.boundary = "--" + boundary;
			} 
		} 
	}

	public Part getPart() {
		return part;
	}

	public void setPart(Part part) {
		this.part = part;
	}

	public MimeMessageHeaders getHeaders() {
		return headers;
	}

	public void setHeaders(MimeMessageHeaders headers) {
		this.headers = headers;
	}
	
}
