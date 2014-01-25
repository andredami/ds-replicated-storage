/**
 * 
 */
package repst;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Jacopo
 * 
 */
public class Channel {

	private Long lastSequence;
	private Long mySequence;
	private Long processId;

	private LinkedList<Message> deliveryQueue = new LinkedList<Message>();
	private LinkedList<Message> holdBackQueue = new LinkedList<Message>();
	private ExecutorService pool = Executors.newCachedThreadPool();

	public void initialize() {
		//TODO open and initialize the ip multicast socket 
		// TODO remote call to sequencer getNewProcessId() and stores it in
		// processId
		pool.execute(readFromSocket);
	}

	

	
	private void onMulticastReceived(OrderedMessage msg) {
		if (msg.sequenceNumber == lastSequence + 1) {
			lastSequence=msg.sequenceNumber;
			putInDeliveryQueue(msg);
		} else {
			discardAndSendNack();
		}
	}

	private Runnable readFromSocket = new Runnable() {

		@Override
		public void run() {
			while (true) {
				// TODO read from socket and call onMulticastReceived
			}

		}
	};

	private void discardAndSendNack() {
		// TODO Auto-generated method stub

	}

	/**
	 * Performed in single thread readFromSocket. insert at the end of the
	 * delivery queue. If the message is in the holdbackQueue the reference to
	 * the payload is replaced.
	 * 
	 * @param msg
	 */
	private void putInDeliveryQueue(OrderedMessage msg) {
		Message toBedelivered = msg;
		if (msg.processId == processId) {
			synchronized (holdBackQueue) {
				Iterator<Message> i = holdBackQueue.iterator();
				Message m = null;
				while (i.hasNext()) {
					m = i.next();
					if (m.messageId == msg.messageId) {
						toBedelivered = new OrderedMessage(msg.sequenceNumber,
								m);// keep the same
									// reference
						i.remove();
						break;
					}
				}
			}
		}

		deliveryQueue.addLast(toBedelivered);
		deliveryQueue.notifyAll();
	}

	/**
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public Serializable pop() throws InterruptedException {
		Serializable s = null;
		synchronized (deliveryQueue) {
			while (deliveryQueue.isEmpty()) {
				deliveryQueue.wait();
			}
			Message m = deliveryQueue.poll();
			s = m.payload;
		}
		return s;

	}

	/**
	 * Asynchronous write operation: only performs the call to the sequencer.
	 * Ensures that the same reference of payload is returned back when reading
	 * from the channel: in this way the server is not aware of the group and we
	 * do not have conflicts in tell apart my payload from the payload of
	 * others.
	 * 
	 * @param payload
	 */
	public synchronized void write(final Payload payload) {

		long assignedSeq;
		synchronized (mySequence) {
			assignedSeq = ++mySequence;
		}
		Message m = new Message(payload, processId, assignedSeq);
		synchronized (holdBackQueue) {
			holdBackQueue.addLast(m);
		}
		sendToSequencer(m);

	}

	private void sendToSequencer(Message m) {
		// TODO Auto-generated method stub
	}
	private void sendNack() {
		// TODO calls remote getLost... on Sequencer
	}

}
