/**
 * 
 */
package repst;

import java.rmi.Remote;

/**
 * 
 *
 */
public interface ServerRemoteInterface extends Remote {
	public Integer readValue(Integer key);
	public void writeValue(Integer key, Integer value);
}
