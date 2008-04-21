package mail.util;

import java.io.IOException;
import java.io.InputStream;

public class StringUtils {
	
	/**
	 * Special characters according to RFC 822
	   
	   specials    =  "(" / ")" / "<" / ">" / "@"  ; Must be in quoted-
                 /  "," / ";" / ":" / "\" / <">  ;  string, to use
                 /  "." / "[" / "]"
	 */
	private final static String[] SPECIAL_CHARACTERS = {
		"(", ")", "<", ">", "@", ",", ";", ":",  
		"\\", "\"", ".", "[", "]" 
	};
	
	
	
	public static boolean isSpecialCharacter(char c) {
	
		for (String specialChar : SPECIAL_CHARACTERS) {
			if (specialChar.charAt(0) == c) return true;
		}
		return false;
	}
	
	/**
	 * CTL characters are all between
	 * 0-31 and 127 (decimal)
	 * @param c
	 * @return
	 */
	public static boolean isCTLCharacter(char c) {
		
		if ((c>=0 && c<=31) || (c==127)) {
			return true;
		}
		return false;
	}
	
	public static byte[] getBytes(String s) {
		
		char [] chars= s.toCharArray();
		int size = chars.length;
		byte[] bytes = new byte[size];
	    	
		for (int i = 0; i < size;)
		    bytes[i] = (byte) chars[i++];
		return bytes;
	}
	
	public static String InputStreamToString(InputStream inputStream) {
		
		String ret = "";
		
		
		char[] buf, lineBuffer; 
		buf = lineBuffer = new char[128];
		int room = buf.length;
		int offset = 0;
		int c;
		
		try {
			while ((c = inputStream.read()) != -1) {
				if (--room < 0) { // No room, need to grow.
			    	buf = new char[offset + 128];
			    	room = buf.length - offset - 1;
			    	System.arraycopy(lineBuffer, 0, buf, 0, offset);
			    	lineBuffer = buf;
			    }
			    buf[offset++] = (char)c;
			}
		    if ((c == -1) && (offset == 0))
			    return null;
			
			return String.copyValueOf(buf, 0, offset);
			
		} catch (IOException ex) {
			
		}
		
		return ret;
	}
	
	public static void main(String[] args) {
		
		char c = "\\".charAt(0);
		if (StringUtils.isSpecialCharacter(c)) System.out.println("sc");;
		
	}
	
}
