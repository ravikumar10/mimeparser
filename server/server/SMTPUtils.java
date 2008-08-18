package server;

import java.util.HashMap;

public class SMTPUtils {
	
	private final static HashMap<String, String> COMMANDS = new HashMap<String, String>() {};
	
	static {
		COMMANDS.put("helo", "HELO");
		COMMANDS.put("ehlo", "EHLO");
		COMMANDS.put("mailfrom", "MAIL FROM");
		COMMANDS.put("rcptto", "RCPT TO");
		COMMANDS.put("data", "DATA");
		COMMANDS.put("quit", "QUIT");
	}
	
	public static String getCommand(String key) {
		return COMMANDS.get(key);
	}
	
}
