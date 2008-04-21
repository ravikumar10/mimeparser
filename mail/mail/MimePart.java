package mail;

import java.io.InputStream;


public class MimePart extends Part {
	
	public MimePart () {}
	
	/**
	 * 
	 * @param inputStream
	 * @param contentType
	 * @param headers
	 */
	public MimePart(InputStream inputStream, ContentType contentType, MimeMessageHeaders headers) {
		this.inputStream=inputStream;
		this.contentType=contentType;
		this.headers=headers;
	}
	
	@Override
	public void parse() {
		
		
		
		
		
		
		
	}
	
	@Override
	public String toString() {
		return new String(content);
	}
	
	private void analizeContent() {
		if (contentType.getBaseType().equals(TEXT_TYPE)) {
			//text
			if (contentType.getBaseType().equals("plain") || contentType.getBaseType().equals("html")) {
				
			}
			
			
		} else if (contentType.getBaseType().equals(MESSAGE_TYPE)) {
			//got simple rfc822 message
			
		} else if (contentType.getBaseType().equals(IMAGE_TYPE)) {
			// got image
			
		} else if (contentType.getBaseType().equals(AUDIO_TYPE)) {
			//got audio type
			
			
		} else if (contentType.getBaseType().equals(VIDEO_TYPE)) {
			//got video type
			
			
		} else if (contentType.getBaseType().equals(APPLICATION_TYPE)) {
			//got application
			
		}
	}

}
