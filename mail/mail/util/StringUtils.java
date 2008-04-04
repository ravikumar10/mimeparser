package mail.util;

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
	
	public static void main(String[] args) {
		
		char c = "\\".charAt(0);
		if (StringUtils.isSpecialCharacter(c)) System.out.println("sc");;
		
	}
	
}
