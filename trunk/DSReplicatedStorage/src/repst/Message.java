/**
 * 
 */
package repst;

import java.io.Serializable;

/**
 * Represents an immutable Member Message sent Point-To-Point to the {@link Sequencer}
 * 
 * @author Andrea
 * 
 */
public class Message {

	private static long nextId = (long) 0;

	/**
	 * The content of the message
	 */
	public final Serializable payload;
	/**
	 * The process ID acquired during the replica configuration phase
	 */
	public final long processId;
	/**
	 * The message ID local to the replica. Do not confuse it with the sequence
	 * number present in messages sent by the sequencer
	 */
	public final long messageId;

	/**
	 * Acknowledges the highest sequence number received by the replica (Piggybacking)
	 */
	public final long lastSequence;

	/**
	 * Builds the immutable Message
	 * 
	 * @param payload The content of the message
	 * @param processId The process ID acquired during the replica configuration phase
	 * @param lastSequence Acknowledges the highest sequence number received by the replica (Piggybacking)
	 */
	public Message(Serializable payload, long processId, long lastSequence) {
		this.payload = payload;
		this.processId = processId;
		this.messageId = nextId++;
		this.lastSequence = lastSequence;
	}
	
	/**
	 * Rebuilds the immutable Message
	 * 
	 * @param message The message to be rebuilt
	 */
	public Message(Message message) {
		this.payload = message.payload;
		this.processId = message.processId;
		this.messageId = message.messageId;
		this.lastSequence = message.lastSequence;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (lastSequence ^ (lastSequence >>> 32));
		result = prime * result + (int) (messageId ^ (messageId >>> 32));
		result = prime * result + ((payload == null) ? 0 : payload.hashCode());
		result = prime * result + (int) (processId ^ (processId >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Message other = (Message) obj;
		if (lastSequence != other.lastSequence)
			return false;
		if (messageId != other.messageId)
			return false;
		if (payload == null) {
			if (other.payload != null)
				return false;
		} else if (!payload.equals(other.payload))
			return false;
		if (processId != other.processId)
			return false;
		return true;
	}
}
