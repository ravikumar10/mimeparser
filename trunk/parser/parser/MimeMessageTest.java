package parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MimeMessageTest {
	
	
	public static void main(String[] args) {
		
		try {	
	        // create some properties and get the default Session 
	        Properties props = new Properties (  ) ; 
	        props.put ( "mail.smtp.host", "sjakis host" ) ; 
			
			Session session = Session.getDefaultInstance ( props, 
	                 new Authenticator (  )   {  
	                     public PasswordAuthentication getPasswordAuthentication (  )   {  


	                         return new PasswordAuthentication ( "user", "pass" ) ; 
	                      }  
	                  }  ) ; 
			
			FileInputStream is = null;
			try {
//				is = new FileInputStream("resources/text_plain.eml");
				is = new FileInputStream("resources/multipart_mixed.eml");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			MimeMessage message = new MimeMessage ( session, is ) ;

			//Object o =message.getContent();
			
			
			Multipart multipart = (Multipart)message.getContent();
			MimeMultipart mimeMultipart = null;
			if (multipart instanceof MimeMultipart) {
				mimeMultipart = (MimeMultipart) multipart;
			}
			
//			 Iterujemy po zawartości listu
			for (int i=0, n=mimeMultipart.getCount(); i<n; i++)
			{
				if (mimeMultipart!=null) {
					
					MimeBodyPart mimeBodyPart = (MimeBodyPart) mimeMultipart.getBodyPart(i);
//					mimeBodyPart.g
//					mimeBodyPart.getInputStream().
					int lenght = mimeBodyPart.getSize();
					byte[] buf = new byte[lenght];
					mimeBodyPart.getInputStream().read(buf, 0, lenght);
					String content = new String(buf);
					
//					System.out.println(mimeBodyPart.getHeader("Content-Type", ":"));
//					System.out.println(content);
				}
			}
	        
	        
	        
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
}
