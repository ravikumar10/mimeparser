package mail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import mail.exceptions.ParseException;
import mail.util.BufferedSharedInputStream;
import mail.util.LineInputStream;
import mail.util.LineOutputStream;
import mail.util.SharedInputStream;
import mail.util.StringUtils;

/**
 * Class represends the part of the e-mail message in mime format
 * Mime Part can contains more parts (it can be f.e. multipart/alternative
 * or can be simple type like image/jpeg or text/plain or text/hmtl (most common)
 *
 * @author zbychu
 *
 */

public class MimeMultiPart extends Part {
	
	public static Logger logger = Logger.getLogger("log");
	
	/**
	 * part may contains other multiparts or can be simple part
	 */
	List<Part> parts = new ArrayList<Part>();
	
	/**
	 * boundary line is boundary get from ContentType + "--"
	 * or if it's not present in ContentType it's
	 * first line of preambula
	 */
	String boundaryLine;
	
	/**
	 * preamble which if message is multipart is always at the beginning
	 */
	Preamble preamble;
	
	/**
	 * Main method resposible for parsing messages
	 * @throws ParseException - zmienic to na jakis inny exception 
	 */
	public void parse() throws ParseException {
		
		LineInputStream lis = new LineInputStream(this.inputStream);
		
		if (contentType==null)
			contentType = headers.getContentType();
		
		boundaryLine=contentType.getBoundaryLine();
		
		preamble = new Preamble(inputStream, boundaryLine);
		//sytuacja ponizej zachodzi jest boundaryLine jest nullem
		// a premabula zawiera cos jak boundaryLine
		if (preamble.isPreambuleABondary()) this.boundaryLine=preamble.getPreamble();
		
		// nie ma boundaryLine to nie mamy zbytnio co robic
		if (this.boundaryLine==null) throw new ParseException("No boundary line");
		
		//tutaj szukamy teraz gdzie sie jest nastepne boundaryLine
		//i ten input Stream przekazujemy do analizy parta
		//korzystamy z algorytmu boyre'a moore'a
		
		byte[] boundaryBytes = StringUtils.getBytes(boundaryLine);
		int boundaryLenght = boundaryBytes.length;
		
		// initialize Bad Character Shift table
	    int[] bcs = StringUtils.initializeBMAlgorithmBadCharacterShiftTable(boundaryBytes);
	    
	    //initialize Good Sufix Shift table
	    int[] good_suffix_shift = StringUtils.initializeBMAlgorithmGoodSuffixShiftTable(boundaryBytes);
		
	    //sliding window
	    byte[] slidingWindowBuffer = new byte[boundaryLenght];
	    
	    try {
		    int i,j=0;
		    int shift=0;
		    
		    byte[] tmpBuffer = new byte[dataBufferSize];
		    int positionInTmpBuffer = 0;
		    
		    int slidingWindowBufferLenght = slidingWindowBuffer.length;
		    inputStream.mark(5*slidingWindowBufferLenght);//marking
		    inputStream.read(slidingWindowBuffer, 0, slidingWindowBuffer.length);
		    
		    for(;;) {
		    	
		    	for (i=boundaryLenght-1; i>=0 && boundaryBytes[i] == slidingWindowBuffer[i]; i--){}
		    	
		    	if (i<0) {
		    		// very useful for debuging
//		    		logger.debug("Part: " + new String(tmpBuffer));
		    		
		    		analizeAndCreatePart(tmpBuffer);
		    		this.content=tmpBuffer;
		    		shift = good_suffix_shift[0];
		    		j += shift;
		    		tmpBuffer = new byte[dataBufferSize];
		    		positionInTmpBuffer = 0;
		    		
		    		//clearing sliding window
		    		for(int k=0;k<slidingWindowBufferLenght;k++) slidingWindowBuffer[k] = 0;
		    		
		    		//marking position
		    		inputStream.mark(5*slidingWindowBufferLenght);
		    	} else {
		    		shift = Math.max(good_suffix_shift[i], bcs[slidingWindowBuffer[i]] - boundaryLenght + 1 + i);
		    		j += shift;
		    		//reseting input stream to read bytes from last shift of buffer
		    		//to another
		    		inputStream.reset();
		    	}
		    	
		    	//increasing tmp buffer if it's not big enough
//		    	if (positionInTmpBuffer+shiftPosition>tmpBuffer.length) {
		    	if (positionInTmpBuffer+shift>tmpBuffer.length) {
//		    		byte[] tmp = new byte[tmpBuffer.length+dataBufferIncreaseSize];
		    		byte[] tmp = new byte[tmpBuffer.length*2];
		    		System.arraycopy(tmpBuffer, 0, tmp, 0, tmpBuffer.length);
		    		tmpBuffer = tmp;
		    	}
		    	
		    	//if we encountered boundary line we read all 
		    	// \n \t after it
		    	if (positionInTmpBuffer==0) {
		    		int c1;
		    		while ((c1 = inputStream.read()) != -1) {
		    			if (c1 == '\n' || c1 == '\t' || c1 == '\r') {
		    				inputStream.mark(5*slidingWindowBufferLenght);
		    				shift--;
		    			} else { 
		    				inputStream.reset();
		    				break;
		    			}
 		    		}
		    	}
		    	
		    	//reading bytes before shift to buffer in which we have all
		    	// data from input stream
		    	inputStream.read(tmpBuffer, positionInTmpBuffer, shift);
		    	
		    	//changing actual position in buffer into which we read bytes
		    	positionInTmpBuffer+=shift;
    			
		    	//marking our new position
    			inputStream.mark(5*slidingWindowBufferLenght);
    			
    			//moving sliding window to new position
    			inputStream.read(slidingWindowBuffer, 0, slidingWindowBufferLenght);
    			
    			//analizing if we get --\n so it's end of parsing
    			byte[] tmpContent = new byte[3];
    			System.arraycopy(tmpBuffer, 0, tmpContent, 0, 3);
    			if (new String(tmpContent).startsWith("--\n")) {
    				logger.debug("Found end of line");
    				break;
    			}
    			
    			// EOS - end of stream
    			if (tmpBuffer[0]==0 || slidingWindowBuffer[0]==0) {
    				logger.debug("End of stream");
    				break;
    			}
    			
		    }
	   
	    } catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * This method analizes if any multipart is in contentType
	 * in given input stream - if so it tries to create mimemultipart
	 * - if not just creates mimepart
	 * 
	 * As a part we also understand all things after boundary and --
	 * which appear at the end of the message  
	 * @return
	 * @throws ParseException 
	 */
	public void analizeAndCreatePart(byte[] content) throws ParseException {
		
		LineInputStream lis = new LineInputStream(new ByteArrayInputStream(content));
		
		Part part = null;
		
		// not all of the systems are so fine that they finish 
		// mime multipart with boundaryline + "--"
		
		MimeMessageHeaders headers = new MimeMessageHeaders(lis);
		ContentType ct = headers.getContentType();
		if (ct==null) throw new ParseException("No content type in message");
		if (ct.getPrimaryType().equals(MULTIPART_TYPE)) {
			//SharedFileInputStream sfis = new SharedFileInputStream(new File();
			BufferedSharedInputStream bsis = new BufferedSharedInputStream(lis);
			part = new MimeMultiPart(bsis, headers, this);
			//recursive creation of mimemultipart
		} else {
			long pos = lis.getPosition();
			int newContentLenght = content.length-(int)pos;
			byte[] newContent = new byte[newContentLenght];
			System.arraycopy(content, (int)pos, newContent, 0, newContentLenght);
			part = new MimePart(newContent, ct, headers, this);
		}
		
		//adding part
		parts.add(part);
	}
	
	
	public List<Part> getParts() {
		return parts;
	}


	public MimeMultiPart() {}
	
	public MimeMultiPart(InputStream inputStream) {
		this.inputStream=inputStream;
	}
	
	public MimeMultiPart(InputStream inputStream, MimeMessageHeaders mimeMessageHeaders, Part parent) throws ParseException {
		this.inputStream=inputStream;
		this.headers=mimeMessageHeaders;
		this.parent=parent;
		parse();
	}
	
	public MimeMultiPart(InputStream inputStream, ContentType contentType, MimeMessageHeaders mimeMessageHeaders, Part parent) throws ParseException {
		this.inputStream=inputStream;
		this.headers=mimeMessageHeaders;
		this.parent=parent;
		this.contentType=contentType;
		parse();
	}

	@Override
	public String toString() {
		String ret = "";
		for (Part part : parts) {
			ret += part.toString().replaceAll("\n", "\n\t");
		}
		return ret;
	}
	
	@Override
	public String toString(int n) {
		
		String ret = "";
		String indent = "";
		for (int i=0;i<n;i++) indent+="\t";
		ret += "\n" + indent + "Parts:\n" + indent + "|_";
		int i=1;
		for (Part part : parts) {
			ret += "\n" + indent + "  Part" + i++ + ":"  + part.toString(n+1).replaceAll("\n", "\n"+indent+"  |");
		}
		return ret;
	}
	
	@Override
	public void writeTo(OutputStream os) {
		
		LineOutputStream los = new LineOutputStream(os);
		
		//headers
		if (headers!=null) {
			for (MimeMessageHeader mimeMessageHeader : headers.getHeaders()) {
				//writing body
				los.writeln(mimeMessageHeader.getLine());
			}
			los.writeln("");
		}
		
		// preamble
		if (preamble!=null) los.writeln(preamble.getPreamble());
		
		//CRLF
		//los.writeln("");
		
		for (Part part : parts) {
			//boundary line
			los.writeln(boundaryLine);
			
			//part
			part.writeTo(os);
			los.writeln("");
		}
		
		los.writeln(boundaryLine + "--");
	}
	
	public String getBoundaryLine() {
		return boundaryLine;
	}


	public void setBoundaryLine(String boundaryLine) {
		this.boundaryLine = boundaryLine;
	}


	public Preamble getPreamble() {
		return preamble;
	}


	public void setPreamble(Preamble preamble) {
		this.preamble = preamble;
	}
	
	public static void main(String[] args) {
		
		String sampleBoundaryString = "--===============0507542226==";
		String sampleContentString = "" +
				"Content-Type: text/plain; charset=\"utf-8\"\n" +
				"MIME-Version: 1.0\n" +
				"Content-Transfer-Encoding: base64\n" +
				"\n" +
				"RG9zdGF3YcSHIMW8eWN6ZW5pYSBvZCB6bmFqb215Y2ggemF3c3plIGplc3QgbWnFgm8hCgpEemlz\n" +
				"aWFqIE1hZ2RhIE0gbyBuaWNrdSBNYWxpbmthIG9iY2hvZHppIHVyb2R6aW55IChkYXRhIHVyb2R6\n" +
				"ZW5pYTogMTk4Ni0wNC0xNyksIHdpxJljIHplcmtuaWogbmEgcHJvZmlsIHRlaiBvc29ieTogaHR0\n" +
				"cDovL2dyb25vLm5ldC91c2Vycy8xMTAwNTE3LywgYSBwb3RlbSBzcHJhdyBqZWogcHJ6eWplbW5v\n" +
				"xZvEhyBpIHd5xZtsaWogxbx5Y3plbmlhIHByemV6IEdyb25vOiBodHRwOi8vZ3Jvbm8ubmV0L21h\n" +
				"aWxib3gvY3JlYXRlLzExMDA1MTcvLgoKWiBwb3pkcm93aWVuaWFtaSwKCkdyb25lawoKaHR0cDov\n" +
				"L2dyb25vLm5ldC8KCkplxZtsaSBuaWUgY2hjZXN6IGRvc3Rhd2HEhyB3acSZY2VqIG1haWxpIHog\n" +
				"Z3JvbmEsIHdlamTFuiBuYSBzdHJvbsSZIGh0dHA6Ly9ncm9uby5uZXQvbWFpbGJveC9zZXR0aW5n\n" +
				"cyBpIHptaWXFhCB1c3Rhd2llbmlhLg==\n" +
				"\n" +
				"--===============0507542226==\n" +
				"\n" +
				"Content-Type: text/plain; charset=\"utf-8\"\n" +
				"MIME-Version: 1.0\n" +
				"Content-Transfer-Encoding: base64\n" +
				"\n" +
				"RG9zdGF3YcSHIMW8eWN6ZW5pYSBvZCB6bmFqb215Y2ggemF3c3plIGplc3QgbWnFgm8hCgpEemlz\n" +
				"aWFqIE1hZ2RhIE0gbyBuaWNrdSBNYWxpbmthIG9iY2hvZHppIHVyb2R6aW55IChkYXRhIHVyb2R6\n" +
				"ZW5pYTogMTk4Ni0wNC0xNyksIHdpxJljIHplcmtuaWogbmEgcHJvZmlsIHRlaiBvc29ieTogaHR0\n" +
				"cDovL2dyb25vLm5ldC91c2Vycy8xMTAwNTE3LywgYSBwb3RlbSBzcHJhdyBqZWogcHJ6eWplbW5v\n" +
				"xZvEhyBpIHd5xZtsaWogxbx5Y3plbmlhIHByemV6IEdyb25vOiBodHRwOi8vZ3Jvbm8ubmV0L21h\n" +
				"aWxib3gvY3JlYXRlLzExMDA1MTcvLgoKWiBwb3pkcm93aWVuaWFtaSwKCkdyb25lawoKaHR0cDov\n" +
				"L2dyb25vLm5ldC8KCkplxZtsaSBuaWUgY2hjZXN6IGRvc3Rhd2HEhyB3acSZY2VqIG1haWxpIHog\n" +
				"Z3JvbmEsIHdlamTFuiBuYSBzdHJvbsSZIGh0dHA6Ly9ncm9uby5uZXQvbWFpbGJveC9zZXR0aW5n\n" +
				"cyBpIHptaWXFhCB1c3Rhd2llbmlhLg==\n" +
				"\n" +
				"--===============0507542226==--\n";

		
		byte[] pattern = StringUtils.getBytes(sampleBoundaryString);
		byte[] text = StringUtils.getBytes(sampleContentString);
		
//		StringUtils.algorithmBM(pattern, text);
		
	}

	
}
