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
	private Long lastClock;

	public void initialize(int processId, int numberOfmember)
			throws IOException {
		procid = processId;
		numOfMember = numberOfmember;
		vectorAck = new VectorAck(numberOfmember, processId);
		history = new HistoryBuffer(processId, numberOfmember);
		// open and initialize the ip multicast socket
		// Which port should we listen to
		int gPort = MULTICAST_GROUP_PORT;
		// Which address
		String groupAddr = IP_MULTICAST_GROUP;
		multicastSocket = new MulticastSocket(gPort);
		multicastSocket.joinGroup(InetAddress.getByName(groupAddr));
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
					((VectorAck)vectorAck.clone()));
			history.record(rMsg);
		}
		pool.execute(new Sender(rMsg));
	}

	protected void onMulticastReceived(RMessage msg) {
		if (msg instanceof RNack) {
			elaborateNackReceived((RNack) msg);
			return;
		}
		RMessageContent m=(RMessageContent) msg;
		elaborateContentMessage( m);

	}

	private void elaborateContentMessage( RMessageContent m) {
		int msgProcid = m.getProcessId();
		long msgclock = m.getClock();
		if(vectorAck.getLastClockFrom(msgProcid)<=m.getClock()){
			return;//it is a duplicate
		}
		if (vectorAck.updateIfCorrect(msgProcid, msgclock)) {
			putInDeliveryQueue(m);
		} else {
			putInHoldbackQueue(m);
		}
		checkIfNackIsToBeSent(m);
		checkAndUpdateHoldbackQueue(m);
		checkAndUpdateHistory(m);
		
		
	}

	private void checkIfNackIsToBeSent(RMessageContent m) {
		// TODO Auto-generated method stub
		//the message can be in holdback if not ask for re-sending
		
	}

	private void elaborateNackReceived(RNack msg) {
		// TODO Auto-generated method stub

	}

	private void checkAndUpdateHistory(RMessageContent msg) {

		VectorAck v = msg.getPiggyBackAcks();
		int pid = v.getProcessId();
		long lastSeen = v.getLastClockFrom(procid);
		history.trimIfYouCan(pid, lastSeen);

	}
	
	private void checkAndUpdateHoldbackQueue(RMessage msg) {
		int pid = msg.getProcessId();
		long lastseenclock = msg.getClock();
		if(vectorAck.getLastClockFrom(pid)<=msg.getClock()){
			return;
		}
		boolean found;
		do {
			found=false;
			for (int i = 0; i < holdback.size(); i++) {
				if (pid == holdback.get(i).getProcessId()
						&& lastseenclock == holdback.get(i).getClock() + 1) {
					found = true;
					vectorAck.update(pid, lastseenclock);
					putInDeliveryQueue(holdback.get(i));
					holdback.remove(i);
					lastseenclock++;
				}
			}
		} while (found);

	}

	private void putInHoldbackQueue(RMessageContent msg) {
		holdback.add(msg);
	}

	private void putInDeliveryQueue(RMessageContent msg) {
		synchronized (delivery) {
			delivery.add(msg.getPayLoad());
			delivery.notifyAll();
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

	public class Sender implements Runnable {

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
