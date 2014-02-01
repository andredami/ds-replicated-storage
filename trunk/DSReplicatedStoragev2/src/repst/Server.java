/**
 * 
 */
package repst;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.bind.ParseConversionEvent;

/**
 * @author Jacopo
 * 
 */
public class Server extends UnicastRemoteObject implements
		ServerRemoteInterface {
	
	static final long serialVersionUID = -7651487551213012175L;
	private static final String port = "1099";

	protected Server() throws RemoteException {
		super();
	}

	private StorageMap storage = new StorageMap();
	private LamportChannel channel = new LamportChannel();
	private ExecutorService pool = Executors.newCachedThreadPool();

	// TODO main - createChannel calls initialize
	public static void main(String[] args) {
		if(args.length!=2){
			System.err.println("USAGE: give me the sequencer registry address!");
			return;
		}
		int nummember=Integer.parseInt(args[1]);
		int procid=Integer.parseInt(args[0]);
		
		Registry registry;
		System.out.println("Creating a rmiregistry...");
		try {
			registry=LocateRegistry.createRegistry(1099);
		} catch (RemoteException e1) {
			e1.printStackTrace();
			return;
		}

		System.out.println("Creating remote server object...");
		Server server;
		try {
			server = new Server();
		} catch (RemoteException e) {
			e.printStackTrace();
			return;
		}
		
		System.out.println("Initializing channel...");
		try {
			
			server.initialize(procid,nummember);
		} catch (NumberFormatException | NotBoundException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

		System.out.println("Binding to rmiregistry...");
		try {
			registry.rebind("Server", server);
		} catch (RemoteException e) {
			e.printStackTrace();
			return;
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

	}

	private void initialize(int procid,int nummember) throws NotBoundException, IOException {
		channel.initialize(procid,nummember);
		pool.execute(readloop);
	}

	private Runnable readloop = new Runnable() {

		@Override
		public void run() {
			while (true) {
				fetchFormChannel();
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see repst.ServerRemoteInterface#readValue(java.lang.Integer)
	 */
	@Override
	public Integer readValue(Integer key) {

		return storage.getValue(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see repst.ServerRemoteInterface#writeValue(java.lang.Integer,
	 * java.lang.Integer)
	 */
	@Override
	public synchronized void writeValue(Integer key, Integer value) {
		System.out.println("Write request received: key="+key+" value="+value);
		Payload p = new Payload(key, value);
		channel.write(p);// asynch call
		return;

	}

	/**
	 * performed by a single thread in a while loop.(to ensure order between
	 * delivered messages.
	 */
	public void fetchFormChannel() {

		Payload p;
		try {
			p = (Payload) channel.pop();
			performAndNotifyWriteOp(p);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Remove from the pending operation list and notify the thread who is
	 * waiting for that operation.
	 * 
	 * @param pNew
	 *            the paylod delivered by channel.
	 */
	private void performAndNotifyWriteOp(Payload pNew) {
		// this write is performed in a single thread: no worries about ordering
		storage.putValue(pNew.key, pNew.value);
	}

}
