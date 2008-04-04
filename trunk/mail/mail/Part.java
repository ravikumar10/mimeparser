package mail;

import mail.exceptions.ParseException;

public abstract class Part {

	final String BOUNDARY = "boundary";
	String body;

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
	public abstract void parse () throws ParseException;
	
}
