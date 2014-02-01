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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UdpReliableChannel {

	private static final int MULTICAST_GROUP_PORT = 2232;
	private static final String IP_MULTICAST_GROUP = "239.0.0.1";

	int procid;
	int numOfMember;

	VectorAck vectorAck;
	HistoryBuffer history;
	ArrayList<Serializable> delivery = new ArrayList<Serializable>();
	ArrayList<RMessageContent> holdback = new ArrayList<RMessageContent>();

	private MulticastSocket multicastSocket;
	private ExecutorService pool = Executors.newCachedThreadPool();
	private Long lastClock=0L;

	public void initialize(int processId, int numberOfmember)
			throws IOException {
		procid = processId;
		numOfMember = numberOfmember;
		vectorAck = new VectorAck(numberOfmember, processId);
		history = new HistoryBuffer(processId, numberOfmember);
		multicastSocket = new MulticastSocket(MULTICAST_GROUP_PORT);
		multicastSocket.joinGroup(InetAddress.getByName(IP_MULTICAST_GROUP));
		pool.execute(readFromSocket);
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
			rMsg = new RMessageContent(m, procid, ++lastClock,
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
		if (vectorAck.getLastClockOf(msgProcid) >= m.getClock()) {
			return;// it is a duplicate
		}
		if (vectorAck.updateIfCorrect(msgProcid, msgclock)) {
			putInDeliveryQueue(m);

		} else {
			putInHoldbackQueue(m);
			checkIfNackIsToBeSent(m);
		}
		checkAndUpdateHoldbackQueue(m);
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
		long lastDelivered = msg.getClock();
		if (vectorAck.getLastClockOf(pid) <= msg.getClock()) {
			return;
		}
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
						lastDelivered++;
					}
				}
			} while (found);
		}
	}

	private void putInHoldbackQueue(RMessageContent msg) {
		synchronized (holdback) {
			holdback.add(msg);
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
			delivery.add(msg.getPayLoad());
			delivery.notifyAll();
		}

	}

	/*
	 * to be called after the hold-back queue is cleaned
	 */
	private void checkIfNackIsToBeSent(RMessageContent m) {
		// the message can be in holdback if not ask for re-sending
		int pid = m.getProcessId();
		long lastReceived = m.getClock();
		long lastDelivered = vectorAck.getLastClockOf(pid);
		if (lastDelivered >= lastReceived) {
			return;
		}
		synchronized (holdback) {
			for (int i = 0; i < holdback.size(); i++) {
				RMessageContent alreadyReceivedMsg = holdback.get(i);
				if (pid == alreadyReceivedMsg.getProcessId()) {
					// a message is blocked in the hold-back queue!
					scheduleNackFor(pid, lastDelivered + 1);
				}
			}
		}

	}

	private void scheduleNackFor(int pid, long clock) {
		// TODO Auto-generated method stub
		RNack m = new RNack(procid, pid, clock);
		pool.execute(new Sender(m));

	}

	private void elaborateNackReceived(RNack msg) {
		if (msg.getProcessId() == procid) {
			RMessageContent m = history.get(msg.getClock());
			pool.execute(new Sender(m));
		} else {
			// TODO suppress the scheduled nack if it is the case
			// depend on scheduleNack method
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
				multicastSocket.send(packet);
				os.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

}
