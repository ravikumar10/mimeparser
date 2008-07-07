package mail.util;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MimeUtility {
	
	private static Hashtable mime2java;
	private static boolean foldText = true;
	
	 static final int ALL_ASCII 		= 1;
	 static final int MOSTLY_ASCII 	= 2;
	 static final int MOSTLY_NONASCII 	= 3;

	
	static final boolean nonascii(int b) {
		return b >= 0177 || (b < 040 && b != '\r' && b != '\n' && b != '\t');
	}
	
	static {
		
		mime2java = new Hashtable(10);
		
		if (mime2java.isEmpty()) {
		    mime2java.put("iso-2022-cn", "ISO2022CN");
		    mime2java.put("iso-2022-kr", "ISO2022KR");
		    mime2java.put("utf-8", "UTF8");
		    mime2java.put("utf8", "UTF8");
		    mime2java.put("ja_jp.iso2022-7", "ISO2022JP");
		    mime2java.put("ja_jp.eucjp", "EUCJIS");
		    mime2java.put("euc-kr", "KSC5601");
		    mime2java.put("euckr", "KSC5601");
		    mime2java.put("us-ascii", "ISO-8859-1");
		    mime2java.put("x-us-ascii", "ISO-8859-1");
		    //mine
		    mime2java.put("ISO-8859-2", "ISO-8859-2");
		    mime2java.put("iso-8859-2", "ISO-8859-2");
		}
		
	}
	
	/**
     * Convert a MIME charset name into a valid Java charset name. <p>
     *
     * @param charset	the MIME charset name
     * @return  the Java charset equivalent. If a suitable mapping is
     *		not available, the passed in charset is itself returned.
     */
    public static String javaCharset(String charset) {
	if (mime2java == null || charset == null)
	    // no mapping table, or charset parameter is null
	    return charset;

	String alias =
	    (String)mime2java.get(charset.toLowerCase(Locale.ENGLISH));
	return alias == null ? charset : alias;
    }
    
    /** 
     * Check if the given string contains non US-ASCII characters.
     * @param	s	string
     * @return		ALL_ASCII if all characters in the string 
     *			belong to the US-ASCII charset. MOSTLY_ASCII
     *			if more than half of the available characters
     *			are US-ASCII characters. Else MOSTLY_NONASCII.
     */
    static int checkAscii(String s) {
	int ascii = 0, non_ascii = 0;
	int l = s.length();

	for (int i = 0; i < l; i++) {
	    if (nonascii((int)s.charAt(i))) // non-ascii
		non_ascii++;
	    else
		ascii++;
	}
	if (non_ascii == 0)
	    return ALL_ASCII;
	if (ascii > non_ascii)
	    return MOSTLY_ASCII;

	return MOSTLY_NONASCII;
    }
	
    
    /** 
     * Check if the given byte array contains non US-ASCII characters.
     * @param	b	byte array
     * @return		ALL_ASCII if all characters in the string 
     *			belong to the US-ASCII charset. MOSTLY_ASCII
     *			if more than half of the available characters
     *			are US-ASCII characters. Else MOSTLY_NONASCII.
     *
     * XXX - this method is no longer used
     */
    static int checkAscii(byte[] b) {
	int ascii = 0, non_ascii = 0;

	for (int i=0; i < b.length; i++) {
	    // The '&' operator automatically causes b[i] to be promoted
	    // to an int, and we mask out the higher bytes in the int 
	    // so that the resulting value is not a negative integer.
	    if (nonascii(b[i] & 0xff)) // non-ascii
		non_ascii++;
	    else
		ascii++;
	}
	
	if (non_ascii == 0)
	    return ALL_ASCII;
	if (ascii > non_ascii)
	    return MOSTLY_ASCII;
	
	return MOSTLY_NONASCII;
    }
    
    /**
     * Fold a string at linear whitespace so that each line is no longer
     * than 76 characters, if possible.  If there are more than 76
     * non-whitespace characters consecutively, the string is folded at
     * the first whitespace after that sequence.  The parameter
     * <code>used</code> indicates how many characters have been used in
     * the current line; it is usually the length of the header name. <p>
     *
     * Note that line breaks in the string aren't escaped; they probably
     * should be.
     *
     * @param	used	characters used in line so far
     * @param	s	the string to fold
     * @return		the folded string
     * @since		JavaMail 1.4
     */
    public static String fold(int used, String s) {
	if (!foldText)
	    return s;

	int end;
	char c;
	// Strip trailing spaces and newlines
	for (end = s.length() - 1; end >= 0; end--) {
	    c = s.charAt(end);
	    if (c != ' ' && c != '\t' && c != '\r' && c != '\n')
		break;
	}
	if (end != s.length() - 1)
	    s = s.substring(0, end + 1);

	// if the string fits now, just return it
	if (used + s.length() <= 76)
	    return s;

	// have to actually fold the string
	StringBuffer sb = new StringBuffer(s.length() + 4);
	char lastc = 0;
	while (used + s.length() > 76) {
	    int lastspace = -1;
	    for (int i = 0; i < s.length(); i++) {
		if (lastspace != -1 && used + i > 76)
		    break;
		c = s.charAt(i);
		if (c == ' ' || c == '\t')
		    if (!(lastc == ' ' || lastc == '\t'))
			lastspace = i;
		lastc = c;
	    }
	    if (lastspace == -1) {
		// no space, use the whole thing
		sb.append(s);
		s = "";
		used = 0;
		break;
	    }
	    sb.append(s.substring(0, lastspace));
	    sb.append("\r\n");
	    lastc = s.charAt(lastspace);
	    sb.append(lastc);
	    s = s.substring(lastspace + 1);
	    used = 1;
	}
	sb.append(s);
	return sb.toString();
    }

    /**
     * Unfold a folded header.  Any line breaks that aren't escaped and
     * are followed by whitespace are removed.
     *
     * @param	s	the string to unfold
     * @return		the unfolded string
     * @since		JavaMail 1.4
     */
    public static String unfold(String s) {
	if (!foldText)
	    return s;

	StringBuffer sb = null;
	int i;
	while ((i = indexOfAny(s, "\r\n")) >= 0) {
	    int start = i;
	    int l = s.length();
	    i++;		// skip CR or NL
	    if (i < l && s.charAt(i - 1) == '\r' && s.charAt(i) == '\n')
		i++;	// skip LF
	    if (start == 0 || s.charAt(start - 1) != '\\') {
		char c;
		// if next line starts with whitespace, skip all of it
		// XXX - always has to be true?
		if (i < l && ((c = s.charAt(i)) == ' ' || c == '\t')) {
		    i++;	// skip whitespace
		    while (i < l && ((c = s.charAt(i)) == ' ' || c == '\t'))
			i++;
		    if (sb == null)
			sb = new StringBuffer(s.length());
		    if (start != 0) {
			sb.append(s.substring(0, start));
			sb.append(' ');
		    }
		    s = s.substring(i);
		    continue;
		}
		// it's not a continuation line, just leave it in
		if (sb == null)
		    sb = new StringBuffer(s.length());
		sb.append(s.substring(0, i));
		s = s.substring(i);
	    } else {
		// there's a backslash at "start - 1"
		// strip it out, but leave in the line break
		if (sb == null)
		    sb = new StringBuffer(s.length());
		sb.append(s.substring(0, start - 1));
		sb.append(s.substring(start, i));
		s = s.substring(i);
	    }
	}
	if (sb != null) {
	    sb.append(s);
	    return sb.toString();
	} else
	    return s;
    }
    
    /**
     * Return the first index of any of the characters in "any" in "s",
     * or -1 if none are found.
     *
     * This should be a method on String.
     */
    private static int indexOfAny(String s, String any) {
	return indexOfAny(s, any, 0);
    }

    private static int indexOfAny(String s, String any, int start) {
	try {
	    int len = s.length();
	    for (int i = start; i < len; i++) {
		if (any.indexOf(s.charAt(i)) >= 0)
		    return i;
	    }
	    return -1;
	} catch (StringIndexOutOfBoundsException e) {
	    return -1;
	}
    }
    
    /**
     * A utility method to quote a word, if the word contains any
     * characters from the specified 'specials' list.<p>
     *
     * The <code>HeaderTokenizer</code> class defines two special
     * sets of delimiters - MIME and RFC 822. <p>
     *
     * This method is typically used during the generation of 
     * RFC 822 and MIME header fields.
     *
     * @param	word	word to be quoted
     * @param	specials the set of special characters
     * @return		the possibly quoted word
     * @see	javax.mail.internet.HeaderTokenizer#MIME
     * @see	javax.mail.internet.HeaderTokenizer#RFC822
     */
    public static String quote(String word, String specials) {
	int len = word.length();

	/*
	 * Look for any "bad" characters, Escape and
	 *  quote the entire string if necessary.
	 */
	boolean needQuoting = false;
	for (int i = 0; i < len; i++) {
	    char c = word.charAt(i);
	    if (c == '"' || c == '\\' || c == '\r' || c == '\n') {
		// need to escape them and then quote the whole string
		StringBuffer sb = new StringBuffer(len + 3);
		sb.append('"');
		sb.append(word.substring(0, i));
		int lastc = 0;
		for (int j = i; j < len; j++) {
		    char cc = word.charAt(j);
		    if ((cc == '"') || (cc == '\\') || 
			(cc == '\r') || (cc == '\n'))
			if (cc == '\n' && lastc == '\r')
			    ;	// do nothing, CR was already escaped
			else
			    sb.append('\\');	// Escape the character
		    sb.append(cc);
		    lastc = cc;
		}
		sb.append('"');
		return sb.toString();
	    } else if (c < 040 || c >= 0177 || specials.indexOf(c) >= 0)
		// These characters cause the string to be quoted
		needQuoting = true;
	}

	if (needQuoting) {
	    StringBuffer sb = new StringBuffer(len + 2);
	    sb.append('"').append(word).append('"');
	    return sb.toString();
	} else 
	    return word;
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
	 * @param headerContent
	 * @return
	 */
	public static String convertHeaderAccordingToInsideEncoding(String headerContent) {
		
		String regexp = "([A-Za-z_0-9-]+)\\?([QB])\\?([A-Za-z_=*0-9]+)";
		StringBuilder ret = new StringBuilder(headerContent.length());
		// deleting \n\t and \nspace
		headerContent = headerContent.replace("\n\t", "").replace("\n ", "");
		//spliting over =? or ?=
		String[] textParts = headerContent.split("(=\\?)|(\\?=)");
		
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
	
	public void convertFromBase64() {
		
		//sun.misc.
		
	}
	
	
	public static void main(String[] args) {
		
		String example = "Bardzo =?ISO-8859-2?Q?d=B3ugi_temat_ze_specjalnymi_znaka?=\n" +
						 " =?ISO-8859-2?Q?mi_=22=22_=5B=5D_=28=29*_i_ciekawe_jak_to_?=\n" +
						 " =?ISO-8859-2?Q?zostanie_zinterpretowane_bo_nikt_tego_nie_wie?=\n" +
						 " =?ISO-8859-2?Q?_o_jeszcze_malpa_=40?=\n";
		
		String hex = "22";
		Integer i  = Integer.parseInt(hex, 16);
		
		String reto = MimeUtility.convertHeaderAccordingToInsideEncoding(example);
		System.out.println(reto);
				
	}
	
	
}
