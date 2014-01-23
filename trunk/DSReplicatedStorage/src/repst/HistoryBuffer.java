/**
 * 
 */
package repst;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class represents the History Buffer used by the {@link Sequencer} to
 * hold a log of all forwarded {@link OrderedMessage}s not acknowledged yet.
 * 
 * @author Andrea
 * 
 */
public class HistoryBuffer {
	private static final int TRIM_AFTER_OPERATION_NUMBER = 15;

	private final int processNum;
	/**
	 * Map<sequenceNumber, Message>
	 */
	private Map<Long, Message> buffer = new HashMap<Long, Message>();
	/**
	 * Map<processId, lastSequence>
	 */
	private Map<Long, Long> lastAckVect = new HashMap<Long, Long>();

	private int operationNumSinceLastTrim = 0;

	/**
	 * Instantiate a new History Buffer
	 * 
	 * @param members
	 *            the number of replicas in the replicated storage network
	 */
	public HistoryBuffer(int members) {
		this.processNum = members;
	}

	/**
	 * Records the {@link Message} into the history buffer and updates the last
	 * acknowledged message counters. It triggers periodically the internal trim
	 * procedure that shrinks the buffer deleting the {@link Message}s that have
	 * been acknowledged by all the processes.
	 * 
	 * @param sequence
	 *            the sequence number of the {@link Message} to be recorded
	 * @param msg
	 *            the {@link Message} to be recorded
	 */
	public void record(long sequence, Message msg) {
		// Policy, if a sequence number is already associated to a message,
		// ignore all other messages registering with that sequence number
		if (!buffer.containsKey(sequence)) {
			buffer.put(sequence, msg);
			record(msg.processId, msg.lastSequence);
		} else {
			// Logging and debugging
			System.err.println("------- WARNING -------");
			System.err
					.println("Discarded message because sequence number already in history buffer");
			System.err.println("The message is:");
			System.err.println("Sequence number:" + sequence);
			System.err.println("Process id:" + msg.processId);
			System.err.println("Process message id:" + msg.messageId);
		}
	}

	private void trim() {
		operationNumSinceLastTrim = 0;
		// Trim can take place only if all the processes has already
		// acknowledged at
		// least one message
		if (lastAckVect.keySet().size() < processNum) {
			return;
		}

		// Find the maximum sequence number acknowledged by all the processes
		Long lastAckByAll = null;
		for (long v : lastAckVect.values()) {
			if (lastAckByAll == null) {
				lastAckByAll = v;
			} else if (v < lastAckByAll) {
				lastAckByAll = v;
			}
		}

		// Removing all the messages acknowledged by all the processes
		Iterator<Entry<Long, Message>> i = buffer.entrySet().iterator();
		while (i.hasNext()) {
			if (i.next().getKey() <= lastAckByAll) {
				i.remove();
			}
		}
	}

	/**
	 * Retrieves all the {@link OrderedMessage}s that were sent after a given
	 * sequence number, excluded.
	 * 
	 * @param lastSequence
	 *            the last sequence number of the last {@link OrderedMessage}
	 *            received in order.
	 * @return a sorted {@link Collection} containing all the
	 *         {@link OrderedMessage}s ordered by ascending sequence number,
	 *         starting from the sequence number subsequent of lastSequence
	 */
	public SortedSet<OrderedMessage> getAllLostMsgAfter(Long lastSequence) {
		SortedSet<OrderedMessage> res = new TreeSet<OrderedMessage>(
				new OrderedMessageComparator());
		for (Entry<Long, Message> e : buffer.entrySet()) {
			if (e.getKey() > lastSequence) {
				res.add(new OrderedMessage(e.getKey(), e.getValue()));
			}
		}
		return res;
	}

	/**
	 * Records the Heartbeat into the history buffer updating the last
	 * acknowledged message counters. It triggers periodically the internal trim
	 * procedure that shrinks the buffer deleting the {@link Message}s that have
	 * been acknowledged by all the processes.
	 * 
	 * 
	 * @param process
	 *            the process ID that sent the Heartbeat
	 * @param lastSequence
	 *            the sequence number of the {@link Message} to be recorded
	 */
	public void record(long process, long lastSequence) {
		if (!lastAckVect.containsKey(process)
				|| lastAckVect.get(process) < lastSequence) {
			lastAckVect.put(process, lastSequence);
		}
		if (operationNumSinceLastTrim > TRIM_AFTER_OPERATION_NUMBER) {
			trim();
		}
		operationNumSinceLastTrim++;
	}
}
