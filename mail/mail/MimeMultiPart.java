package mail;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import mail.exceptions.ParseException;
import mail.util.LineInputStream;
import mail.util.StringUtils;

/**
 * Class represends the part of the e-mail message in mime format
 * Mime Part can contains more parts (it can be f.e. multipart/alternative
 * or can be simple type like image/jpeg or text/plain or text/hmtl (most common)
 *
 * @author zbychu
 *
 */

public class MimeMultiPart extends Part {
	
	/**
	 * part may contains other multiparts or can be simple part
	 */
	List<Part> parts;
	
	/**
	 * our super part (parent) null if we are top part
	 */
	MimeMultiPart parent;
	
	/**
	 * boundary line is boundary get from ContentType + "--"
	 * or if it's not present in ContentType it's
	 * first line of preambula
	 */
	String boundaryLine;
	
	/**
	 * preamble which if message is multipart is always at the beginning
	 */
	Preamble preamble;
	
	/**
	 * Main method resposible for parsing messages
	 * @throws ParseException - zmienic to na jakis inny exception 
	 */
	public void parse() throws ParseException {
		
		LineInputStream lis = new LineInputStream(this.inputStream);
		
		//parsowanie miemow jest jak szukanie wzorca w tekscie
		// wzorcem jest boundaryline a tekstem to tresc wiadomosci 
		
		preamble = new Preamble(this.inputStream, this.boundaryLine);
		//sytuacja ponizej zachodzi jest boundaryLine jest nullem
		// a premabula zawiera cos jak boundaryLine
		if (preamble.isPreambuleABondary()) this.boundaryLine=preamble.getPreamble();
		
		// nie ma boundaryLine to nie mamy zbytnio co robic
		if (this.boundaryLine==null) throw new ParseException("No boundary line");
		
		//tutaj szukamy teraz gdzie sie jest nastepne boundaryLine
		//i ten input Stream przekazujemy do analizy parta
		//korzystamy z algorytmu boyre'a moore'a
		
		
		byte[] boundaryBytes = StringUtils.getBytes(boundaryLine);
		int boundaryLenght = boundaryBytes.length;
		
		// initialize Bad Character Shift table
	    int[] bcs = initializeBadCharacterShiftTable(boundaryBytes);
	    //initialize Good Sufix Shift table
	    int[] good_suffix_shift = initializeGoodSuffixShiftTable(boundaryBytes);
		
	    //sliding window
	    byte[] slidingWindowBuffer = new byte[boundaryLenght];
	    
	    try {
			inputStream.read(slidingWindowBuffer, 0, slidingWindowBuffer.length);
		
		
		    int i,j=0;
		    int shift;
		    boolean complete = false;
		    for(;;) {
		    	
		    	for (i=boundaryLenght-1; i>=0 && boundaryBytes[i] == slidingWindowBuffer[i+j]; i--);
		    	if (i<0) {
		    		System.out.print(j + " ");
		    		shift = good_suffix_shift[0];
		    		j += shift;
		    	} else {
		    		shift = Math.max(good_suffix_shift[i], bcs[slidingWindowBuffer[i + j]] - boundaryLenght + 1 + i); 
		    		j += shift;
		    	}
		    	
		    	//moving j positions forward in stream -> save this moving to some stream
		    	//maybe parsing meanwhile
		    	
		    	//shifting input stream
		    	for (int k=0; k<shift;k++)
		    		if (inputStream.read()==-1) {
		    			complete=true;
		    			break;
		    		}
		    	if (complete) break;
		    }
	   
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    // m - pattern lenght
	    // n - text lenght
//	    j = 0;
//        while (j <= n - m) {
//           for (i = m - 1; i >= 0 && pattern[i] == text[i + j]; --i);
//           if (i < 0) {
//              System.out.print(j + " ");
//              j += good_suffix_shift[0];
//           }
//           else
//              j += Math.max(good_suffix_shift[i], bcs[text[i + j]] - m + 1 + i);
//        }
	    
	    
		
	}
	
	
	/**
	 * This method analizes if any multipart is in contentType
	 * in given input stream - if so it tries to create mimemultipart
	 * - if not just creates mimepart  
	 * @return
	 */
	public Part analizeAndCreatePart(InputStream inputStream) {
		
		Part ret = null;
		
		return ret; 
	}
	
	public MimeMultiPart() {}
	
	public MimeMultiPart(InputStream inputStream) {
		this.inputStream=inputStream;
	}
	
	public MimeMultiPart(InputStream inputStream, ContentType contentType, String boundary) {
		this.contentType=contentType;
		this.inputStream=inputStream;
		this.boundaryLine=boundaryLine;
		//boundary line
		try {
			parse();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			// TODO Na razie tak - trzeba sie zastanowic co z tym zrobic
			e.printStackTrace();
		}
	}
	
	public static int[] initializeBadCharacterShiftTable(byte[] pattern) {
		
		int[] bcs = new int[255];
		int m = pattern.length;
        for (int i = 0; i < 255; i++)
        	bcs[i] = m;

        for (int i = 0; i < m - 1; ++i)
        	bcs[pattern[i]] = m - i - 1;

		return bcs;
	}
	
	public static int[] initilizeSuffixesTable(byte[] pattern) {
		 
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
	
	public static int[] initializeGoodSuffixShiftTable(byte[] pattern) {
		
		int j = 0;
        int m = pattern.length;
        int[] good_suffix_shift = new int[m];

        int[] suff = initilizeSuffixesTable(pattern);

        for (int i = 0; i < m; i++)
        {
           good_suffix_shift[i] = m;
        }

        j = 0;
        for (int i = m - 1; i >= 0; --i)
        {
           if (suff[i] == i + 1)
           {
              for (; j < m - 1 - i; ++j)
              {
                 good_suffix_shift[j] = m - 1 - i;
              }
           }
        }

        for (int i = 0; i <= m - 2; ++i)
           good_suffix_shift[m - 1 - suff[i]] = m - 1 - i;

        return good_suffix_shift;
	}
	
	public static void algorithmBM(byte[] pattern, byte[] text) {
		
		 int i, j;
         int m = pattern.length;
         int n = text.length;

         int[] bcs = initializeBadCharacterShiftTable(pattern);
         int[] good_suffix_shift = initializeGoodSuffixShiftTable(pattern);
         
         j = 0;
         while (j <= n - m) {
            for (i = m - 1; i >= 0 && pattern[i] == text[i + j]; --i);
            if (i < 0) {
               System.out.print(j + " ");
               j += good_suffix_shift[0];
            }
            else
               j += Math.max(good_suffix_shift[i], bcs[text[i + j]] - m + 1 + i);
         }
		
	}
	
	public static void main(String[] args) {
		
		byte[] pattern = StringUtils.getBytes("mucha");
		byte[] text = StringUtils.getBytes("mala mucha ciagle mucha rucha na wiezy");
		
		MimeMultiPart.algorithmBM(pattern, text);
		System.out.println(new String(new byte[]{text[5]}));
		System.out.println(new String(new byte[]{text[18]}));
	}
}
