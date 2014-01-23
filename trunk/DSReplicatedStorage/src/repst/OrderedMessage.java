package repst;

/**
 * Represents a wrapper for an immutable {@link Message} to be forwarded to all
 * replicas.
 * 
 * @author Andrea
 */
public class OrderedMessage extends Message {
	/**
	 * The message sequence number
	 */
	public final long sequenceNumber;

	/**
	 * Wraps a {@link Message} in the immutable OrderedMessage to be delivered
	 * to other replicas
	 * 
	 * @param sequenceNumber
	 *            The message sequence number
	 * @param message
	 *            The message to be wrapped and forwarded
	 */
	public OrderedMessage(long sequenceNumber, Message message) {
		super(message);
		this.sequenceNumber = sequenceNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ (int) (sequenceNumber ^ (sequenceNumber >>> 32));
		return result;
	}

	/**
	 * Compares the two {@link OrderedMessage}s only with respect to their
	 * sequence number. It can be used because the sequence number must be
	 * unique for each {@link Message}
	 * 
	 * @param obj
	 *            the reference {@link OrderedMessage} with which to compare.
	 * @return {@code true} if this object is the same as the obj argument;
	 *         {@link false} otherwise.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equalsSN(OrderedMessage obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		OrderedMessage other = (OrderedMessage) obj;
		if (sequenceNumber != other.sequenceNumber)
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		OrderedMessage other = (OrderedMessage) obj;
		if (sequenceNumber != other.sequenceNumber)
			return false;
		return true;
	}
}
