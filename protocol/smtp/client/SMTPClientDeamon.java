package smtp.client;

import org.apache.log4j.Logger;

import queue.Queue;
import smtp.SMTPSendingFailedException;
import mail.MimeMessage;
import mail.util.MimeUtility;

/**
 * Responsible for getting messages from out queue and sending them
 * @author zbychu
 *
 */
public class SMTPClientDeamon implements Runnable {

	private Queue queue = Queue.getQueue("out_queue");
	private int SLEEP_TIME = 3000;
	private int SMTP_PORT = 5679;
	
	public static Logger logger = Logger.getLogger("log");
	
	@Override
	public void run() {
		
		logger.info("Starting sending deamon ...");
		
		while(true) {
			
			Object queueElement = queue.getMessageFromQueue();
			if (queueElement!=null) {
				MimeMessage mm = (MimeMessage) queueElement;
				
				String sender = mm.getSenders().iterator().next();
				if (sender==null) {
					logger.error("No senders");
					continue;
				}
				String host = MimeUtility.getHostFromAddress(sender);
				if (host==null) {
					logger.error("No host in senders");
					continue;
				}
				SMTPClient client = new SMTPClient(host, SMTP_PORT, mm.getSenders(), mm.getReceivers());// sending message
				
				logger.info("Sending message");
//				try {
//					client.connect();
//					client.sendMessage(mm);
//					client.close();
//				} catch (SMTPSendingFailedException e) {
//					e.printStackTrace();
//				}
				logger.info("Sent message");
			} else {
				logger.info("Nothing in out_queue - sleeping for " + SLEEP_TIME + " ");
				try {
					Thread.sleep(this.SLEEP_TIME);
				} catch (InterruptedException e) {
					logger.error("Nie udalo sie uspic watku");
					continue;
				}
			}
		}
	}
}
