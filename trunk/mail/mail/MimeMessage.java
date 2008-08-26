package mail;

import java.io.InputStream;
import java.io.OutputStream;

import mail.exceptions.ParseException;
import mail.util.LineOutputStream;

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
			part = new MimeMultiPart(inputStream, ct, null, null);
		} else {
			part = new MimePart(inputStream, ct, headers, null); 
		}
		
	}	
	
	@Override
	public String toString() {
		String headerString = "";
		if (headers!=null) headerString = "Headers:\n\t" + headers.toString().replace("\n", "\n\t");
		
		return headerString+part.toString(1);
	}
	
	
	public void writeTo(OutputStream os) {
		
		LineOutputStream los = new LineOutputStream(os);
		
		// putting headers
		for (MimeMessageHeader mimeMessageHeader : headers.getHeaders()) {
			los.writeln(mimeMessageHeader.getLine());
		}
		
		los.writeln("");
		part.writeTo(los);
		
		// double CRLF
		los.writeln("");
		los.writeln("");
		//los.writeln(".");
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
