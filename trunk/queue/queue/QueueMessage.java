package queue;

import java.util.List;

public class QueueMessage {
	
	private byte[] messageBuffer;
	private List<String> senders;
	private List<String> receivers;
	
	public QueueMessage(byte[] messageBuffer, List<String> senders,
			List<String> receivers) {
		super();
		this.messageBuffer = messageBuffer;
		this.senders = senders;
		this.receivers = receivers;
	}
	
	public byte[] getMessageBuffer() {
		return messageBuffer;
	}
	public void setMessageBuffer(byte[] messageBuffer) {
		this.messageBuffer = messageBuffer;
	}
	public List<String> getSenders() {
		return senders;
	}
	public void setSenders(List<String> senders) {
		this.senders = senders;
	}
	public List<String> getReceivers() {
		return receivers;
	}
	public void setReceivers(List<String> receivers) {
		this.receivers = receivers;
	}
	
	
	
}
