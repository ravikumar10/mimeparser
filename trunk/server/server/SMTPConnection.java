package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Spawn a new SMTP connection for the connected client.
 *
 */
public class SMTPConnection implements Runnable {

	
	public enum ServerStates { BEFORE_HELO, BEFORE_MAIL, BEFORE_RCPT, BEFORE_DATA,
		BEFORE_QUIT }
	
	final static String GREETINGS = "220 filter.mail ESMTP";
	final static String GOODBY = "221 Bye";
	final static String BAD_SYNTAX = "500 Error: bad syntax";

//	250-PIPELINING
//	250-SIZE 20480000
//	250-ETRN
//	250-AUTH LOGIN PLAIN
//	250-AUTH=LOGIN PLAIN
//	250 8BITMIME
	
	final static String EHLO = "250-filter.mail\r\n250-SIZE 20480000\r\n250-ETRN\r\n250-AUTH LOGIN PLAIN\r\n250-AUTH=LOGIN PLAIN\r\n250 8BITMIME";
	final static String HELO = "250 filter.mail";
	
	final static String NESTED_MAIL_COMMAND = "503 Error: nested MAIL command";
	final static String NEED_MAIL_COMMAND = "503 Error: need MAIL command";
	
	final static String END_OF_MESSAGE = ".";
	
	final static String DATA_COMMAND_RESPONSE = "354 Enter message, ending with \".\" on a line by itself";
	final static String OK_250 = "250 OK";
	
    private Socket socket;
    private boolean isConnectionClosed = false; 
	private DataOutputStream toClient;
	private BufferedReader fromClient;

	private List<String> senders = new ArrayList<String>();
	private List<String> receivers = new ArrayList<String>();
	
	private static final String CRLF = "\r\n";
	
	private ServerStates serverState = ServerStates.BEFORE_HELO;

    public SMTPConnection(Socket socket) throws Exception {
		this.socket = socket;
	}

