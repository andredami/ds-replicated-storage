/**
 * 
 */
package repst;

import java.util.List;

/**
 * @author Chiara
 * 
 */
public class Sequencer {

	private static Long nextId = (long) 0;
	private static Long nextSequence = (long) 0;
	private HistoryBuffer buffer;

	// TODO main

	public void frowardMessage(Message msg) {
		// TODO create OrderedMessage and increment nextSequence and puts it
		// into History and forward it via multicast
	}

	public void recordHartbeat(Message msg) {
		// TODO in history buffer update lastSequence
	}

	public List<OrderedMessage> getLostMessages(Long afterSequence) {
		return buffer.getAllLostMsgAfter(afterSequence);
	}

	public Long getNewProcessId() {
		// TODO blocks until start on this.main
		// TODO return and increment nextId
		// TODO create an internal lock and synchronize on it
	}
}
