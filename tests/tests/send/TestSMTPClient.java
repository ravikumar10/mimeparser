package tests.send;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import mail.MimeMessage;
import mail.exceptions.ParseException;
import mail.util.SharedFileInputStream;
import smtp.SMTPSendingFailedException;
import smtp.client.SMTPClient;
import junit.framework.TestCase;

public class TestSMTPClient extends TestCase {
	
	String resourceDirName = "resources";
	String filename = "multipart_mixed.eml";
	
	public void testSendMessage() throws SMTPSendingFailedException {
		
		String host = "127.0.0.1";
		int port = 5678;
		List<String> senders = Arrays.asList("zbychu@astronet.pl");
		List<String> receivers = Arrays.asList("mala@mala.pl");
		SharedFileInputStream is = null;
		
		try {
			is = new SharedFileInputStream(resourceDirName + "/" +filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// parsing messages
		MimeMessage mm = null;
		try {
			mm = new MimeMessage(is);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		SMTPClient client = new SMTPClient(host, port, senders, receivers);
		
		// connecting
		client.connect();
		
		//sending message
		client.sendMessage(mm);
		
		//closing connection
		client.close();
		
		
	}
	
}
