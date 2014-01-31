/**
 * 
 */
package repst;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface is used by the {@link Client}s to access the replicated
 * storage services through a remote method invocation.
 * 
 * @see Server
 * @category Remote Interfaces
 * @author Andrea
 */
public interface ServerRemoteInterface extends Remote {
	/**
	 * Reeds the value stored at the given key in the replicated storage.
	 * 
	 * @param key
	 *            The key for which to lookup in the replicated storage
	 * @return The value stored at {@code key} in the replicated storage, if
	 *         present, otherwise {@code null}
	 * @throws RemoteException
	 *             Remote method invocation did not work properly
	 */
	public Integer readValue(Integer key) throws RemoteException;

	/**
	 * Stores a new value at the given key in the replicated storage. If the key
	 * is already present in the replicated storage, its associated value will
	 * be overwritten.
	 * 
	 * @param key
	 *            The key for which to lookup in the replicated storage
	 * @param value
	 *            The value to store at {@code key} in the replicated storage
	 * @throws RemoteException
	 *             Remote method invocation did not work properly
	 */
	public void writeValue(Integer key, Integer value) throws RemoteException;
}
