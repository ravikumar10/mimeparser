package mail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import mail.exceptions.ParseException;
import mail.util.LineInputStream;
import mail.util.SharedInputStream;
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
	
	
	private final static int tmpBufferSize = 1024;  
	
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
		
		preamble = new Preamble(inputStream, boundaryLine);
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
		
		// boundary
		System.out.println("Boundary: " + boundaryLine);
		
		
		// initialize Bad Character Shift table
	    int[] bcs = initializeBadCharacterShiftTable(boundaryBytes);
	    //initialize Good Sufix Shift table
	    int[] good_suffix_shift = initializeGoodSuffixShiftTable(boundaryBytes);
		
	    //sliding window
	    byte[] slidingWindowBuffer = new byte[boundaryLenght];
	    
	    
	    // rzut na sharedinputstream'a bo tam mozna
	    // sprawdzac pozycje ;]
	    SharedInputStream sin = null;
	    if (inputStream instanceof SharedInputStream)
		    sin = (SharedInputStream)inputStream;
	    
	    
	    
	    
	    ByteArrayOutputStream bufferOut = null;
	    
	    try {
	    	//int position = inputStream.
	    	//inputStream.read(slidingWindowBuffer, 0, slidingWindowBuffer.length);
			
	    	long shiftPosition = sin.getPosition();
	    	long positionBegin = 0;
	    	//pozycja jako offset od poczatku
	    	
	    	long positionEnd = 0;
		    int i,j=0;
		    int shift=0;
		    boolean complete = false;
		    
		    byte[] tmpBuffer = new byte[tmpBufferSize];
		    int positionInTmpBuffer = 0;
		    
		    byte[] part = null; 
		    
		    int slidingWindowBufferLenght = slidingWindowBuffer.length;
		    inputStream.mark(5*slidingWindowBufferLenght);//marking
		    inputStream.read(slidingWindowBuffer, 0, slidingWindowBuffer.length);
		    
		    for(;;) {
		    	
		    	
		    	for (i=boundaryLenght-1; i>=0 && boundaryBytes[i] == slidingWindowBuffer[i]; i--) {
//		    		System.out.println(i+j);
//		    		System.out.println("Sliding window buffer \n" + new String(slidingWindowBuffer));
		    	}
		    	if (i<0) {
//		    		System.out.print("before + shift" + j);
//		    		System.out.println("J" + j);
		    		//System.out.println(new String( new byte[]{slidingWindowBuffer[i],text[j+1]}));
//		    		System.out.println("Buffor: " + new String(slidingWindowBuffer));
		    		shift = good_suffix_shift[0];
		    		positionEnd=j;
		    		j += shift; // po boundary line jest \n
		    		
		    		System.out.println("Buffer: " + new String(tmpBuffer));
		    		tmpBuffer = new byte[tmpBufferSize];
		    		positionInTmpBuffer = 0;
//		    		System.out.println("J" + j);
//		    		System.out.print("after + shift" + j);
		    		//jesli positionEnd=0 to pierwsze trafienie
		    		// w boundary line
//		    		if (positionEnd==0) {
//		    			positionBegin=positionBegin+j+shift;
//		    			System.out.println("pierwszy heat" + positionBegin);
//		    		} else {
//		    			positionEnd=j;
		    			
//		    			System.out.println("J" + j);
		    			
		    			//System.out.println("PositionBegin" + positionBegin);
		    			//System.out.println("PositionEnd" + positionEnd);
		    			//System.out.println("PB" + shiftPosition+positionBegin);
		    			//System.out.println("PE" + shiftPosition+positionEnd);
		    		    
		    			analizeAndCreatePart(tmpBuffer);
		    			//read to buffer from 
//		    		}
		    			positionBegin=j;
		    		
		    	} else {
//		    		System.out.println("I: " + i);
		    		shift = Math.max(good_suffix_shift[i], bcs[slidingWindowBuffer[i]] - boundaryLenght + 1 + i);
//		    		System.out.println("Gss: " + good_suffix_shift[i]);
//		            System.out.println("Bcs: " + (bcs[slidingWindowBuffer[i]] - boundaryLenght + 1 + i) + " " + bcs[slidingWindowBuffer[i]]);
//		    		System.out.println("Shift" + shift);
//		    		System.out.println("J" + j);
//		    		System.out.println("Before shift: " + new String(slidingWindowBuffer));
		    		j += shift;
		    		
		    		
//		    		if (j > slidingWindowBufferLenght * (how_many+1)) {
//		    			how_many++;
//		    		}
//		    		inputStream.reset();
//	    			byte[] tmpBytes = new byte[j-(how_many*slidingWindowBufferLenght)];
//		    		byte[] tmpBytes = new byte[shift];
//	    			inputStream.read(tmpBytes, 0, shift);
//	    			System.out.println("Tmp bytes: " + new String(tmpBytes));
//	    			inputStream.mark(5*slidingWindowBufferLenght);//marking
//	    			inputStream.read(slidingWindowBuffer, 0, slidingWindowBufferLenght);
//	    			System.out.println("After shift: " + new String(slidingWindowBuffer));	
//		    	System.out.println("");
		    		inputStream.reset();
		    	}
		    	
		    	//moving j positions forward in stream -> save this moving to some stream
		    	//maybe parsing meanwhile
		    	
		    	//shifting input stream
		    	
		    	
		    	//tmp bytes to bufor do ktorego czytamy cale cialo parta
    			//jeszcze zwiekszanie sie bufora obadac!!
		    	if (positionInTmpBuffer+shiftPosition>tmpBuffer.length) {
		    		//zwiekszamy bufor
		    		byte[] tmp = new byte[2*tmpBuffer.length];
		    		System.arraycopy(tmpBuffer, 0, tmp, 0, tmpBuffer.length);
		    		tmpBuffer = tmp;
		    		System.out.println("Increasing buffer");
		    	}
		    	inputStream.read(tmpBuffer, positionInTmpBuffer, shift);
		    	
		    	positionInTmpBuffer+=shift;
    			
		    	
		    	
		    	//z tego tmp czytamy piszemy do nowego buffora
		    	
    			inputStream.mark(5*slidingWindowBufferLenght);//marking
    			inputStream.read(slidingWindowBuffer, 0, slidingWindowBufferLenght);
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
	public void analizeAndCreatePart(byte[] content) {
				
		
		
		
		
	}
	
	public MimeMultiPart() {}
	
	public MimeMultiPart(InputStream inputStream) {
		this.inputStream=inputStream;
	}
	
	public MimeMultiPart(InputStream inputStream, ContentType contentType, String boundary) {
		this.contentType=contentType;
		this.inputStream=inputStream;
		this.boundaryLine=boundary;
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
        for (int i = 0; i < 255; i++) {
        	bcs[i] = m;
        }
        for (int i = 0; i < m - 1; ++i) {
        	bcs[pattern[i]] = m - i - 1;
        }
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
	
	public static void algorithmBM(byte[] pattern, byte[] text) {
		
		 int i, j;
         int m = pattern.length;
         int n = text.length;
         int shift = 0;

         int[] bcs = initializeBadCharacterShiftTable(pattern);
         int[] good_suffix_shift = initializeGoodSuffixShiftTable(pattern);
         
         String textString = new String(text);
         
         j = 0;
         while (j <= n - m) {
            for (i = m - 1; i >= 0 && pattern[i] == text[i + j]; --i);
            if (i < 0) {
               System.out.print(j + " ");
               System.out.println(new String( new byte[]{text[j],text[j+1]}));
               j += good_suffix_shift[0];
               System.out.print(j + " ");
               System.out.println(new String( new byte[]{text[j],text[j+1]}));
            }
            else {
//            	System.out.println("I: " + i);
            	shift = Math.max(good_suffix_shift[i], bcs[text[i + j]] - m + 1 + i);
//            	System.out.println("Gss: " + good_suffix_shift[i]);
//            	System.out.println("Bcs: " + (bcs[text[i + j]] - m + 1 + i) + " " + bcs[text[i + j]]);
//            	System.out.println("Shift: " + shift);
//            	System.out.println("J: " + j);
//            	System.out.println("Before shift Substring: " + textString.substring(j, j+m));
            	j +=shift;
//            	System.out.println("After shift Substring: " + textString.substring(j, j+m));
//            	System.out.println("");
            }
         }
		
	}
	
	public static void main(String[] args) {
		
		//byte[] pattern = StringUtils.getBytes("------=_NextPart_000_0063_01C82582.9D9C6DF0");
		//byte[] text = StringUtils.getBytes("fsdfhsdifsdiuhfsif fsghghjjjgjgdfid sfh oifh ds ------=_NextPart_000_0063_01C82582.9D9C6DF0 fdsffs");
		
		String sampleBoundaryString = "--===============0507542226==";
		String sampleContentString = "" +
				"Content-Type: text/plain; charset=\"utf-8\"\n" +
				"MIME-Version: 1.0\n" +
				"Content-Transfer-Encoding: base64\n" +
				"\n" +
				"RG9zdGF3YcSHIMW8eWN6ZW5pYSBvZCB6bmFqb215Y2ggemF3c3plIGplc3QgbWnFgm8hCgpEemlz\n" +
				"aWFqIE1hZ2RhIE0gbyBuaWNrdSBNYWxpbmthIG9iY2hvZHppIHVyb2R6aW55IChkYXRhIHVyb2R6\n" +
				"ZW5pYTogMTk4Ni0wNC0xNyksIHdpxJljIHplcmtuaWogbmEgcHJvZmlsIHRlaiBvc29ieTogaHR0\n" +
				"cDovL2dyb25vLm5ldC91c2Vycy8xMTAwNTE3LywgYSBwb3RlbSBzcHJhdyBqZWogcHJ6eWplbW5v\n" +
				"xZvEhyBpIHd5xZtsaWogxbx5Y3plbmlhIHByemV6IEdyb25vOiBodHRwOi8vZ3Jvbm8ubmV0L21h\n" +
				"aWxib3gvY3JlYXRlLzExMDA1MTcvLgoKWiBwb3pkcm93aWVuaWFtaSwKCkdyb25lawoKaHR0cDov\n" +
				"L2dyb25vLm5ldC8KCkplxZtsaSBuaWUgY2hjZXN6IGRvc3Rhd2HEhyB3acSZY2VqIG1haWxpIHog\n" +
				"Z3JvbmEsIHdlamTFuiBuYSBzdHJvbsSZIGh0dHA6Ly9ncm9uby5uZXQvbWFpbGJveC9zZXR0aW5n\n" +
				"cyBpIHptaWXFhCB1c3Rhd2llbmlhLg==\n" +
				"\n" +
				"--===============0507542226==\n" +
				"\n" +
				"Content-Type: text/plain; charset=\"utf-8\"\n" +
				"MIME-Version: 1.0\n" +
				"Content-Transfer-Encoding: base64\n" +
				"\n" +
				"RG9zdGF3YcSHIMW8eWN6ZW5pYSBvZCB6bmFqb215Y2ggemF3c3plIGplc3QgbWnFgm8hCgpEemlz\n" +
				"aWFqIE1hZ2RhIE0gbyBuaWNrdSBNYWxpbmthIG9iY2hvZHppIHVyb2R6aW55IChkYXRhIHVyb2R6\n" +
				"ZW5pYTogMTk4Ni0wNC0xNyksIHdpxJljIHplcmtuaWogbmEgcHJvZmlsIHRlaiBvc29ieTogaHR0\n" +
				"cDovL2dyb25vLm5ldC91c2Vycy8xMTAwNTE3LywgYSBwb3RlbSBzcHJhdyBqZWogcHJ6eWplbW5v\n" +
				"xZvEhyBpIHd5xZtsaWogxbx5Y3plbmlhIHByemV6IEdyb25vOiBodHRwOi8vZ3Jvbm8ubmV0L21h\n" +
				"aWxib3gvY3JlYXRlLzExMDA1MTcvLgoKWiBwb3pkcm93aWVuaWFtaSwKCkdyb25lawoKaHR0cDov\n" +
				"L2dyb25vLm5ldC8KCkplxZtsaSBuaWUgY2hjZXN6IGRvc3Rhd2HEhyB3acSZY2VqIG1haWxpIHog\n" +
				"Z3JvbmEsIHdlamTFuiBuYSBzdHJvbsSZIGh0dHA6Ly9ncm9uby5uZXQvbWFpbGJveC9zZXR0aW5n\n" +
				"cyBpIHptaWXFhCB1c3Rhd2llbmlhLg==\n" +
				"\n" +
				"--===============0507542226==--\n";

		
		byte[] pattern = StringUtils.getBytes(sampleBoundaryString);
		byte[] text = StringUtils.getBytes(sampleContentString);
		
		MimeMultiPart.algorithmBM(pattern, text);
		
	}
}
