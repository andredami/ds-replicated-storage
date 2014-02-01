/**
 * 
 */
package repst;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

/**
 * Keeps the messages sent from this host. for each of the messages it keeps if it is acked by all. nack are not sequenced
 **/
public class HistoryBuffer {

	public HistoryBuffer(int processId, int numberOfmember) {
		processNum=numberOfmember;
	}

	
	public synchronized RMessageContent get(long clock){
		return buffer.get(new Long(clock));
	}

	public synchronized void trimIfYouCan(int pid, long lastSeen) {
		record(pid, lastSeen);
		
	}
	
	private static final int TRIM_AFTER_OPERATION_NUMBER = 15;

	private final int processNum;
	/**
	 * Map<sequenceNumber, Message>
	 */
	private Map<Long, RMessageContent> buffer = new HashMap<Long, RMessageContent>();
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
	public synchronized void record( RMessageContent msg) {
		// Policy, if a sequence number is already associated to a message,
		// ignore all other messages registering with that sequence number
		if (!buffer.containsKey(msg.getClock())) {
			buffer.put(msg.getClock(), msg);
			record(msg.getProcessId(), msg.getClock());
		} else {
			// Logging and debugging
			System.err.println("------- WARNING -------");
			System.err
					.println("Discarded message because sequence number already in history buffer");
			System.err.println("The message is:");
			System.err.println("Process id:" + msg.getProcessId());
			System.err.println("Sequence number:" + msg.getClock());
			
			
		}
	}

	private void trim() {
		System.out.println("Trimming memory...");
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
		System.out.println("Trimmed history buffer after sequence"+lastAckByAll);

		// Removing all the messages acknowledged by all the processes
		Iterator<Entry<Long, RMessageContent>> i = buffer.entrySet().iterator();
		while (i.hasNext()) {
			if (i.next().getKey() <= lastAckByAll) {
				i.remove();
			}
		}
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
	public synchronized void record(long process, long lastSequence) {
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
