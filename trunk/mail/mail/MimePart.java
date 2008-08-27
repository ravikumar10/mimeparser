package mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mail.util.LineOutputStream;


public class MimePart extends Part {
	
	private final static int LINE_LENGTH = 76;
	
	public MimePart () {}
	
	/**
	 * 
	 * @param inputStream
	 * @param contentType
	 * @param headers
	 */
	public MimePart(InputStream inputStream, ContentType contentType, MimeMessageHeaders headers, Part parent) {
		this.inputStream=inputStream;
		this.contentType=contentType;
		this.headers=headers;
		parse();
	}
	
	/**
	 * 
	 * @param conntent
	 * @param contentType
	 * @param headers
	 * @param parent
	 */
	public MimePart(byte[] content, ContentType contentType, MimeMessageHeaders headers, Part parent) {
		this.content = content;
		this.contentType = contentType;
		this.headers = headers;
		this.parent = parent;
	}
	
	@Override
	public void parse() {
		
		//just reading from inputstream to byte array
		char[] buffer = new char[dataBufferSize];
		int offset = 0;
		int c1;
		int room = buffer.length-1;
		
		try {
			while ((c1 = inputStream.read()) != -1) {
				buffer[offset++] = (char) c1;
				
				if (--room < 0) { // No room, need to grow.
					//tmp buffer
					char[] tmpBuffer = new char[offset + dataBufferIncreaseSize];
					room = tmpBuffer.length - offset - 1;
					//copying from original to tmp
					System.arraycopy(buffer, 0, tmpBuffer, 0, buffer.length);
					//swaping original to new tmp
					buffer = tmpBuffer;
				}
			}
			
			//converting char array to bytes and save it into part 
			//structure
			content = new byte[buffer.length];
			for (int i=0;i<buffer.length;i++)
//				content[i] = (byte)(buffer[i] & 0xff);
				content[i] = (byte)(buffer[i]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	@Override
	public String toString() {
		
		String headersString = "", contentString = "";
		if (headers!=null) headersString = "\nHeaders:\n" + headers.toString();
		if (content!=null) contentString = "\nContent:\n" + new String(content);
		
		String ret = headersString + contentString;   
		
		return ret;
	}
	
	@Override
	public String toString(int n) {
		return toString();
	}

	@Override
	public void writeTo(OutputStream os) {
		
		LineOutputStream los = new LineOutputStream(os);
		
		// headers
		if (headers!=null) {
			for (MimeMessageHeader mimeMessageHeader : headers.getHeaders()) {
				los.writeln(mimeMessageHeader.getLine());
			}
		}
		
		// CRLF before body
		los.writeln("");
		
		byte[] writeBuffer = new byte[LINE_LENGTH]; 
		for (int i = 0; i < content.length; i+=LINE_LENGTH) {
			if (i+LINE_LENGTH>content.length)
				System.arraycopy(content, i, writeBuffer, 0, i+LINE_LENGTH-content.length);
			else
				System.arraycopy(content, i, writeBuffer, 0, LINE_LENGTH);
			
			// here is some kind of hack 
			// when we parse message to avoid too many buffer grows we each time 
			// make buffer twice big - this causes that sometime buffer (content) contains
			// at the end of this 0 - when writing to stream we CANT write it!!!
			if (writeBuffer[LINE_LENGTH-1]==0) {
				//buffer contains 0 at the end write sign by sing from it
				int j=0;
				while (writeBuffer[j]!=0) {
					los.write(writeBuffer[j]);
					j++;
				}
				break;
			} else {
				//buffer doesn't contain 0 write all of it
				try {
					los.write(writeBuffer);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
