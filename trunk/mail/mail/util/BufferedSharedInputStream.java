package mail.util;

import java.io.BufferedInputStream;
import java.io.InputStream;


public class BufferedSharedInputStream extends BufferedInputStream implements
		SharedInputStream {

	public BufferedSharedInputStream(InputStream in) {
		super(in);
	}

	@Override
	public long getPosition() {
		if (in == null)
		    throw new RuntimeException("Stream closed");
		return pos;
	}

}
