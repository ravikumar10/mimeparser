package tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import mail.MimeMessage;
import mail.exceptions.ParseException;
import mail.util.SharedFileInputStream;

public class MimeMessageParseTest extends TestCase {
	
	String resourceDirName = "resources";
	
	private List<String> getFileNamesToParseList() {
		
		File dir = new File(resourceDirName);
		String[] fileNames = dir.list();
		List<String> retFileNames = new ArrayList<String>();
		for (int i = 0; i < fileNames.length; i++) {
			String fileName = fileNames[i];
			if (fileName.indexOf(".".charAt(0))==0) continue; //omijamy pliki katalogi z . na poczatku
			retFileNames.add(fileName);
		}
		
		return retFileNames;
	}
	
//	public void testHeadersParsing () {
//		
//		List<String> fileNames = getFileNamesToParseList();
//		
//		for (String fileName : fileNames) {
//			FileInputStream is = null;
////			System.out.println(fileName);
//			try {
//				is = new FileInputStream(resourceDirName + "/" +fileName);
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
//			MimeMessageHeaders messageHeaders = new MimeMessageHeaders();
//			messageHeaders.parseAndLoadHeaders(is);
//			
//			/*
//			 * Validating 
//			 try {
//				for (MimeMessageHeader header : messageHeaders.getHeaders()) {
//					System.out.println("Validating header " + header.getName());
//					messageHeaders.validateHeader(header);
//				}
//				System.out.println("-----");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			*/
//			for (MimeMessageHeader header : messageHeaders.getHeaders()) {
//				assertNotNull(header);
//			}
//			
//		}
//	}
	
	// na razie tego nie odpalamy
	
//	public void testMessageParsing() {
//		
//		List<String> fileNames = getFileNamesToParseList();
//		
//		for (String fileName : fileNames) {
//			FileInputStream is = null;
//			System.out.println(fileName);
//			try {
//				is = new FileInputStream(resourceDirName + "/" +fileName);
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
//			
//			// parsing messages
//			MimeMessage mm = null;
//			try {
//				mm = new MimeMessage(is);
//			} catch (ParseException e) {
//				e.printStackTrace();
//			}
//			
//			
//			
//		}
//	}
	
	
	public void testMimeMultipartMessage() {
		
//		String filename = "text_plain.eml";
//		String filename = "test_multipart.eml";
		String filename = "multipart_mixed.eml";
//		String filename = "multipart_mixed_aware.eml";
//		String filename = "text_and_pdf.eml";
		SharedFileInputStream is = null;
//		System.out.println(filename);
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
		long begin = 0, end = 0;
		try {
			begin = System.currentTimeMillis();
			mm = new MimeMessage(is);
			end = System.currentTimeMillis();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		System.out.println("Parsing time: " + (end-begin) + " milliseconds");
		
		System.out.println(mm.toString());
		
		assertNotNull(mm.getHeaders());
		assertNotNull(mm.getPart());
		
		
	}
	
	
}
