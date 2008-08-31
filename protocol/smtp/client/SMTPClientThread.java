package smtp.client;

import smtp.SMTPSendingFailedException;
import mail.MimeMessage;

public class SMTPClientThread implements Runnable {
	
	private SMTPClient smtpClient;
	private MimeMessage mimeMessage;
	
	public SMTPClientThread(SMTPClient smtpClient, MimeMessage mimeMessage) {
		this.smtpClient = smtpClient;
		this.mimeMessage = mimeMessage;
	}
	
	@Override
	public void run() {
		try {
			System.out.println("Connecting");
			smtpClient.connect();
		} catch (SMTPSendingFailedException e) {
			System.out.println("Sending failed");
		}
		System.out.println("Sending");
		smtpClient.sendMessage(mimeMessage);
		System.out.println("Closing");
		smtpClient.close();
	}

}
