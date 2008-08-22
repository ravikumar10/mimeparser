package server;

import java.util.HashMap;
import java.util.Random;

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
	
	public static String generateMessageId() {
		Random r = new Random();
    	String token = Long.toString(Math.abs(r.nextLong()), 36);
    	token=System.currentTimeMillis()+token;
    	return token;
	}
	
}
