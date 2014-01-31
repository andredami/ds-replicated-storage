/**
 * 
 */
package repst;

import java.io.IOException;
import java.io.Serializable;
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
	private LinkedList<LamportMessage> orderingQueue = new LinkedList<LamportMessage>();
	private LinkedList<Integer> ackList = new LinkedList<Integer>();

	private ExecutorService pool = Executors.newCachedThreadPool();
	private UdpReliableChannel udpReliableChannel;
	

	public void initialize(int processId, int numberOfmember) throws IOException {
		this.processId = processId;
		this.numberOfMember = numberOfmember;
		this.udpReliableChannel = new UdpReliableChannel();
		udpReliableChannel.initialize(processId, numberOfmember);
		pool.execute(readFromUDPChannel);
	}

	private void onMulticastReceived(LMessage m) {
		if (m instanceof LamportMessage) {
			long lamportMsg = ((LamportMessage) m).getLamportClock();
			synchronized (lamportClock) {
					lamportClock = lamportClock<=lamportMsg?lamportMsg + 1:lamportClock+1;
			}
			putInOrderingQueue((LamportMessage) m);
		} else if (m instanceof LamportAck) {
			updateReceivedAck((LamportAck) m);
		}
	}
	
	private synchronized void putInOrderingQueue(LamportMessage newMsg) {
		for(int i=0;i<orderingQueue.size();i++){
			LamportMessage lMsginQ = orderingQueue.get(i);
			if(lMsginQ.getLamportClock()>newMsg.getLamportClock()
					||(lMsginQ.getLamportClock()==newMsg.getLamportClock()
					&&lMsginQ.getProcessId()>newMsg.getProcessId())){
				orderingQueue.add(i, newMsg);
				ackList.add(i, 1);//one is my own ack
				break;
			}
		}
		if(newMsg.getProcessId()!=processId){
			sendAck(newMsg);
		}

	}
	private synchronized void updateReceivedAck(LamportAck m) {
		long procid = m.getProcessId();
		long clock = m.getLamportClock();
		int msgIndex = -1;
		for (int i = 0; i < orderingQueue.size(); i++) {
			if (procid == orderingQueue.get(i).getProcessId()
					&& clock == orderingQueue.get(i).getLamportClock()) {
				msgIndex = i;
				break;
			}
		}
		Integer n = ackList.remove(msgIndex);
		n++;
		ackList.add(msgIndex, n);
		if ( msgIndex != 0) {
			return;
		}
		for (int i = 0; i < orderingQueue.size(); i++) {
			if (ackList.get(i) == numberOfMember) {
				putInDeliveryQueue(orderingQueue.get(i));
				orderingQueue.remove(i);
				ackList.remove(i);
				i--;
			}else{
				break;
			}
		}

	}

	

	private void sendAck(LamportMessage m) {
		LamportAck ack=new LamportAck(m.getProcessId(),m.getLamportClock());
		udpReliableChannel.write(ack);
	}

	private Runnable readFromUDPChannel = new Runnable() {

		@Override
		public void run() {
			while (true) {
				LamportMessage m = null;
				try {
					m = (LamportMessage) udpReliableChannel.read();
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
		putInDeliveryQueue(m);
		udpReliableChannel.write(m);

	}

}
