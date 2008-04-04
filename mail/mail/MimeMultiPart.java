package mail;

import java.io.InputStream;
import java.util.List;

import mail.exceptions.ParseException;
import mail.util.LineInputStream;

/**
 * Class represends the part of the e-mail message in mime format
 * Mime Part can contains more parts (it can be f.e. multipart/alternative
 * or can be simple type like image/jpeg or text/plain or text/hmtl (most common)
 *
 * @author zbychu
 *
 */

public class MimeMultiPart extends Part {
	
	/**
	 * part may contains other multiparts or can be simple part
	 */
	List<Part> parts;
	
	/**
	 * our super part (parent) null if we are top part
	 */
	MimeMultiPart parent;
	
	/**
	 * 
	 */
	InputStream content;
	
	/**
	 * simple content type 
	 */
	ContentType contentType;
	
	/**
	 * boundary line is boundary get from ContentType + "--"
	 * or if it's not present in ContentType it's
	 * first line of preambula
	 */
	String boundaryLine;
	
	/**
	 * Main method resposible for parsing messages
	 * @throws ParseException - zmienic to na jakis inny exception 
	 */
	public void parse() throws ParseException {
		
		LineInputStream lis = new LineInputStream(this.content);
		
		
		
		
		
		
		
	}
	
	public MimeMultiPart() {}
	
	public MimeMultiPart(InputStream inputStream) {
		this.content=inputStream;
	}
	
	public MimeMultiPart(InputStream inputStream, ContentType contentType, String boundary) {
		this.contentType=contentType;
		this.content=content;
		this.boundaryLine=boundaryLine;
		//boundary line
		try {
			parse();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			// TODO Na razie tak - trzeba sie zastanowic co z tym zrobic
			e.printStackTrace();
		}
	}
}
