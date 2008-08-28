package smtp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import smtp.server.SMTPConnection;

public class SMTPServer implements Runnable {
	
	public static Logger logger = Logger.getLogger("log");
	
	private final static int PORT = 5678;
	private int port;
	
	public SMTPServer() {
		this.port = PORT;
	}
	
	public SMTPServer(int port) {
		this.port = port;
	}
	
	@Override
	public void run() {
		
		logger.info("Starting SMTP server ...");
		
		ServerSocket mailSocket = null;
		try {
			mailSocket = new ServerSocket(port);
		} catch (IOException e) {
			logger.error("Problem z bindowaniem do portu: " + e.toString());
			return;
		}
        
		logger.info("Listetning connections on port: " + port);
        
		while (true) {
			
			Socket SMTPSocket = null;
			SMTPConnection connection = null;
			try {
				SMTPSocket = mailSocket.accept();
				connection = new SMTPConnection(SMTPSocket);
			} catch (Exception e) {
				logger.error("Problem z akceptowaniem polaczen");
				return;
			}
			Thread thread = new Thread(connection);
			thread.start();
		}
	}
	
	public static void main(String[] args) {
		
		SMTPServer smtpServer = new SMTPServer(5679);
		Thread thread = new Thread(smtpServer);
		thread.start();
		
	}
}
