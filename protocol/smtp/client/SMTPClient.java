package smtp.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import smtp.SMTPSendingFailedException;

import mail.MimeMessage;
import mail.util.LineInputStream;

public class SMTPClient {
	
	private String host;
	private String user;
	private String password;
	private Socket serverSocket;
	private int port;
	
	private Hashtable extMap;
	private OutputStream serverOutput;
	private BufferedInputStream  serverInput;
	private LineInputStream lineInputStream;
	private String lastServerResponse;	// last SMTP response
	
	private final static String EHLO_COMMAND = "EHLO filter";
	private final static String CRLF = "\r\n";
	private final static int OK_250 = 250;
	private final static int OK_354 = 354;
	private final static int OK_220 = 220;
	private int lastReturnCode;		// last SMTP return code
	
	private List<String> senders;
	private List<String> receivers;
	private boolean isClosed = false;
	
	public SMTPClient() {}
	
	public SMTPClient(String host, int port) {
		this.host=host;
		this.port=port;
	}
	
	public SMTPClient(String host, int port, List<String> senders, List<String> receivers) {
		this.host=host;
		this.port=port;
		this.senders=senders;
		this.receivers=receivers;
	}
	
	/**
	 * sends message to client
	 */
	public void sendMessage(MimeMessage message) {
		
		try {
			mailFrom();
			rcptTo();
			data(message);
		} catch (SMTPSendingFailedException e) {
			close();
		} 
	}
	
	public void connect() throws SMTPSendingFailedException {
		
		serverSocket = new Socket();
		try {
			serverSocket.connect(new InetSocketAddress(host, port));
		} catch (IOException e) {
			System.err.println("Nieudane podlaczenie do hosta: " + host + " na porcie: " + port);
		}
		// stream initialization
		initStreams();
		
		// reading greetins from server
		int ret = readServerResponse();
		if (ret != OK_220) {
			close();
			throw new SMTPSendingFailedException();
		}
		
		// sending ehlo to server
		ehlo("filter");
	}
    
	private void mailFrom() throws SMTPSendingFailedException {
		
		for (String sender : senders) {
			sendCommand("MAIL FROM" + sender);
			int ret;
			if ((ret = readServerResponse()) != OK_250) {
				throw new SMTPSendingFailedException();
			}
			
		}
		
	}
	
	private void rcptTo() throws SMTPSendingFailedException {
		
		for (String receiver : receivers) {	
			sendCommand("RCPT TO" + receiver);
			int ret;
			if ((ret = readServerResponse()) != OK_250) {
				throw new SMTPSendingFailedException();
			}
		}
	}
	
	private void data(MimeMessage message) throws SMTPSendingFailedException {
		
		sendCommand("DATA");
		int ret;
		if ((ret = readServerResponse()) != OK_354) {
			throw new SMTPSendingFailedException();
		}
		message.writeTo(serverOutput);
		sendCommand(".");
	}
	
	public void close() {
	    
		try {
			if (!isClosed) {
				if (serverSocket != null) {
					sendCommand("QUIT");
					int resp = readServerResponse();
					isClosed = true;
					if (resp != 221 && resp != -1)
					System.out.println("DEBUG SMTP: QUIT failed with " + resp);
				}
			}
    	} finally {
    	    closeConnection();
    	}
    }
    
    private void closeConnection() {
    	try {
    	    if (serverSocket != null)
    		serverSocket.close();
    	} catch (IOException ioex) {	    // shouldn't happen
    	    System.out.println("Real problem in closing connection " + ioex.toString());
    	} finally {
    	    serverSocket = null;
    	    serverOutput = null;
    	    serverInput = null;
    	    lineInputStream = null;
    	}
    }
	
	private void initStreams() {
		try {
			serverOutput = new BufferedOutputStream(serverSocket.getOutputStream());
			serverInput = new BufferedInputStream(serverSocket.getInputStream());
			lineInputStream = new LineInputStream(serverInput);
		} catch (IOException e) {
			System.out.println("Problem with server sockets initilization");
		}
    }
	
	private void sendCommand(byte[] cmdBytes) {
        try {
		    serverOutput.write(cmdBytes);
		    serverOutput.write(CRLF.getBytes());
		    serverOutput.flush();
		} catch (IOException ex) {
		    System.err.println("Sending comman problem " + ex.toString());
		}
    }
	
	private void sendCommand(String command) {
		sendCommand(command.getBytes());
	}
	
	protected boolean ehlo(String domain) {
		String cmd;
		if (domain != null)
		    cmd = "EHLO " + domain;
		else
		    cmd = "EHLO";
		sendCommand(cmd);
		int resp = readServerResponse();
		if (resp == 250) {
		    // extract the supported service extensions
		    BufferedReader rd =
			new BufferedReader(new StringReader(lastServerResponse));
		    String line;
		    extMap = new Hashtable();
		    try {
				boolean first = true;
				while ((line = rd.readLine()) != null) {
				    if (first) {	// skip first line which is the greeting
						first = false;
						continue;
				    }
				    if (line.length() < 5)
					continue;		// shouldn't happen
				    line = line.substring(4);	// skip response code
				    int i = line.indexOf(' ');
				    String arg = "";
				    if (i > 0) {
						arg = line.substring(i + 1);
						line = line.substring(0, i);
				    }
				    
					System.out.println("DEBUG SMTP: Found extension \"" +
							    line + "\", arg \"" + arg + "\"");
				    extMap.put(line.toUpperCase(Locale.ENGLISH), arg);
				}
		    } catch (IOException ex) { }	// can't happen
		}
		return resp == 250;
    }
	
	protected int readServerResponse() {
    
		String serverResponse = "";
        int returnCode = 0;
		StringBuffer buf = new StringBuffer(100);

		// read the server response line(s) and add them to the buffer
		// that stores the response
        try {
		    String line = null;

		    do {
			line = lineInputStream.readLine();
			if (line == null) {
			    serverResponse = buf.toString();
			    if (serverResponse.length() == 0)
				serverResponse = "[EOF]";
			    lastServerResponse = serverResponse;
			    lastReturnCode = -1;
			    
				System.out.println("DEBUG SMTP: EOF: " + serverResponse);
			    return -1;
			}
			buf.append(line);
			buf.append("\n");
		    } while (isNotLastLine(line));
	            serverResponse = buf.toString();
        } catch (IOException ioex) {
		    
			System.out.println("DEBUG SMTP: exception reading response: " + ioex);
			//ioex.printStackTrace(out);
		    lastServerResponse = "";
		    lastReturnCode = 0;
            //returnCode = -1;
        }

		// print debug info
        //if (debug)
        //out.println("DEBUG SMTP RCVD: " + serverResponse);

		// parse out the return code
        if (serverResponse != null && serverResponse.length() >= 3) {
            try {
                returnCode = Integer.parseInt(serverResponse.substring(0, 3));
            } catch (NumberFormatException nfe) {
			    close();
			} catch (StringIndexOutOfBoundsException ex) {
				close();
			}
		} else {
			close();// FIXME - should it be here??
		    returnCode = -1;
		}
        
		if (returnCode == -1)
		    System.out.println("DEBUG SMTP: bad server response: " + serverResponse);
		
		System.out.println("Server response " + serverResponse);
		
        lastServerResponse = serverResponse;
		lastReturnCode = returnCode;
        return returnCode;
    }
	
    	    
    // tests if the <code>line</code> is an intermediate line according to SMTP
    private boolean isNotLastLine(String line) {
        return line != null && line.length() >= 4 && line.charAt(3) == '-';
    }
    
}
