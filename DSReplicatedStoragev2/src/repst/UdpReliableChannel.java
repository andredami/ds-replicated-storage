package repst;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class UdpReliableChannel {

	private static final int MULTICAST_GROUP_PORT = 2232;
	private static final String IP_MULTICAST_GROUP = "239.0.0.1";

	int procid;
	int numOfMember;

	VectorAck vectorAck;
	HistoryBuffer history;

	// in order, contains the message to be delivered to the upper layer
	ArrayList<Serializable> delivery = new ArrayList<Serializable>();

	// not in order, contains the massages received out of sequence
	ArrayList<RMessageContent> holdback = new ArrayList<RMessageContent>();

	private MulticastSocket multicastSocket;
	private ExecutorService pool = Executors.newCachedThreadPool();

	/**
	 * NACK-SUPRESSION EXTENSION: This pool allows the channel to schedule a
	 * future send of a NACK message and to suppress it in case of another
	 * channel signaling for the same NACK
	 */
	private ScheduledExecutorService nackPool = Executors
			.newScheduledThreadPool(numOfMember);
	private Map<RMessage, ScheduledFuture<?>> schedNacks = new HashMap<RMessage, ScheduledFuture<?>>();
	private long NACK_SUPPRESSION_DELAY_MAX = 10 * 1000;
	private long NACK_SUPPRESSION_DELAY_MIN = 3 * 1000;

	private Timer timer = new Timer(true);
	private Long lastClock = 0L;

	public void initialize(int processId, int numberOfmember)
			throws IOException {
		procid = processId;
		numOfMember = numberOfmember;
		vectorAck = new VectorAck(numberOfmember, processId);
		history = new HistoryBuffer(processId, numberOfmember);
		multicastSocket = new MulticastSocket(MULTICAST_GROUP_PORT);
		multicastSocket.joinGroup(InetAddress.getByName(IP_MULTICAST_GROUP));
		pool.execute(readFromSocket);
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				for (int i = 0; i < numOfMember; i++) {
					if (i != procid) {
						checkIfNackIsToBeSent(i);
					}
				}

			}
		}, 0, 2 * 1000);
	}

	public Serializable read() throws InterruptedException {
		Serializable s = null;
		synchronized (delivery) {
			while (delivery.isEmpty()) {
				delivery.wait();
			}
			s = delivery.remove(0);
		}
		return s;
	}

	public void write(Serializable m) {
		RMessageContent rMsg;
		synchronized (lastClock) {
			++lastClock;
			vectorAck.update(procid, lastClock);
			rMsg = new RMessageContent(m, procid, lastClock,
					((VectorAck) vectorAck.clone()));
			history.record(rMsg);
		}
		pool.execute(new Sender(rMsg));
	}

	protected void onMulticastReceived(RMessage msg) {
		if (msg instanceof RNack) {
			elaborateNackReceived((RNack) msg);
			return;
		} else if (msg instanceof RMessageContent) {
			elaborateContentMessage((RMessageContent) msg);
		}
	}

	private void elaborateContentMessage(RMessageContent m) {
		int msgProcid = m.getProcessId();
		long msgclock = m.getClock();
		if (msgProcid == procid
				|| vectorAck.getLastClockOf(msgProcid) >= m.getClock()) {
			return;// it is a duplicate or it is one of mine
		}
		if (vectorAck.updateIfCorrect(msgProcid, msgclock)) {
			putInDeliveryQueue(m);
			checkAndUpdateHoldbackQueue(m);
		} else {
			putInHoldbackQueue(m);
		}
		checkIfNackIsToBeSent(m.getProcessId());
		checkAndUpdateHistory(m);

	}

	/*
	 * Clear the holdback queue delivering the messages if possible. It is
	 * called when a message is delivered. Mantains FIFO order.
	 * 
	 * @param msg
	 */
	private void checkAndUpdateHoldbackQueue(RMessage msg) {
		int pid = msg.getProcessId();
		long lastDelivered = vectorAck.getLastClockOf(pid);
		boolean found;
		synchronized (holdback) {
			do {
				found = false;
				for (int i = 0; i < holdback.size(); i++) {
					RMessageContent alreadyReceivedMsg = holdback.get(i);
					if (pid == alreadyReceivedMsg.getProcessId()
							&& lastDelivered + 1 == alreadyReceivedMsg
									.getClock()) {
						found = true;
						vectorAck.update(pid, alreadyReceivedMsg.getClock());
						putInDeliveryQueue(alreadyReceivedMsg);
						holdback.remove(i);
						i--;
						lastDelivered++;
					}
				}
			} while (found);
		}
	}

	private void putInHoldbackQueue(RMessageContent msg) {
		synchronized (holdback) {
			if (!holdback.contains(msg)) {// based on equals in Rmessage
				System.out.println("R: in hold-back:" + msg);
				holdback.add(msg);
			}
		}
	}

	private void checkAndUpdateHistory(RMessageContent msg) {

		VectorAck v = msg.getPiggyBackAcks();
		int pid = msg.getProcessId();
		long lastSeen = v.getLastClockOf(procid);
		history.trimIfYouCan(pid, lastSeen);

	}

	private void putInDeliveryQueue(RMessageContent msg) {
		synchronized (delivery) {
			System.out.println("R: delivering:" + msg);
			delivery.add(msg.getPayLoad());
			delivery.notifyAll();
		}

	}

	/*
	 * to be called after the hold-back queue is cleaned
	 */
	private void checkIfNackIsToBeSent(int pid) {
		// the message can be in holdback if not ask for re-sending
		long lastDelivered = vectorAck.getLastClockOf(pid);
		synchronized (holdback) {
			long clocktoaskfor = lastDelivered + 1;
			for (int i = 0; i < holdback.size(); i++) {
				RMessageContent alreadyReceivedMsg = holdback.get(i);
				if (pid == alreadyReceivedMsg.getProcessId()) {
					// a message is blocked in the hold-back queue!
					scheduleNackFor(pid, clocktoaskfor);
					break;
				}
			}
		}

	}

	private synchronized void scheduleNackFor(int pid, long clock) {
		// TODO Auto-generated method stub
		RNack m = new RNack(procid, pid, clock);
		/**
		 * NACK-SUPRESSION EXTENSION
		 */
		long delay = NACK_SUPPRESSION_DELAY_MIN
				+ (int) (Math.random() * ((NACK_SUPPRESSION_DELAY_MAX - NACK_SUPPRESSION_DELAY_MIN) + 1));
		if(!schedNacks.containsKey(m)){
		schedNacks
				.put(m,
						nackPool.schedule(
								new Sender(m),
								delay,
								TimeUnit.MILLISECONDS));
		System.out.println("R: Scheduled NACK for message "+clock+"."+pid+" after " + (delay/1000) + " seconds");
		}
		// pool.execute(new Sender(m));
	}

	private synchronized void elaborateNackReceived(RNack msg) {
		System.out.println("R: received: " + msg);
		if (msg.getProcessId() == procid) {
			RMessageContent m = history.get(msg.getClock());
			m.setPiggyBackAcks((VectorAck) vectorAck.clone());
			pool.execute(new Sender(m));
		} else {
			/**
			 * NACK-SUPRESSION EXTENSION
			 */
			Iterator<Entry<RMessage, ScheduledFuture<?>>> i = schedNacks
					.entrySet().iterator();
			while (i.hasNext()) {
				Entry<RMessage, ScheduledFuture<?>> entry = i.next();
				if (entry.getValue().isDone()) {
					i.remove();
				} else if (entry.getKey().equals(msg)) {
					if(entry.getValue().cancel(false)){
						System.out.println("R: Suppressing NACK for message "+msg.getClock()+"."+msg.getProcessId());
					}
					i.remove();
				}
			}
		}

	}

	private Runnable readFromSocket = new Runnable() {

		@Override
		public void run() {
			byte[] recvBuf = new byte[1024];
			DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
			while (true) {
				// read from socket and call onMulticastReceived
				try {
					multicastSocket.receive(packet);
					ByteArrayInputStream byteStream = new ByteArrayInputStream(
							recvBuf);
					ObjectInputStream is = new ObjectInputStream(
							new BufferedInputStream(byteStream));
					RMessage msg = (RMessage) is.readObject();
					is.close();
					onMulticastReceived(msg);
				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
				}
			}

		}
	};

	private class Sender implements Runnable {

		RMessage msg;

		public Sender(RMessage ordMsg) {
			this.msg = ordMsg;
		}

		@Override
		public void run() {
			try {

				ByteArrayOutputStream byteStream = new ByteArrayOutputStream(
						1024);
				ObjectOutputStream os = new ObjectOutputStream(
						new BufferedOutputStream(byteStream));

				os.flush();
				os.writeObject(this.msg);
				os.flush();

				byte[] sendBuf = byteStream.toByteArray();
				InetAddress address = InetAddress.getByName(IP_MULTICAST_GROUP);
				DatagramPacket packet = new DatagramPacket(sendBuf,
						sendBuf.length, address, MULTICAST_GROUP_PORT);

				System.out.println("R: sending:" + msg);
				multicastSocket.send(packet);

				os.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

}
