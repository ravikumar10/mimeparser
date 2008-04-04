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
	
	MimeMultiPart multipart;
	Preambula preambula;
	MimeMessageHeaders headers;
	final String BOUNDARY = "boundary";
	String boundary;
	
	/**
	 * creates mimeMessage from inputStream by parsing headers and message body
	 * @param inputStream
	 * @throws ParseException 
	 */
	public void createMimeMessage(InputStream inputStream) throws ParseException {
		
		headers = new MimeMessageHeaders(inputStream);
		getBoundaryLine(headers.getContentType());
		preambula = new Preambula(inputStream, this.boundary);
		multipart = new MimeMultiPart(inputStream, headers.getContentType(), preambula.isPreambuleABondary()?preambula.getPreamble():this.boundary);
		
		
	}
	
	public void getBoundaryLine(ContentType contentType) throws ParseException {
		
		String boundary = null;
		if (contentType!=null) {
			boundary = contentType.getParameter(this.BOUNDARY);
			if (boundary!=null) {
				this.boundary = "--" + boundary;
			} else {
				throw new ParseException("No boundary");
			}
		} else {
			throw new ParseException("No content type in message");
			// moze juz na wczesniejszym etapie rzucac ten wyjatek?
		}
		
	}
	
	
	
}
