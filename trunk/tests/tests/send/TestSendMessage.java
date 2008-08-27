package tests.send;

import java.util.Properties;

//import javax.mail.Message;
//import javax.mail.MessagingException;
//import javax.mail.Session;
//import javax.mail.Transport;
//import javax.mail.internet.InternetAddress;
//import javax.mail.internet.MimeMessage;

import junit.framework.TestCase;

public class TestSendMessage extends TestCase {
	
	
//	public void testSendMessage() throws MessagingException  {
		
//		Properties props = new Properties();
//		props.setProperty("mail.transport.protocol", "smtp");
//		props.setProperty("mail.host", "127.0.0.1");
//		props.setProperty("mail.smtp.port", "5678");
//		props.setProperty("mail.smtp.from", "zbychu@astronet.pl");
//		props.setProperty("mail.password", "");
//
//		Session mailSession = Session.getDefaultInstance(props, null);
//		Transport transport = mailSession.getTransport();
//
//		MimeMessage message = new MimeMessage(mailSession);
//		message.setSubject("Testing javamail plain");
//		message.setContent("This is a test", "text/plain");
//		message.addRecipient(Message.RecipientType.TO,
//				new InternetAddress("elvis@presley.org"));
//		
//		transport.connect();
//		transport.sendMessage(message,
//				message.getRecipients(Message.RecipientType.TO));
//		transport.close();
//	}
}
