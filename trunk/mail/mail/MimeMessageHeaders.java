package mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import mail.exceptions.ParseException;



import mail.MimeMessageHeader;
import mail.exceptions.MimeMessageHeaderValidationException;
import mail.util.HeadersUtils;
import mail.util.LineInputStream;

public class MimeMessageHeaders {
	
	public static Logger logger = Logger.getLogger("log");
	
	private List<MimeMessageHeader> headers;
	
	public MimeMessageHeaders() {
		headers = new ArrayList<MimeMessageHeader>();
	}
	
	public MimeMessageHeaders(InputStream inputStream) {
		headers = new ArrayList<MimeMessageHeader>();
		parseAndLoadHeaders(inputStream);
	}
	
	public void addHeader(MimeMessageHeader mimeMessageHeader) {
		headers.add(mimeMessageHeader);
	}
	
	public void addHeader(MimeMessageHeader mimeMessageHeader, int position) {
		headers.add(position,mimeMessageHeader);
	}
	
	public MimeMessageHeader getHeader(String headerName) {
		for (MimeMessageHeader header : headers) {
			if (header.getName().equalsIgnoreCase(headerName)) {
				return header;
			}
		}
		return null;
	}
	
	public void removeHeader(MimeMessageHeader mimeMessageHeader) {
//		for (MimeMessageHeader header : headers) {
//			if (mimeMessageHeader.getName().equals(header.getName())) {
//				headers.remove(header);
//				break;
//			}
//		}
	}
	
	/**
	 * Parses and loads to structures into MimeMessageHeader from
	 * input stream is
	 * @param addLineFolded should we while parsing headers add \r\n to headers 
	 * @param is
	 */
	public void parseAndLoadHeaders(InputStream is, boolean addLineFolded) {
		
		// Read header lines until a blank line. It is valid
		// to have BodyParts with no header lines.
		String line;
		LineInputStream lis = new LineInputStream(is);
		String prevline = null;	// the previous header line, as a string
		// a buffer to accumulate the header in, when we know it's needed
		
		//w oryginalne uzywane StringBuffer - thread safe?
		StringBuilder lineBuffer = new StringBuilder();
		
		try {
		    do {
			line = lis.readLine();
			if (line != null &&
				(line.startsWith(" ") || line.startsWith("\t"))) {
			    // continuation of header
			    if (prevline != null) {
			    	lineBuffer.append(prevline);
			    	prevline = null;
			    }
			    if (addLineFolded) lineBuffer.append("\r\n");
			    lineBuffer.append(line);
			} else {
			    // new header
			    if (prevline != null)
				addHeaderLine(prevline);
			    else if (lineBuffer.length() > 0) {
				// store previous header first
				addHeaderLine(lineBuffer.toString());
				lineBuffer.setLength(0);
			    }
			    prevline = line;
			}
		    } while (line != null && line.length() > 0);
		} catch (IOException ioex) {
		    logger.error("Parsing error " + ioex.toString());
		}
	}
	
	/**
	 * Parses and loads to structures into MimeMessageHeader from
	 * input stream is 
	 * by default id dosen't add any \r\n
	 * @param is
	 */
	public void parseAndLoadHeaders(InputStream is) {
		parseAndLoadHeaders(is, true);
	}
	
	/**
	 * Add an RFC822 header line to the header store.
	 * If the line starts with a space or tab (a continuation line),
	 * add it to the last header line in the list.  Otherwise,
	 * append the new header line to the list.  <p>
	 *
	 * Note that RFC822 headers can only contain US-ASCII characters
	 *
	 * @param	line	raw RFC822 header line
	 */
	public void addHeaderLine(String line) {
		try {
		    char c = line.charAt(0);
		    if (c == ' ' || c == '\t') {
			MimeMessageHeader h =
			    (MimeMessageHeader)headers.get(headers.size() - 1);
			h.setLine("\r\n" + h.getLine());//dodanie \r\n bo wczesniej usuniete
			// w ramach czytania w LineInputStream
		    } else
			headers.add(new MimeMessageHeader(line));
		} catch (StringIndexOutOfBoundsException ex) {
			logger.error("Parsing error " + ex.toString());
		    return;
		} catch (NoSuchElementException ex) {
			logger.error("Parsing error " + ex.toString());
			return;
		}
	}
	
	/**
	 * check if in name of the header is correct 
	 * there should not be any CTLs, SPACE and ":" in field name (name)
	 * and after name should go ":" space and literrals
	 * 
	 * Check also semantics of each header
	 * should it be done?
	 * 
	 */
	public void validateHeader(MimeMessageHeader header) {
		
		try {
			HeadersUtils.validateHeader(header);
		} catch (MimeMessageHeaderValidationException ex) {
			/* caught exception should be put to logs and parsing 
			 * shout be countinuing
			 * */
			ex.printStackTrace();
		}
		
	}
	
	@Override
	public String toString() {
		
		String ret = "";
		for (MimeMessageHeader header : headers) {
			ret += header.toString();
			ret += "\n";
		}
		return ret;
	}
	
	public ContentType getContentType() throws ParseException {
		MimeMessageHeader header = getHeader("content-type");
		if (header==null) return null;
		return new ContentType(header.getValue());
	}
	
	public List<MimeMessageHeader> getHeaders() {
		return headers;
	}

	public void setHeaders(List<MimeMessageHeader> headers) {
		this.headers = headers;
	}
	
	
	/**
	 * main
	 * 
	 * For testing - reads all mails from resources - parses them
	 * and validate header (only some of them)
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		String resourceDirName = "resources";
		File dir = new File(resourceDirName);
		String[] fileNames = dir.list();
		for (int i = 0; i < fileNames.length; i++) {
			String fileName = fileNames[i];
			FileInputStream is = null;
			try {
				is = new FileInputStream("resources/"+fileName);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			MimeMessageHeaders messageHeaders = new MimeMessageHeaders();
			messageHeaders.parseAndLoadHeaders(is);
			
			try {
				for (MimeMessageHeader header : messageHeaders.getHeaders()) {
					System.out.println("Validating header " + header.getName());
					messageHeaders.validateHeader(header);
				}
				System.out.println("-----");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		
		
		
		
		
	}
	
}
