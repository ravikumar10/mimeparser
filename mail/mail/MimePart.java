package mail;

import java.io.IOException;
import java.io.InputStream;


public class MimePart extends Part {
	
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
		char[] buffer = new char[1024];
		int offset = 0;
		int c1;
		int room = buffer.length-1;
		
		try {
			while ((c1 = inputStream.read()) != -1) {
				buffer[offset++] = (char) c1;
				
				if (--room < 0) { // No room, need to grow.
					//tmp buffer
					char[] tmpBuffer = new char[offset + 256];
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
	
}
