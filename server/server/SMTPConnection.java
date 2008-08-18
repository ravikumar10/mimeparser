package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

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
	
	final static String EHLO = "250-filter.mail\n250-SIZE 20480000";
	final static String HELO = "250 filter.mail";
	
	final static String NESTED_MAIL_COMMAND = "503 Error: nested MAIL command";
	final static String NEED_MAIL_COMMAND = "503 Error: need MAIL command";
	
    private Socket socket;
	DataOutputStream toClient;
	BufferedReader fromClient;

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
		boolean success = handleHeloCommand();
		boolean not_to_quit = true;
		// mail transaction
		while(true) {
			if (success) success = handleMailFromCommand();
			if (success) success = handleRcptToCommand();
			if (success) success = handleDataCommand();
			// quit command
			if (success) not_to_quit = handleQuitCommand();
			if (!not_to_quit) break;
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
		return message;
	}
	
	/**
	 * Replies with given string message to client 
	 * @param command given message (command)
	 */
	private void reply (String command){
		try {
			if (!socket.isClosed()) toClient.writeBytes(command+CRLF);			
		} catch (IOException e) {
			System.out.println("Write socket error: "+e);
		}
		//System.out.println(command);
	}

	/* This method process the message body */
	private void receiveMessage(String sender,String receiver){
		String body="";
		String line="";
//		MessageSave newMessage;

//		try {
//			do {
				/* Read each line from client */
//				line = /* Fill in */;

//				if (line == null) break;

				/* If two dots appear at the beginning of a line, some processing is needed */
//				if (line.matches(/* Fill in */)) body+=/* Fill in */;
//				else body+=/* Fill in */;

			/* Do it again until the ending delimiter is hit */
//			} while(!line.equals(/* Fill in */));
//		}catch (IOException e) {
//			System.out.println("Read socket error: "+e);
//		}
		try	{
			if (line == null) socket.close();
		} catch (IOException e)	{
			System.out.println("Close connection error: "+e);
		}

		/* If the message body is not null, call the MessageSave class to save it */
//		if (line != null) newMessage = /* Fill in */;
//		return;
		
		// PUTTING MESSAGE INTO QUEUE !!!
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
			else if (isCommand(requestCommand, "mailfrom")) analizeMailFromCommand(requestCommand);
			else if (isCommand(requestCommand, "rcptto")) analizeMailFromCommand(requestCommand);
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
			else if (isCommand(requestCommand, "mailfrom")) analizeMailFromCommand(requestCommand);
			else { reply(BAD_SYNTAX); break; }
		}
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	private boolean handleRcptToCommand() {
		
		String requestCommand;
		
		while (true) {
			requestCommand=fetch();
			if ( isCommand(requestCommand,"helo") ||  isCommand(requestCommand,"ehlo")) continue;
			else if (isQuitCommand(requestCommand)) return sayGoodbye();
			else if (isCommand(requestCommand, "mailfrom")) analizeMailFromCommand(requestCommand);
			else { reply(BAD_SYNTAX); break; }
		}
		return false;
	}
	
	private boolean handleDataCommand() {
		
		return false;
	}
	
	/**
	 * handles quit command
	 * 
	 */
	private boolean handleQuitCommand() {
		
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
		
		//NEED_MAIL_COMMAND
		
		return false;
	}
	
	/**
	 * Analizes RCPT TO command from client
	 * @param requestCommand
	 * @return
	 */
	private boolean analizeRcptToCommand(String requestCommand) {
		
		return false;
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
	
    /* Destructor. Closes the connection if something bad happens. */
    protected void finalize() throws Throwable {
	    socket.close();
		super.finalize();
    }
}
