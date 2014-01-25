/**
 * 
 */
package repst;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Jacopo
 * 
 */
public class Server implements ServerRemoteInterface {

	private StorageMap storage = new StorageMap();
	private Channel channel = new Channel();
	private List<Payload> operations = new LinkedList<Payload>();
	private ExecutorService pool = Executors.newCachedThreadPool();

	// TODO main - createChannel calls initialize
	public static void main(String[] args) {

		// take parameters?

		// initialize server object
		System.out.println("Initializing server...");
		Server server = new Server();
		server.initialize();

		// register the rmi object

	}

	private void initialize() {
		channel.initialize();
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
		Payload p = new Payload(key, value);
		synchronized (operations) {
			operations.add(operations.size(), p);
		}

		channel.write(p);// asynch call
		try {
			p.wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
			performAndnotifyWriteOp(p);
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
	private void performAndnotifyWriteOp(Payload pNew) {
		Payload pOld = null;
		boolean my = false;
		synchronized (operations) {
			Iterator<Payload> i = operations.iterator();
			while (i.hasNext()) {
				pOld = i.next();
				if (pOld == pNew) {
					i.remove();
					my = true;
					break;
				}
			}
		} // release lock on operations

		// this write is performed in a single thread: no worries about ordering
		storage.putValue(pNew.key, pNew.value);
		if (pOld != null && my == true) {
			pOld.notify();
		}
	}

}
