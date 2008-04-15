package mail;

import java.io.InputStream;


public class MimePart extends Part {
	
	public MimePart () {}
	
	public MimePart(InputStream inputStream, ContentType contentType) {
		this.inputStream=inputStream;
		this.contentType=contentType;
	}
	
	@Override
	public void parse() {
		
		//tutaj w zaleznosci od mime typow i opcji przy nich musimy cos robic
		String baseType = getContentType().getBaseType();
		String subType = getContentType().getSubType();
		if (baseType.equals(TEXT_TYPE)) {
			//text
			if (subType.equals("plain") || subType.equals("html")) {
				
			}
			
			
		} else if (baseType.equals(MESSAGE_TYPE)) {
			//got simple rfc822 message
			
		} else if (baseType.equals(IMAGE_TYPE)) {
			// got image
			
		} else if (baseType.equals(AUDIO_TYPE)) {
			//got audio type
			
			
		} else if (baseType.equals(VIDEO_TYPE)) {
			//got video type
			
			
		} else if (baseType.equals(APPLICATION_TYPE)) {
			//got application
			
		}
		
		
		
		
		
	}
	
	

}
