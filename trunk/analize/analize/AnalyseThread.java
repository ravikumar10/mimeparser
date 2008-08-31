package analize;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.apache.log4j.Logger;

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
	
	public static Logger logger = Logger.getLogger("log");
	
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
		
		logger.info("Starting analysing new message ...");
		
		//running parser of the message 
		ByteArrayInputStream bais = new ByteArrayInputStream(queueMessage.getMessageBuffer());
		MimeMessage mm = null;
		try {
			logger.debug("Starting parsing message");
			long beginTime = System.currentTimeMillis();
			mm = new MimeMessage(bais, queueMessage.getSenders(), queueMessage.getReceivers());
			long endTime = System.currentTimeMillis();
			logger.debug("End of parsing message from " + mm.getSenders().iterator().next());
			logger.debug("Parsing took [ " + (endTime-beginTime) + " ] miliseconds");
		} catch (ParseException e) {
			logger.error("Parsing error " + e.toString());
			return;
		} catch (MimeMessageHeaderException e) {
			logger.error("Parsing error " + e.toString());
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
			logger.info("Dropping message - drop rule " + droppingRules.iterator().next().toString());
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
		logger.info("Analysing done  ...");

	}
}
