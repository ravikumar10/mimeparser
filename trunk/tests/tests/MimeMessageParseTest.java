package tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import mail.MimeMessageHeader;
import mail.MimeMessageHeaders;
import junit.framework.TestCase;

public class MimeMessageParseTest extends TestCase {
	
	String resourceDirName = "resources";
	
	public void testHeadersParsing () {
		
		File dir = new File(resourceDirName);
		String[] fileNames = dir.list();
		for (int i = 0; i < fileNames.length; i++) {
			String fileName = fileNames[i];
			if (fileName.indexOf(".".charAt(0))==0) continue; //omijamy pliki katalogi z . na poczatku
			FileInputStream is = null;
			System.out.println(fileName);
			try {
				is = new FileInputStream(resourceDirName + "/" +fileName);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			MimeMessageHeaders messageHeaders = new MimeMessageHeaders();
			messageHeaders.parseAndLoadHeaders(is);
			
			/*
			 * Validating 
			 try {
				for (MimeMessageHeader header : messageHeaders.getHeaders()) {
					System.out.println("Validating header " + header.getName());
					messageHeaders.validateHeader(header);
				}
				System.out.println("-----");
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/
			for (MimeMessageHeader header : messageHeaders.getHeaders()) {
				assertNotNull(header);
			}
			
		}
	}
	
	public void testMessagePartsParsing() {
		
		
		
	}
	
}
