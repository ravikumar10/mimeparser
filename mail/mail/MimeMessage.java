package mail;

import java.io.InputStream;

import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeMultipart;

import mail.exceptions.ParseException;

/**
 * Class represends the sense of the project mail message
 * 
 * It's an abstract message that contains root {@link MimeMultipart}
 * inside which points to main information and 
 * headers
 * @author zbychu
 *
 */
public class MimeMessage {
	
	
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
		
		// headers
		headers = new MimeMessageHeaders(inputStream);
		ContentType ct = headers.getContentType();
		if (ct==null) throw new ParseException("No content type in message");
		if (ct.getPrimaryType().equals(MULTIPART_TYPE)) {
			part = new MimeMultiPart(inputStream, headers, null);
		} else {
			part = new MimePart(inputStream, ct, headers, null); 
		}
		
	}	
	
	@Override
	public String toString() {
		//najpierw mozna jeszcze stringnac
		// headery
		String headerString = "";
		if (headers!=null) headerString = "Headers:\n\t" + headers.toString().replace("\n", "\n\t");
		
		return headerString+part.toString(1);
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
