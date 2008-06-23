package tests;

import java.io.FileNotFoundException;
import java.io.IOException;

import analize.Analizer;
import configuration.Configuration;

import junit.framework.TestCase;

import mail.MimeMessage;
import mail.exceptions.ParseException;
import mail.util.SharedFileInputStream;

public class MessageAnalizerTest extends TestCase {
	
	public void testMessageAnalize() {
		
		String resourceDirName = "resources";
//		String[] messages = new String[]{"text_plain.eml", "test_multipart.eml", "multipart_mixed.eml"};
//		String[] messages = new String[]{"test_multipart.eml"};
		String[] messages = new String[]{"dziwne_znaczki.eml"};
		
		// loading rules
		Configuration c = new Configuration("configuration/configuration/example_configuration_file.con");
		Analizer analizer = new Analizer(c.getRules());
		
		for (String message : messages) {
			
			SharedFileInputStream is = null;
			try {
				is = new SharedFileInputStream(resourceDirName + "/" + message);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// parsing messages
			MimeMessage mm = null;
			try {
				mm = new MimeMessage(is);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			assertNotNull(mm);
			
			System.out.println("Analazing message: " + message);
			analizer.setMessage(mm);
			analizer.analize();
			
			
		}
		
	}
	
	
}
