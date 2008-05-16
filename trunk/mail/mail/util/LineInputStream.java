package mail.util;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;


public class LineInputStream extends BufferedInputStream implements SharedInputStream {

	 private char[] lineBuffer = null; // reusable byte buffer

	 public LineInputStream(InputStream in) {
		super(in);
	 }

	 /**
	  * Read a line containing only ASCII characters from the input 
	  * stream. A line is terminated by a CR or NL or CR-NL sequence.
	  * A common error is a CR-CR-NL sequence, which will also terminate
	  * a line.
	  * The line terminator is not returned as part of the returned 
	  * String. Returns null if no data is available. <p>
	  *
	  * RFC822
	  */
	 public String readLine() throws IOException {
		InputStream in = this.in;
		char[] buf = lineBuffer;

		if (buf == null)
		    buf = lineBuffer = new char[128];

		int c1;
		int room = buf.length;
		int offset = 0;

		while ((c1 = in.read()) != -1) {
		    if (c1 == '\n') // Got NL, outa here.
			break;
		    else if (c1 == '\r') {
				// Got CR, is the next char NL ?
				int c2 = in.read();
				if (c2 == '\r')		// discard extraneous CR
				    c2 = in.read();
				if (c2 != '\n') {
				    // If not NL, push it back
				    if (!(in instanceof PushbackInputStream))
					in = this.in = new PushbackInputStream(in);
				    ((PushbackInputStream)in).unread(c2);
				}
				break; // outa here.
		    }

		    // Not CR, NL or CR-NL ...
		    // .. Insert the byte into our byte buffer
		    if (--room < 0) { // No room, need to grow.
		    	buf = new char[offset + 128];
		    	room = buf.length - offset - 1;
		    	System.arraycopy(lineBuffer, 0, buf, 0, offset);
		    	lineBuffer = buf;
		    }
		    buf[offset++] = (char)c1;
		}

		if ((c1 == -1) && (offset == 0))
		    return null;
		
		return String.copyValueOf(buf, 0, offset);
	 }

	@Override
	public long getPosition() {
		if (in == null)
		    throw new RuntimeException("Stream closed");
		return pos;
	}
}