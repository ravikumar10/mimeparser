package tests.send;

import helpers.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mail.MimeMessage;
import mail.exceptions.ParseException;
import mail.util.SharedFileInputStream;
import smtp.SMTPSendingFailedException;
import smtp.client.SMTPClient;
import smtp.client.SMTPClientThread;
import junit.framework.TestCase;

public class TestSMTPClient extends TestCase {
	
	String resourceDirName = "resources/tests";
//	String resourceDirName = "resources/tests1";
	// text_plain.eml multipart_mixed.eml text_and_pdf.eml multipart_text.eml multipart_mixed_aware.eml
	String filename = "multipart_mixed_aware.eml";
	
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
	
//	public void testSimultenouslySendMessage() throws SMTPSendingFailedException {
//		
//
//		String host = "127.0.0.1";
//		int port = 5678;
//		
//		List<String> receivers = Arrays.asList("mala@mala.pl");
//		List<Pair<SMTPClient, MimeMessage> > clients = new ArrayList<Pair<SMTPClient, MimeMessage> >();
//		List<String> fileNames = getFileNamesToParseList();
//		
//		int i = 0;
//		for (String fileName : fileNames) {
//		
//			SharedFileInputStream is = null;
//			
//			try {
//				is = new SharedFileInputStream(resourceDirName + "/" +fileName);
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			// parsing messages
//			MimeMessage mm = null;
//			try {
//				mm = new MimeMessage(is);
//			} catch (ParseException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			List<String> senders = Arrays.asList(fileName + "@localhost");
//			
//			SMTPClient client = new SMTPClient(host, port, senders, receivers);
//			
//			clients.add(new Pair(client, mm));
//		}
//		
//		List<Thread> threadsList = new ArrayList<Thread>();
//		for (Pair<SMTPClient, MimeMessage> smtpClient : clients) {
//			SMTPClientThread thread = new SMTPClientThread(smtpClient.getFirstObject(), smtpClient.getSecondObject());
//			Thread t = new Thread(thread);
//			threadsList.add(t);
//			t.start();
//		}
//		
//		for (Thread thread : threadsList) {
//			try {
//				thread.join();
//			} catch (InterruptedException e) {
//				System.out.println("Nieudane joinowanie");
//			}
//		}
//		//joinowanie
//		
//		
//		System.out.println("Koniec");
//	}
	
	public void testSendMessage() throws SMTPSendingFailedException {
		
		String host = "127.0.0.1";
		int port = 5678;
		List<String> senders = Arrays.asList("zbychu@localhost");
		List<String> receivers = Arrays.asList("mala@mala.pl");
		
		List<String> fileNames = getFileNamesToParseList();
		
//		for (String fileName : fileNames) {
		
			SharedFileInputStream is = null;
			
			System.out.println("Sending " + filename);
			
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
			
//		}
		
	}
	
}
