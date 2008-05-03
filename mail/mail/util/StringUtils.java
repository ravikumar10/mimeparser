package mail.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	/**
	 * According to RFC 2047
	 * Method parses (using regexp) text in non USASCII words 
	 * to String with set encoding
	 * codedText can't not be longer than 75 characters
	 * 
	 * B encoding - like Base64
	 * Q encoding:
	 *   - = followed by hex digits
	 *   - _ - space
	 *   - other than = ? _ printable ASCII represends by themselves
	 *     SPACE and TAB MUST NOT be represented by themselves
	 * 
	 * Given text may be folded
	 * 
	 * @param text
	 * @return
	 */
	public String convertStringAccordingToInsideEncoding(String text) {
		
		String regexp = "([A-Za-z_0-9-]+)\\?([QB])\\?([A-Za-z_=*0-9]+)";
		StringBuilder ret = new StringBuilder(text.length());
		// deleting \n\t and \nspace
		text = text.replace("\n\t", "").replace("\n ", "");
		//spliting over =? or ?=
		String[] textParts = text.split("(=\\?)|(\\?=)");
		
		for (String textPart : textParts) {
			
			Pattern pattern = Pattern.compile(regexp);
			Matcher matcher = pattern.matcher(textPart);
			if (matcher.matches()) {
				String charset = matcher.group(1);
				String codingType = matcher.group(2);
				String codedText = matcher.group(3);
				
				
				switch (codingType.toLowerCase().charAt(0)) {
				case 'q':
					int codedTextBufferDecrease = 0;
					byte[] codedTextBuffer = new byte[codedText.length()];
					for (int i = 0, bi=0; i < codedText.length(); i++) {
						char c = codedText.charAt(i);
						if (c == '=') {//hex
							codedTextBufferDecrease+=2;
							String hex = codedText.substring(i + 1, i + 3);
							c = (char)Integer.parseInt(hex, 16);
							i += 2;
						} else if (c == '_') //space
							c = (char)32;
						codedTextBuffer[bi++] = (byte)c;
					}
					try {
						byte[] tmpCodedTextBuffer = new byte[codedText.length()-codedTextBufferDecrease]; 
						System.arraycopy(codedTextBuffer, 0, tmpCodedTextBuffer, 0, codedText.length()-codedTextBufferDecrease);
						ret.append(new String(tmpCodedTextBuffer,charset));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					break;
				case 'b':
//					base 64
					break;
				default:
//					rzucic jakims wyjatkiem
					break;
				}
			} else {
				ret.append(textPart);
			}
		}
		
		return ret.toString();
	}
	
	public static void main(String[] args) {
		
		String example = "Bardzo =?ISO-8859-2?Q?d=B3ugi_temat_ze_specjalnymi_znaka?=\n" +
						 " =?ISO-8859-2?Q?mi_=22=22_=5B=5D_=28=29*_i_ciekawe_jak_to_?=\n" +
						 " =?ISO-8859-2?Q?zostanie_zinterpretowane_bo_nikt_tego_nie_wie?=\n" +
						 " =?ISO-8859-2?Q?_o_jeszcze_malpa_=40?=\n";
		
		String hex = "22";
		Integer i  = Integer.parseInt(hex, 16);
		
		StringUtils su = new StringUtils();
		
		String reto = su.convertStringAccordingToInsideEncoding(example);
		System.out.println(reto);
				
	}
	
}
