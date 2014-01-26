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
	/**
	 * 
	 */
	private static final long serialVersionUID = -4620786144190360054L;
	private static final String port = "1099";

	private static Long nextId = (long) 0;
	private static Long nextSequence = (long) 0;
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
			try {
				registry.bind("Sequencer", sequencer);
			} catch (AlreadyBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (AccessException e) {
			e.printStackTrace();
			return;
		} catch (RemoteException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			address = InetAddress.getByName("225.4.5.6");
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		
		try {
			multicastSocket = new MulticastSocket(1099);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		Scanner in = new Scanner(System.in);
		synchronized(lock){
			System.out.println("Tipe start");
			in.nextLine();
		}
		
		do{
			in.nextLine();
		}while(!in.equals("finish"));
	}
	
	public synchronized Long getNewProcessId() {
		synchronized(lock){
		nextId++;
		}
		return nextId;
	}

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
			    DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, address, 1099);
			    int byteCount = packet.getLength();
			    multicastSocket.send(packet);
			    os.close();
			    
			}catch (IOException e){
				e.printStackTrace();
			}

		}
	}

	public void recordHartbeat(Message msg) {
		// TODO in history buffer update lastSequence
	}

	public List<OrderedMessage> getLostMessages(Long afterSequence) {
		return (List<OrderedMessage>) buffer.getAllLostMsgAfter(afterSequence);
	}


}
