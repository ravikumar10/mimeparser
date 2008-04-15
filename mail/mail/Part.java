package mail;

import java.io.InputStream;

import mail.exceptions.ParseException;

public abstract class Part {

	final String BOUNDARY = "boundary";
	
	//glownych siedem predefiniowanych Content-Typeow
	
	final String MULTIPART_TYPE = "multipart";
	final String TEXT_TYPE = "text";
	final String MESSAGE_TYPE = "message";
	final String IMAGE_TYPE = "image";
	final String AUDIO_TYPE = "audio";
	final String VIDEO_TYPE = "video";
	final String APPLICATION_TYPE = "application";
	
	/**
	 * body of message as a string
	 */
	String body;
	
	/**
	 * simple content type 
	 */
	ContentType contentType;
	
	/**
	 * 
	 */
	InputStream inputStream;
	
	/**
	 * Content is extracted from got input stream 
	 * amount of bytes which indicates the body of this part
	 * (if it's top part it represends all content
	 * beetween opening boundary line and ending boundary line)
	 */
	InputStream content;
	
	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
	public abstract void parse () throws ParseException;
	
}
