package analize;

import java.io.ByteArrayInputStream;
import java.util.List;

import mail.MimeMessage;
import mail.exceptions.MimeMessageHeaderException;
import mail.exceptions.ParseException;
import configuration.Configuration;

/**
 * Reponsible for parsing message and analizng the content of it
 * according to given rules
 * 
 * @author zbychu
 *
 */
public class AnalyseThread implements Runnable {

	private byte[] messageBuffer;
	private Configuration configuration;
	
	public AnalyseThread(byte[] messageBuffer, Configuration configuration) {
		this.messageBuffer = messageBuffer;
		this.configuration = configuration;
	}
	
	@Override
	public void run() {
		
		//running parser of the message 
		ByteArrayInputStream bais = new ByteArrayInputStream(messageBuffer);
		MimeMessage mm = null;
		try {
			mm = new MimeMessage(bais);
		} catch (ParseException e) {
			System.out.println("Parsing error " + e.toString());
			return;
		} catch (MimeMessageHeaderException e) {
			System.out.println("Parsing error " + e.toString());
			return;
		}
		
		//running analizer
		Analyser analyser = new Analyser(configuration.getRules());
		//setting message to analize
		analyser.setMessage(mm);
		analyser.analize();
		List<Rule> droppingRules = analyser.getDroppingRules();
		if (droppingRules.size()>0) {
			System.out.println("Message should be dropped");
		}
		
		// adding header
	}
}
