package analize;

import java.io.ByteArrayInputStream;
import java.util.List;

import queue.Queue;
import queue.QueueMessage;

import mail.MimeMessage;
import mail.MimeMessageHeader;
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

	private QueueMessage queueMessage;
	private Configuration configuration;
//	private Analyser analyser;
	private static String HEADER = "Received: from OK by OK id OK";
	private boolean addHeader = true;
	
	private Queue queue = Queue.getQueue("out_queue");
	
	public AnalyseThread(QueueMessage queueMessage, Configuration configuration) {
		this.queueMessage = queueMessage;
		this.configuration = configuration;
	}
	
//	public List<Rule> getResults() {
//		if (analyser!=null) return analyser.getDroppingRules();
//		return null;
//	}
	
	@Override
	public void run() {
		
		System.out.println("Starting analysing new message ...");
		
		//running parser of the message 
		ByteArrayInputStream bais = new ByteArrayInputStream(queueMessage.getMessageBuffer());
		MimeMessage mm = null;
		try {
			mm = new MimeMessage(bais, queueMessage.getSenders(), queueMessage.getReceivers());
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
		
		
		//check if message should be dropped
		if (droppingRules.size()>0) {
			System.out.println("Message should be dropped");
		} else {
			if (addHeader) {
				//adding header to msg
				MimeMessageHeader mimeMessageHeader = new MimeMessageHeader(HEADER);
				//adds header to the beginning of the message
				mm.getHeaders().addHeader(mimeMessageHeader,0);
			}
			//putting msg to out queue
			queue.addMessageToQueue(mm);
		}
		System.out.println("Analysing done ...");

	}
}
