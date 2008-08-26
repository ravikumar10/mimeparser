package tests.send;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;
import mail.MimeMessage;
import mail.exceptions.ParseException;
import mail.util.SharedFileInputStream;

public class TestWriteMessageToOs extends TestCase {
	
	String resourceDirName = "resources";
	String filename = "multipart_mixed.eml";
	String outputFile = "test01.eml";
	
	public void testWriteToOs() {
		
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
		
		FileOutputStream fo = null;
		File f = new File(resourceDirName + "/" +outputFile);
		try {
			f.createNewFile();
			fo = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mm.writeTo(fo);
//		System.out.println(mm.toString());
//		Part rootPart = mm.getPart();
//		if (rootPart instanceof MimeMultiPart) {
//			MimeMultiPart rootPartMimeMultiPart = (MimeMultiPart) rootPart;
//			//rootPartMimeMultiPart.g
//		}
//		for (MimeMessageHeader header : mm.getPart().getHeaders().getHeaders()) {
//			System.out.println(header.getLine());
//		} 
	}
	
}