    public void run() {
		try {
			processRequest();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

    private void processRequest(){

		// creating channel between client and server
		try {
			InputStream is = socket.getInputStream();
			toClient = new DataOutputStream(socket.getOutputStream());
			InputStreamReader sr = new InputStreamReader(is);
			fromClient = new BufferedReader(sr);
		}catch (IOException e) {
			System.out.println("Initialization error: "+e);
		}
		
		// sending hello to client after connection
		displayGreetings();
		
		//setting server status
		
		
		// waiting for helo (ehlo) command from client
		boolean success = false;
		// mail transaction
		while(true) {
			//helo only first time
			if (!success) success = handleHeloCommand();
			
			if (success) success = handleMailFromCommand();
			if (success) success = handleRcptToCommand();
			if (success) success = handleDataCommand();
			// quit command
			if (success) success = handleQuitCommand();
			if (isConnectionClosed) break;
		}
		
		System.out.println("Closing connection");
		try {
			socket.close();
		}catch (IOException e) {
			System.out.println("Close connection error: "+e);
		}
	}

	/**
	 * 
	 * @return
	 */
	private String fetch(){
		String message="";
		try {
			do {
				message = fromClient.readLine();
				if (message == null) return "";
			} while (message.length()<=0);
		} catch (IOException e) {
			System.out.println("Read socket error: "+e);
		}
		System.out.println(("K: " + message));
		return message;
	}
	
	/**
	 * Replies with given string message to client 
	 * @param command given message (command)
	 */
	private void reply (String command){
		try {
			System.out.println(("S: " + command));
			if (!socket.isClosed()) toClient.writeBytes(command+CRLF);			
		} catch (IOException e) {
			System.out.println("Write socket error: "+e);
		}
		//System.out.println(command);
	}
	
	/**
	 * Handles HELO or EHLO command sent to server
	 * in response to it server sends 250 code and greetings 
	 * and information about extensions
	 * 
	 * Responses:
	 * 250 - OK 
	 * 500 501 503 - Failed
	 */
	private boolean handleHeloCommand() {
		
		String requestCommand;
		while (true) {
			requestCommand=fetch();
			if ( isCommand(requestCommand,"helo") ||  isCommand(requestCommand,"ehlo"))
				return analizeHeloCommand(requestCommand);
			else if (isQuitCommand(requestCommand)) return sayGoodbye(); 
			else { reply(BAD_SYNTAX); break; }
		}
		return false;
	}
	
	
	
	/**
	 * Handles MAIL FROM command sent to server
	 * server responses 250 code 
	 * No e-mail validation is made!!!
	 * 
	 * If hello again is sent than we replay greetings again
	 * 
	 * Responses:
	 * 250 - OK
	 */
	private boolean handleMailFromCommand() {
		
		String requestCommand;
		
		while (true) {
			requestCommand=fetch();
			if ( isCommand(requestCommand,"helo") ||  isCommand(requestCommand,"ehlo")) continue;
			else if (isQuitCommand(requestCommand)) return sayGoodbye();
			else if (isCommand(requestCommand, "mailfrom")) return analizeMailFromCommand(requestCommand);
			else { reply(BAD_SYNTAX); break; }
		}
		return false;
	}
	
	/**
	 * Handles RCPT TO command sent to server
	 * No e-mail validation is made!!!
	 * 
	 * Responses:
	 * 250 - OK
	 * 
	 * @return
	 */
	private boolean handleRcptToCommand() {
		
		String requestCommand;
		
		while (true) {
			requestCommand=fetch();
			if (isQuitCommand(requestCommand)) return sayGoodbye();
			else if (isCommand(requestCommand, "mailfrom")) analizeMailFromCommand(requestCommand);
			else if (isCommand(requestCommand, "rcptto")) return analizeRcptToCommand(requestCommand);
			else { reply(BAD_SYNTAX); break; }
		}
		return false;
	}
	
	private boolean handleDataCommand() {
		
		String requestCommand;
		
		while (true) {
			requestCommand=fetch();
			if (isQuitCommand(requestCommand)) return sayGoodbye();
			else if (isCommand(requestCommand, "rcptto")) analizeRcptToCommand(requestCommand);
			else if (isCommand(requestCommand, "data")) return analizeDataCommand(requestCommand);
			else { reply(BAD_SYNTAX); break; }
		}
		return false;
	}
	
	/**
	 * handles quit command
	 * 
	 */
	private boolean handleQuitCommand() {
		//this.isConnectionClosed=true;
		return false;
	}
	
	/**
	 * Display to client greetins after connection
	 */
	private void displayGreetings() { 
		reply(GREETINGS);
	}
	
	/**
	 * Say to client command with some faile status
	 * like 500 or 503
	 * @return
	 */
	private boolean sayFailStatusCommand(String command) {
		reply(command);
		return false;
	}
	
	/**
	 * Says bye to disconneting client
	 * @return false - returns false to jump out to socket.close()
	 */
	private boolean sayGoodbye() {
		this.isConnectionClosed = true;
		return sayFailStatusCommand(GOODBY);
	}
	
	/**
	 * Analizes helo (ehlo) command and sends to client 
	 * appriopriate output
	 * @param requestCommand
	 * @return
	 */
	private boolean analizeHeloCommand(String requestCommand) {
		
		if (isCommand(requestCommand,"helo")) reply(HELO);
		else if (isCommand(requestCommand,"ehlo")) reply(EHLO); 
		this.serverState = ServerStates.BEFORE_MAIL;
		return true;
	}
	
	/**
	 * Analizes MAIL FROM command from client
	 * @param requestCommand
	 * @return
	 */
	private boolean analizeMailFromCommand(String requestCommand) {
		
		// excluding sender and putting to senders list
		String sender = requestCommand.substring(SMTPUtils.getCommand("mailfrom").length());
		System.out.println("Sender: " + sender);
		this.senders.add(sender);
		
		reply(OK_250);
		
		// if state is BEFORE_MAIL then MAIL FROM command should change
		// state to so we return true
		if (this.serverState == ServerStates.BEFORE_MAIL) {
			this.serverState = ServerStates.BEFORE_RCPT;
			return true;
		}
		// if state is different than MAIL FROM is probably
		// when the state is BEFORE_RCPT
		return false;
	}
	
	/**
	 * Analizes RCPT TO command from client
	 * @param requestCommand
	 * @return
	 */
	private boolean analizeRcptToCommand(String requestCommand) {
		
		// excluding receiver and putting to receivers list
		String receiver = requestCommand.substring(SMTPUtils.getCommand("rcptto").length());
		System.out.println("Receiver: " + receiver);
		this.receivers.add(receiver);
		
		reply(OK_250);
		
		// if state is BEFORE_RCPT then RECPTTO command should change
		// state to so we return true
		if (this.serverState == ServerStates.BEFORE_RCPT) {
			this.serverState = ServerStates.BEFORE_DATA;
			return true;
		}
		// if state is different than MAIL FROM is probably
		// when the state is BEFORE_DATA
		return false;
	}
	
	/**
	 * Analizes DATA command 
	 * it's stops after receiving \r\n.\r\n
	 * 
	 * after data server responses 354
	 * @param requestCommand
	 * @return
	 */
	private boolean analizeDataCommand(String requestCommand) {
		
		// reply 354
		reply(DATA_COMMAND_RESPONSE);
		
		// tutaj while bo czytam maila
		boolean isEndOfMessage = false;
		String message = "", tmp = "";
		while(!isEndOfMessage) {
			tmp=fetch();
			if (!tmp.equals(END_OF_MESSAGE)) {
				message+=tmp+"\r\n";
			} else isEndOfMessage=true;
		}
		
		System.out.println("Mail is :\n" + message);
		
		// reply 250 OK id=message_id
		reply(OK_250 + " id=" + SMTPUtils.generateMessageId());
		
		return true;
	}
	
	/**
	 * Checks if given command is not Quit command
	 * @param requestCommand
	 * @return true/false
	 */
	private boolean isQuitCommand(String requestCommand) {
		return isCommand(requestCommand, "quit");
	}
	
	/**
	 * Checks if the given requestCommand is the command
	 * type
	 * 
	 * @param requestCommand - command from client 
	 * @param command - command to equals to it
	 * @return true/false
	 */
	private boolean isCommand(String requestCommand,String commandKey) {
		
		String command = SMTPUtils.getCommand(commandKey);
		if (command!=null) {
			try {
				if (requestCommand.substring(0, command.length()).equals(command)) return true;
			} catch (StringIndexOutOfBoundsException ex) {return false;}
		}
		return false;
	}
	
    protected void finalize() throws Throwable {
	    socket.close();
		super.finalize();
    }
    
    

}
