package mail;

import java.io.IOException;
import java.io.InputStream;

import com.sun.mail.util.LineInputStream;

public class Preamble {
	
	private String preamble;
	private boolean isPreambleABondary = false;
	
	public Preamble(InputStream inputStream, String boundary) {
		parse(inputStream, boundary);
	}
	
	private void parse(InputStream inputStream, String boundary) {
		
		// Skip and save the preamble
	    LineInputStream lin = new LineInputStream(inputStream);
	    StringBuffer preamblesb = null;
	    String line;
	    String lineSeparator = null;
		
		try {
		    while ((line = lin.readLine()) != null) {
				/*
				 * Strip trailing whitespace.  Can't use trim method
				 * because it's too aggressive.  Some bogus MIME
				 * messages will include control characters in the
				 * boundary string.
				 */
				int i;
				for (i = line.length() - 1; i >= 0; i--) {
				    char c = line.charAt(i);
				    if (!(c == ' ' || c == '\t'))
					break;
				}
				line = line.substring(0, i + 1);
				if (boundary != null) {
				    if (line.equals(boundary))
					break;
				} else {
				    /*
				     * Boundary hasn't been defined, does this line
				     * look like a boundary?  If so, assume it is
				     * the boundary and save it as preambule
				     * 
				     * we set the flag in preambule tha point that
				     * this is not standard preamble but this is
				     * maybe a boundary
				     */
				    if (line.startsWith("--")) {
				    	this.isPreambleABondary=true;
				    	this.preamble=line;
				    	break;
				    } else {
				    	//TODO!! Rzucic wyjatek jakis bo tak to nie ma boundary
				    	
				    	
				    }
				}
		
				// save the preamble after skipping blank lines
				if (line.length() > 0) {
				    // if we haven't figured out what the line seprator
				    // is, do it now
				    if (lineSeparator == null) {
					    lineSeparator = "\n";
				    }
				    // accumulate the preamble
				    if (preamblesb == null)
					preamblesb = new StringBuffer(line.length() + 2);
				    preamblesb.append(line).append(lineSeparator);
				}
		    }
		} catch (IOException ex) {
			
			//TODO!! Jakos to zhandlowac
			ex.printStackTrace();
		}
		
		if (preamblesb != null)
			this.preamble = preamblesb.toString();
	}

	public String getPreamble() {
		return preamble;
	}

	public void setPreamble(String preamble) {
		this.preamble = preamble;
	}

	public boolean isPreambuleABondary() {
		return isPreambleABondary;
	}

	public void setPreambuleABondary(boolean isPreambuleABondary) {
		this.isPreambleABondary = isPreambuleABondary;
	}
	
}
