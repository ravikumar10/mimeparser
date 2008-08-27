package smtp.client;

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
	
	@Override
	public void run() {
		
		System.out.println("Starting sending deamon ...");
		
		while(true) {
			
			Object queueElement = queue.getMessageFromQueue();
			if (queueElement!=null) {
				MimeMessage mm = (MimeMessage) queueElement;
				
				String sender = mm.getSenders().iterator().next();
				if (sender==null) {
					System.out.println("No senders");
					continue;
				}
				String host = MimeUtility.getHostFromAddress(sender);
				if (host==null) {
					System.out.println("No host in senders");
					continue;
				}
				SMTPClient client = new SMTPClient(host, SMTP_PORT, mm.getSenders(), mm.getReceivers());// sending message
				
				System.out.println("Sending message");
//				try {
//					client.connect();
//					client.sendMessage(mm);
//					client.close();
//				} catch (SMTPSendingFailedException e) {
//					e.printStackTrace();
//				}
				System.out.println("Sent message");
			} else {
				System.out.println("Nothing in out_queue - sleeping for " + SLEEP_TIME + " ...");
				try {
					Thread.sleep(this.SLEEP_TIME);
				} catch (InterruptedException e) {
					System.err.println("Nie udalo sie uspic watku");
					continue;
				}
			}
		}
	}
}
