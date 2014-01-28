/**
 * 
 */
package repst;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Chiara
 * 
 */
public class Sequencer extends UnicastRemoteObject implements SequencerRemoteInterface{
	private static final int MULTICAST_GROUP_PORT = 2232;
	private static final String IP_MULTICAST_GROUP = "239.0.0.1";
	/**
	 * 
	 */
	private static final long serialVersionUID = -4620786144190360054L;
	private static final String port = "1099";

	private static Long nextId = (long) 1;
	private static Long nextSequence = (long) 1;
	private HistoryBuffer buffer;
	
	private ExecutorService pool = Executors.newCachedThreadPool();
	private static MulticastSocket multicastSocket;
	private static InetAddress address;
	private static Object lock = new Object();
	
	public Sequencer() throws RemoteException {
		super();
	}
	
	public static void main(String[] args){
		Registry registry;
		
		try {
			System.out.println("Creating a rmiregistry...");
			registry=LocateRegistry.createRegistry(1099);
		} catch (RemoteException e) {
			e.printStackTrace();
			return;
		}
		
		System.out.println("Creating remote sequencer object...");
		Sequencer sequencer;
		try {
			sequencer=new Sequencer();
		} catch (RemoteException e) {
			e.printStackTrace();
			return;
		}
		
		
		System.out.println("Binding to rmiregistry...");
		try {
			registry.rebind("Sequencer", sequencer);
		
		} catch (RemoteException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			address = InetAddress.getByName(IP_MULTICAST_GROUP);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		
		try {
			multicastSocket = new MulticastSocket(MULTICAST_GROUP_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		InetAddress mHost = null;
		try {
			mHost = InetAddress.getLocalHost();
			System.out.println("Server is operative at:"
					+ mHost.getHostAddress() + ":" + port);
		} catch (UnknownHostException e) {
			System.out
					.println("Server should be operative, but i am unable to retrieve localhost information. ");
		}
		
		
		Scanner in = new Scanner(System.in);
		
		System.out.println("Press newline to start:");
		in.nextLine();
		
		System.out.println("Ready to start with "+(nextId-1)+" replicas");
     	sequencer.buffer=new HistoryBuffer((int) (sequencer.nextId-1));
	}
	
	@Override
	public Long getNewProcessId() {
		long assignedId;
		synchronized(nextId){
			assignedId=nextId;
			nextId++;
		}
		return assignedId;
	}

	@Override
	public synchronized void forwardMessage(Message msg) {
		OrderedMessage ordMsg = new OrderedMessage(nextSequence, msg);
		buffer.record(nextSequence, ordMsg);
		nextSequence++;
		pool.execute(new Sender(ordMsg));
	}
	
	
	
	public class Sender implements Runnable{
		
		OrderedMessage msg;
		
		public Sender(OrderedMessage ordMsg) {
			this.msg=ordMsg;
		}

		@Override
		public void run() {
			try{
				
			    ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1024);
			    ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
			    
			    os.flush();
			    os.writeObject(this.msg);
			    os.flush();
			    
			    
			    byte[] sendBuf = byteStream.toByteArray();
			    DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, address, MULTICAST_GROUP_PORT);
			    int byteCount = packet.getLength();
			    multicastSocket.send(packet);
			    os.close();
			    
			}catch (IOException e){
				e.printStackTrace();
			}

		}
	}
	
	@Override
	public synchronized void recordHeartbeat(Message msg) {
		buffer.record(msg.processId, msg.lastSequence);
	}

	@Override
	public List<OrderedMessage> getLostMessages(Long afterSequence) {
		return new ArrayList<OrderedMessage>(buffer.getAllLostMsgAfter(afterSequence));
	}


}
