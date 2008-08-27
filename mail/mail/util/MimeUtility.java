package mail.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mail.exceptions.ParseException;

public class MimeUtility {
	
	private static Hashtable mime2java;
	private static Hashtable java2mime;
	private static boolean foldText = true;
	private static boolean decodeStrict = true;
	private static boolean foldEncodedWords = true;
	 static final int ALL_ASCII 		= 1;
	 static final int MOSTLY_ASCII 	= 2;
	 static final int MOSTLY_NONASCII 	= 3;

	
	static final boolean nonascii(int b) {
		return b >= 0177 || (b < 040 && b != '\r' && b != '\n' && b != '\t');
	}
	
	static {
		java2mime = new Hashtable(40);
		mime2java = new Hashtable(10);
		
		if (java2mime.isEmpty()) {
		    java2mime.put("8859_1", "ISO-8859-1");
		    java2mime.put("iso8859_1", "ISO-8859-1");
		    java2mime.put("iso8859-1", "ISO-8859-1");

		    java2mime.put("8859_2", "ISO-8859-2");
		    java2mime.put("iso8859_2", "ISO-8859-2");
		    java2mime.put("iso8859-2", "ISO-8859-2");

		    java2mime.put("8859_3", "ISO-8859-3");
		    java2mime.put("iso8859_3", "ISO-8859-3");
		    java2mime.put("iso8859-3", "ISO-8859-3");

		    java2mime.put("8859_4", "ISO-8859-4");
		    java2mime.put("iso8859_4", "ISO-8859-4");
		    java2mime.put("iso8859-4", "ISO-8859-4");

		    java2mime.put("8859_5", "ISO-8859-5");
		    java2mime.put("iso8859_5", "ISO-8859-5");
		    java2mime.put("iso8859-5", "ISO-8859-5");

		    java2mime.put("8859_6", "ISO-8859-6");
		    java2mime.put("iso8859_6", "ISO-8859-6");
		    java2mime.put("iso8859-6", "ISO-8859-6");

		    java2mime.put("8859_7", "ISO-8859-7");
		    java2mime.put("iso8859_7", "ISO-8859-7");
		    java2mime.put("iso8859-7", "ISO-8859-7");

		    java2mime.put("8859_8", "ISO-8859-8");
		    java2mime.put("iso8859_8", "ISO-8859-8");
		    java2mime.put("iso8859-8", "ISO-8859-8");

		    java2mime.put("8859_9", "ISO-8859-9");
		    java2mime.put("iso8859_9", "ISO-8859-9");
		    java2mime.put("iso8859-9", "ISO-8859-9");

		    java2mime.put("sjis", "Shift_JIS");
		    java2mime.put("jis", "ISO-2022-JP");
		    java2mime.put("iso2022jp", "ISO-2022-JP");
		    java2mime.put("euc_jp", "euc-jp");
		    java2mime.put("koi8_r", "koi8-r");
		    java2mime.put("euc_cn", "euc-cn");
		    java2mime.put("euc_tw", "euc-tw");
		    java2mime.put("euc_kr", "euc-kr");
		}
		
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
     * Look for encoded words within a word.  The MIME spec doesn't
     * allow this, but many broken mailers, especially Japanese mailers,
     * produce such incorrect encodings.
     */
    private static String decodeInnerWords(String word)
				throws UnsupportedEncodingException {
	int start = 0, i;
	StringBuffer buf = new StringBuffer();
	while ((i = word.indexOf("=?", start)) >= 0) {
	    buf.append(word.substring(start, i));
	    // find first '?' after opening '=?' - end of charset
	    int end = word.indexOf('?', i + 2);
	    if (end < 0)
		break;
	    // find next '?' after that - end of encoding
	    end = word.indexOf('?', end + 1);
	    if (end < 0)
		break;
	    // find terminating '?='
	    end = word.indexOf("?=", end + 1);
	    if (end < 0)
		break;
	    String s = word.substring(i, end + 2);
	    try {
		s = decodeWord(s);
	    } catch (ParseException pex) {
		// ignore it, just use the original string
	    }
	    buf.append(s);
	    start = end + 2;
	}
	if (start == 0)
	    return word;
	if (start < word.length())
	    buf.append(word.substring(start));
	return buf.toString();
    }

	
	/**
     * The string is parsed using the rules in RFC 2047 for parsing
     * an "encoded-word". If the parse fails, a ParseException is 
     * thrown. Otherwise, it is transfer-decoded, and then 
     * charset-converted into Unicode. If the charset-conversion
     * fails, an UnsupportedEncodingException is thrown.<p>
     *
     * @param	eword	the encoded value
     * @exception       ParseException if the string is not an
     *			encoded-word as per RFC 2047.
     * @exception       UnsupportedEncodingException if the charset
     *			conversion failed.
     */
    public static String decodeWord(String eword)
		throws ParseException, UnsupportedEncodingException {

	if (!eword.startsWith("=?")) // not an encoded word
	    throw new ParseException(
		"encoded word does not start with \"=?\": " + eword);
	
	// get charset
	int start = 2; int pos; 
	if ((pos = eword.indexOf('?', start)) == -1)
	    throw new ParseException(
		"encoded word does not include charset: " + eword);
	String charset = javaCharset(eword.substring(start, pos));

	// get encoding
	start = pos+1;
	if ((pos = eword.indexOf('?', start)) == -1)
	    throw new ParseException(
		"encoded word does not include encoding: " + eword);
	String encoding = eword.substring(start, pos);

	// get encoded-sequence
	start = pos+1;
	if ((pos = eword.indexOf("?=", start)) == -1)
	    throw new ParseException(
		"encoded word does not end with \"?=\": " + eword);
	/*
	 * XXX - should include this, but leaving it out for compatibility...
	 *
	if (decodeStrict && pos != eword.length() - 2)
	    throw new ParseException(
		"encoded word does not end with \"?=\": " + eword););
	 */
	String word = eword.substring(start, pos);

	try {
	    String decodedWord;
	    if (word.length() > 0) {
		// Extract the bytes from word
		ByteArrayInputStream bis = 
		    new ByteArrayInputStream(StringUtils.getBytes(word));

		// Get the appropriate decoder
		InputStream is;
		if (encoding.equalsIgnoreCase("B")) 
		    is = new BASE64DecoderStream(bis);
		else if (encoding.equalsIgnoreCase("Q"))
		    is = new QDecoderStream(bis);
		else
		    throw new UnsupportedEncodingException(
				    "unknown encoding: " + encoding);

		// For b64 & q, size of decoded word <= size of word. So
		// the decoded bytes must fit into the 'bytes' array. This
		// is certainly more efficient than writing bytes into a
		// ByteArrayOutputStream and then pulling out the byte[]
		// from it.
		int count = bis.available();
		byte[] bytes = new byte[count];
		// count is set to the actual number of decoded bytes 
		count = is.read(bytes, 0, count);

		// Finally, convert the decoded bytes into a String using
		// the specified charset
		decodedWord = count <= 0 ? "" :
				new String(bytes, 0, count, charset);
	    } else {
		// no characters to decode, return empty string
		decodedWord = "";
	    }
	    if (pos + 2 < eword.length()) {
		// there's still more text in the string
		String rest = eword.substring(pos + 2);
		if (!decodeStrict)
		    rest = decodeInnerWords(rest);
		decodedWord += rest;
	    }
	    return decodedWord;
	} catch (UnsupportedEncodingException uex) {
	    // explicitly catch and rethrow this exception, otherwise
	    // the below IOException catch will swallow this up!
	    throw uex;
	} catch (IOException ioex) {
	    // Shouldn't happen.
	    throw new ParseException(ioex.toString());
	} catch (IllegalArgumentException iex) {
	    /* An unknown charset of the form ISO-XXX-XXX, will cause
	     * the JDK to throw an IllegalArgumentException ... Since the
	     * JDK will attempt to create a classname using this string,
	     * but valid classnames must not contain the character '-',
	     * and this results in an IllegalArgumentException, rather than
	     * the expected UnsupportedEncodingException. Yikes
	     */
	    throw new UnsupportedEncodingException(charset);
	}
    }
	
	/**
     * Decode "unstructured" headers, that is, headers that are defined
     * as '*text' as per RFC 822. <p>
     *
     * The string is decoded using the algorithm specified in
     * RFC 2047, Section 6.1. If the charset-conversion fails
     * for any sequence, an UnsupportedEncodingException is thrown.
     * If the String is not an RFC 2047 style encoded header, it is
     * returned as-is <p>
     *
     * Example of usage:
     * <p><blockquote><pre>
     *
     *  MimePart part = ...
     *  String rawvalue = null;
     *  String  value = null;
     *  try {
     *    if ((rawvalue = part.getHeader("X-mailer")[0]) != null)
     *      value = MimeUtility.decodeText(rawvalue);
     *  } catch (UnsupportedEncodingException e) {
     *      // Don't care
     *      value = rawvalue;
     *  } catch (MessagingException me) { }
     *
     *  return value;
     *
     * </pre></blockquote><p>
     *
     * @param	etext	the possibly encoded value
     * @exception       UnsupportedEncodingException if the charset
     *			conversion failed.
     */
    public static String decodeText(String etext)
		throws UnsupportedEncodingException {
	/*
	 * We look for sequences separated by "linear-white-space".
	 * (as per RFC 2047, Section 6.1)
	 * RFC 822 defines "linear-white-space" as SPACE | HT | CR | NL.
	 */
	String lwsp = " \t\n\r";
	StringTokenizer st;

	/*
	 * First, lets do a quick run thru the string and check
	 * whether the sequence "=?"  exists at all. If none exists,
	 * we know there are no encoded-words in here and we can just
	 * return the string as-is, without suffering thru the later 
	 * decoding logic. 
	 * This handles the most common case of unencoded headers 
	 * efficiently.
	 */
	if (etext.indexOf("=?") == -1)
	    return etext;

	// Encoded words found. Start decoding ...

	st = new StringTokenizer(etext, lwsp, true);
	StringBuffer sb = new StringBuffer();  // decode buffer
	StringBuffer wsb = new StringBuffer(); // white space buffer
	boolean prevWasEncoded = false;

	while (st.hasMoreTokens()) {
	    char c;
	    String s = st.nextToken();
	    // If whitespace, append it to the whitespace buffer
	    if (((c = s.charAt(0)) == ' ') || (c == '\t') ||
		(c == '\r') || (c == '\n'))
		wsb.append(c);
	    else {
		// Check if token is an 'encoded-word' ..
		String word;
		try {
		    word = decodeWord(s);
		    // Yes, this IS an 'encoded-word'.
		    if (!prevWasEncoded && wsb.length() > 0) {
			// if the previous word was also encoded, we
			// should ignore the collected whitespace. Else
			// we include the whitespace as well.
			sb.append(wsb);
		    }
		    prevWasEncoded = true;
		} catch (ParseException pex) {
		    // This is NOT an 'encoded-word'.
		    word = s;
		    // possibly decode inner encoded words
		    if (!decodeStrict) {
			String dword = decodeInnerWords(word);
			if (dword != word) {
			    // if a different String object was returned,
			    // decoding was done.
			    if (prevWasEncoded && word.startsWith("=?")) {
				// encoded followed by encoded,
				// throw away whitespace between
			    } else {
				// include collected whitespace ..
				if (wsb.length() > 0)
				    sb.append(wsb);
			    }
			    // did original end with encoded?
			    prevWasEncoded = word.endsWith("?=");
			    word = dword;
			} else {
			    // include collected whitespace ..
			    if (wsb.length() > 0)
				sb.append(wsb);
			    prevWasEncoded = false;
			}
		    } else {
			// include collected whitespace ..
			if (wsb.length() > 0)
			    sb.append(wsb);
			prevWasEncoded = false;
		    }
		}
		sb.append(word); // append the actual word
		wsb.setLength(0); // reset wsb for reuse
	    }
	}
	sb.append(wsb);		// append trailing whitespace
	return sb.toString();
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
	
	/**
     * Encode a RFC 822 "word" token into mail-safe form as per
     * RFC 2047. <p>
     *
     * The given Unicode string is examined for non US-ASCII
     * characters. If the string contains only US-ASCII characters,
     * it is returned as-is.  If the string contains non US-ASCII
     * characters, it is first character-encoded using the platform's
     * default charset, then transfer-encoded using either the B or 
     * Q encoding. The resulting bytes are then returned as a Unicode 
     * string containing only ASCII  characters. <p>
     * 
     * This method is meant to be used when creating RFC 822 "phrases".
     * The InternetAddress class, for example, uses this to encode
     * it's 'phrase' component.
     *
     * @param	word	Unicode string
     * @return	Array of Unicode strings containing only US-ASCII 
     *		characters.
     * @exception UnsupportedEncodingException if the encoding fails
     */
    public static String encodeWord(String word) 
			throws UnsupportedEncodingException {
	return encodeWord(word, null, null);
    }

    /**
     * Encode a RFC 822 "word" token into mail-safe form as per
     * RFC 2047. <p>
     *
     * The given Unicode string is examined for non US-ASCII
     * characters. If the string contains only US-ASCII characters,
     * it is returned as-is.  If the string contains non US-ASCII
     * characters, it is first character-encoded using the specified
     * charset, then transfer-encoded using either the B or Q encoding.
     * The resulting bytes are then returned as a Unicode string 
     * containing only ASCII characters. <p>
     * 
     * @param	word	Unicode string
     * @param	charset	the MIME charset
     * @param	encoding the encoding to be used. Currently supported
     *		values are "B" and "Q". If this parameter is null, then
     *		the "Q" encoding is used if most of characters to be
     *		encoded are in the ASCII charset, otherwise "B" encoding
     *		is used.
     * @return	Unicode string containing only US-ASCII characters
     * @exception UnsupportedEncodingException if the encoding fails
     */
    public static String encodeWord(String word, String charset, 
				    String encoding)
    			throws UnsupportedEncodingException {
	return encodeWord(word, charset, encoding, true);
    }
    
    public static String getDefaultJavaCharset() {
    	return "8859_2";
    }
    
    public static String getDefaultMIMECharset() {
    	return "8859_2";
    }
    
    /**
     * Convert a java charset into its MIME charset name. <p>
     *
     * Note that a future version of JDK (post 1.2) might provide
     * this functionality, in which case, we may deprecate this
     * method then.
     *
     * @param   charset    the JDK charset
     * @return      	the MIME/IANA equivalent. If a mapping
     *			is not possible, the passed in charset itself
     *			is returned.
     * @since		JavaMail 1.1
     */
    public static String mimeCharset(String charset) {
	if (java2mime == null || charset == null) 
	    // no mapping table or charset param is null
	    return charset;

	String alias =
	    (String)java2mime.get(charset.toLowerCase(Locale.ENGLISH));
	return alias == null ? charset : alias;
    }
    
    /*
     * Encode the given string. The parameter 'encodingWord' should
     * be true if a RFC 822 "word" token is being encoded and false if a
     * RFC 822 "text" token is being encoded. This is because the 
     * "Q" encoding defined in RFC 2047 has more restrictions when
     * encoding "word" tokens. (Sigh)
     */ 
    private static String encodeWord(String string, String charset,
				     String encoding, boolean encodingWord)
			throws UnsupportedEncodingException {

	// If 'string' contains only US-ASCII characters, just
	// return it.
	int ascii = checkAscii(string);
	if (ascii == ALL_ASCII)
	    return string;

	// Else, apply the specified charset conversion.
	String jcharset;
	if (charset == null) { // use default charset
	    jcharset = getDefaultJavaCharset(); // the java charset
	    charset = getDefaultMIMECharset(); // the MIME equivalent
	} else // MIME charset -> java charset
	    jcharset = javaCharset(charset);

	// If no transfer-encoding is specified, figure one out.
	if (encoding == null) {
	    if (ascii != MOSTLY_NONASCII)
		encoding = "Q";
	    else
		encoding = "B";
	}

	boolean b64;
	if (encoding.equalsIgnoreCase("B")) 
	    b64 = true;
	else if (encoding.equalsIgnoreCase("Q"))
	    b64 = false;
	else
	    throw new UnsupportedEncodingException(
			"Unknown transfer encoding: " + encoding);

	StringBuffer outb = new StringBuffer(); // the output buffer
	doEncode(string, b64, jcharset, 
		 // As per RFC 2047, size of an encoded string should not
		 // exceed 75 bytes.
		 // 7 = size of "=?", '?', 'B'/'Q', '?', "?="
		 75 - 7 - charset.length(), // the available space
		 "=?" + charset + "?" + encoding + "?", // prefix
		 true, encodingWord, outb);

	return outb.toString();
    }
    
    private static void doEncode(String string, boolean b64, 
    		String jcharset, int avail, String prefix, 
    		boolean first, boolean encodingWord, StringBuffer buf) 
    			throws UnsupportedEncodingException {

    	// First find out what the length of the encoded version of
    	// 'string' would be.
    	byte[] bytes = string.getBytes(jcharset);
    	int len;
    	if (b64) // "B" encoding
    	    //len = BEncoderStream.encodedLength(bytes);
    		len = 0;
    	else // "Q"
    	    //len = QEncoderStream.encodedLength(bytes, encodingWord);
    		len = 0;
    	
    	int size;
    	if ((len > avail) && ((size = string.length()) > 1)) { 
    	    // If the length is greater than 'avail', split 'string'
    	    // into two and recurse.
    	    doEncode(string.substring(0, size/2), b64, jcharset, 
    		     avail, prefix, first, encodingWord, buf);
    	    doEncode(string.substring(size/2, size), b64, jcharset,
    		     avail, prefix, false, encodingWord, buf);
    	} else {
    	    // length <= than 'avail'. Encode the given string
    	    ByteArrayOutputStream os = new ByteArrayOutputStream();
    	    OutputStream eos; // the encoder
    	    if (b64) // "B" encoding
//    		eos = new BEncoderStream(os);
    	    eos = null;	
    	    else // "Q" encoding
//    		eos = new QEncoderStream(os, encodingWord);
    	    eos = null;
    	    	
    	    try { // do the encoding
    		eos.write(bytes);
    		eos.close();
    	    } catch (IOException ioex) { }

    	    byte[] encodedBytes = os.toByteArray(); // the encoded stuff
    	    // Now write out the encoded (all ASCII) bytes into our
    	    // StringBuffer
    	    if (!first) // not the first line of this sequence
    		if (foldEncodedWords)
    		    buf.append("\r\n "); // start a continuation line
    		else
    		    buf.append(" "); // line will be folded later

    	    buf.append(prefix);
    	    for (int i = 0; i < encodedBytes.length; i++)
    		buf.append((char)encodedBytes[i]);
    	    buf.append("?="); // terminate the current sequence
    	}
    }
	
    public static String getHostFromAddress(String address) {
    	String[] ret = address.split("@");
    	if (ret.length==2) return ret[1];
    	else return null;
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
