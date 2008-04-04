package mail;

import mail.exceptions.MimeMessageHeaderException;
import mail.exceptions.MimeMessageHeaderValidationException;

public class MimeMessageHeader {
	
	private String name;
	private String value;
	private String line;
	
	public MimeMessageHeader(String line) {
		int i = line.indexOf(':');
	    if (i < 0) { // throw exceptions - should never happen
	    	throw new MimeMessageHeaderException("Header - " + line + " - doesn't contain any : ");
	    } 
	    this.name = line.substring(0, i).trim(); 
		this.value = line.substring(i+1).trim();
		this.line = line;
	}
	
	public MimeMessageHeader(String name, String value) {
		super();
		this.name = name;
		this.value = value;
		if (name!=null && value!=null) {
			line = name + ": " + value;
		}
	}
	
	@Override
	public String toString() {
		if (line!=null) return line;
		else return name+value;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}
	
	public static void main(String[] args) {
		
		try {
			new MimeMessageHeader("Return-Path: <almulista+bounces-3988-zbychu=astronet.pl@astronet.pl>");
		} catch (MimeMessageHeaderValidationException ex) {
			ex.printStackTrace();
		}
	}
}
