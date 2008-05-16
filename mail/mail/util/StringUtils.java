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
	 * Initialize Bad Character Shift Table for Boyer-Moore algorithm according
	 * to given pattern
	 * 
	 * @param given pattern
	 * @return
	 */
	public static int[] initializeBMAlgorithmBadCharacterShiftTable(byte[] pattern) {
		
		int[] bcs = new int[255];
		int m = pattern.length;
        for (int i = 0; i < 255; i++) {
        	bcs[i] = m;
        }
        for (int i = 0; i < m - 1; ++i) {
        	bcs[pattern[i]] = m - i - 1;
        }
		return bcs;
	}
	
	/**
	 * Initialize Suffixes Table for Boyer-Moore algorithm according
	 * to given pattern
	 * 
	 * @param given pattern
	 * @return
	 */
	public static int[] initilizeBMAlgorithmSuffixesTable(byte[] pattern) {
		 
		 int j;
         int m = pattern.length;
         int[] suff = new int[m];

         suff[m - 1] = m;
         for (int i = m - 2; i >= 0; --i) {
            for (j = 0; j <= i && pattern[i-j] == pattern[m-j-1]; j++);
            suff[i] = j;
         }
		return suff;
	}
	
	/**
	 * Initialize Good Suffix Shift Table for Boyer-Moore algorithm according
	 * to given pattern
	 * 
	 * @param given pattern
	 * @return
	 */
	public static int[] initializeBMAlgorithmGoodSuffixShiftTable(byte[] pattern) {
		
		int j = 0;
        int m = pattern.length;
        int[] good_suffix_shift = new int[m];

        int[] suff = initilizeBMAlgorithmSuffixesTable(pattern);

        for (int i = 0; i < m; i++) {
           good_suffix_shift[i] = m;
        }
        
        j = 0;
        for (int i = m - 1; i >= 0; --i) {
           if (suff[i] == i + 1) {
              for (; j < m - 1 - i; ++j) {
                 good_suffix_shift[j] = m - 1 - i;
              }
           }
        }

        for (int i = 0; i <= m - 2; ++i) {
           good_suffix_shift[m - 1 - suff[i]] = m - 1 - i;
        }

        return good_suffix_shift;
	}
	
	/**
	 * Uses Boyer-Moore algorithm to find pattern in text
	 * return true if pattern was found
	 * @param pattern
	 * @param text
	 * @return true if pattern is in text
	 */
	public static boolean isPatternInText(byte[] pattern, byte[] text) {
		
		int i, j;
        int m = pattern.length;
        int n = text.length;
        int shift = 0;

        int[] bcs = initializeBMAlgorithmBadCharacterShiftTable(pattern);
        int[] good_suffix_shift = initializeBMAlgorithmGoodSuffixShiftTable(pattern);
        
        j = 0;
        while (j <= n - m) {
           for (i = m - 1; i >= 0 && pattern[i] == text[i + j]; --i);
           if (i < 0) return true;
           else {
        	   shift = Math.max(good_suffix_shift[i], bcs[text[i + j]] - m + 1 + i);
        	   j +=shift;
           }
        }
        return false;
	}
	
	/**
	 * Boyer moore algorithm - just for test do nothing
	 * @param pattern
	 * @param text
	 */
	public static void algorithmBM(byte[] pattern, byte[] text) {
		
		 int i, j;
         int m = pattern.length;
         int n = text.length;
         int shift = 0;

         int[] bcs = initializeBMAlgorithmBadCharacterShiftTable(pattern);
         int[] good_suffix_shift = initializeBMAlgorithmGoodSuffixShiftTable(pattern);
         
         j = 0;
         while (j <= n - m) {
            for (i = m - 1; i >= 0 && pattern[i] == text[i + j]; --i);
            if (i < 0) {
            	j += good_suffix_shift[0];
            } else {
            	shift = Math.max(good_suffix_shift[i], bcs[text[i + j]] - m + 1 + i);
            	j +=shift;
            }
         }
	}
	
	public static void main(String[] args) {
		
		String text = " mucha walczy w gnoju z rosolem na scianie";
		String pattern = "gnoj";
		System.out.println(isPatternInText(pattern.getBytes(), text.getBytes()));
		
	}
	
	
}
