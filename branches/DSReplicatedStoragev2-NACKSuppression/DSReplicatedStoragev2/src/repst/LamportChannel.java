/**
 * 
 */
package repst;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 * 
 */
public class LamportChannel {

	private Long lamportClock = (long) 0;
	private int processId = 0;
	private int numberOfMember;

	private LinkedList<LamportMessage> deliveryQueue = new LinkedList<LamportMessage>();
	private LinkedList<LMessage> orderingQueue = new LinkedList<LMessage>();

	private ExecutorService pool = Executors.newCachedThreadPool();
	private UdpReliableChannel udpReliableChannel;

	public void initialize(int processId, int numberOfmember)
			throws IOException {
		this.processId = processId;
		this.numberOfMember = numberOfmember;
		this.udpReliableChannel = new UdpReliableChannel();
		udpReliableChannel.initialize(processId, numberOfmember);
		pool.execute(readFromUDPChannel);
	}

	private synchronized void onMulticastReceived(LMessage m) {
		long lamportMsg = m.getLamportClock();
		lamportClock = lamportClock <= lamportMsg ? lamportMsg + 1
					: lamportClock + 1;
		if (m instanceof LamportMessage) {
			putInOrderingQueue((LamportMessage) m);
			sendAck((LamportMessage) m);
		} else if (m instanceof LamportAck) {
			updateReceivedAck((LamportAck) m);
		}
	}

	private synchronized void putInOrderingQueue(LMessage newMsg) {
		if (orderingQueue.isEmpty()) {
			insertInOrderingQueue(newMsg,0);
		} else {
			boolean inserted=false;
			for (int i = 0; i < orderingQueue.size(); i++) {
				LMessage lMsginQ = orderingQueue.get(i);
				if (lMsginQ.getLamportClock() > newMsg.getLamportClock()
						|| (lMsginQ.getLamportClock() == newMsg
								.getLamportClock() && lMsginQ.getProcessId() > newMsg
								.getProcessId())) {
					insertInOrderingQueue(newMsg,i);
					inserted=true;
					break;
				}
			}
			if(!inserted){
				insertInOrderingQueue(newMsg, orderingQueue.size());
			}
		}
	}

	private synchronized void insertInOrderingQueue(LMessage newMsg,int index) {
		System.out.println("L: Inserting msg:"+newMsg.lamportclock+"."+newMsg.processId+" at "+index);
		orderingQueue.add(index, newMsg);
	}

	private synchronized void updateReceivedAck(LamportAck m) {
		System.out.println("L: Received ack for:"+m.lamportclock+"."+m.processId);
		putInOrderingQueue(m);
		if(!(orderingQueue.get(0) instanceof LamportMessage)){
			return;
		}
		LamportMessage topMsg=(LamportMessage) orderingQueue.get(0);
		ArrayList<LamportAck> acks=new ArrayList<LamportAck>(numberOfMember);
		for (int i=0;i<orderingQueue.size();i++){
			if((orderingQueue.get(i) instanceof LamportAck)){
				LamportAck ack=(LamportAck) orderingQueue.get(i);
				if(ack.isFor(topMsg)){
					acks.add(ack);
				}
			}
		}
		if(acks.size()==numberOfMember){
			orderingQueue.remove(topMsg);
			for(LamportAck a:acks){
				orderingQueue.remove(a);
			}
			putInDeliveryQueue(topMsg);
		}
		
	}

	private synchronized void sendAck(LamportMessage m) {	
		LamportAck ack = new LamportAck(processId,++lamportClock,m.getProcessId(), m.getLamportClock());
		updateReceivedAck(ack);
		udpReliableChannel.write(ack);
	}

	private Runnable readFromUDPChannel = new Runnable() {

		@Override
		public void run() {
			while (true) {
				LMessage m = null;
				try {
					m = (LMessage) udpReliableChannel.read();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				onMulticastReceived(m);
			}

		}
	};

	/**
	 * Performed in single thread readFromSocket. insert at the end of the
	 * delivery queue.
	 * 
	 * @param msg
	 */
	private void putInDeliveryQueue(LamportMessage msg) {
		
		synchronized (deliveryQueue) {
			System.out.println("L: Delivering message:"+msg.lamportclock+"."+msg.processId);
			deliveryQueue.addLast(msg);
			deliveryQueue.notifyAll();
		}
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
			LamportMessage m = deliveryQueue.poll();
			s = m.payload;
		}
		return s;

	}

	/**
	 * 
	 **/
	public synchronized void write(final Payload payload) {
		LamportMessage m;
		synchronized (lamportClock) {
			long assignedClock;
			assignedClock = ++lamportClock;
			m = new LamportMessage(payload, processId, assignedClock);
		}
		putInOrderingQueue(m);
		udpReliableChannel.write(m);
		sendAck(m);

	}

}
