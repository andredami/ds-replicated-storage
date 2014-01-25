/**
 * 
 */
package repst;

import java.util.HashMap;
import java.util.Map;

/**
 * This class encapsulates the single replica server storage
 * 
 * @author Andrea
 * 
 */
public class StorageMap {

	private Map<Integer, Integer> map = new HashMap<Integer, Integer>();

	/**
	 * Returns the value to which the specified key is mapped in the storage, or
	 * {@code null} if this map contains no mapping for the key.<br />
	 * More formally, if the storage contains a mapping from a key k to a value
	 * v such that {@code (key==null ? k==null :
	 * key.equals(k))}, then this method returns v; otherwise it returns null.
	 * (There can be at most one such mapping.)
	 * 
	 * @param key
	 *            the key whose associated value is to be returned
	 * @return the value to which the specified key is mapped, or null if this
	 *         map contains no mapping for the key
	 * @throws NullPointerException
	 *             if the specified key is null
	 */
	public synchronized Integer getValue(Integer key) {
		if (key == null) {
			throw new NullPointerException(this.getClass().getName()
					+ " do not allow null keys.");
		}
		return map.get(key);
	}

	/**
	 * Associates the specified value with the specified key in the storage. If
	 * the map previously contained a mapping for the key, the old value is
	 * replaced by the specified value. If the value is null, the present
	 * key-value association is deleted
	 * 
	 * @param key
	 *            key with which the specified value is to be associated
	 * @param value
	 *            value to be associated with the specified key
	 * @throws NullPointerException
	 *             if the specified key is null
	 */
	public synchronized void putValue(Integer key, Integer value) {
		if (key == null) {
			throw new NullPointerException(this.getClass().getName()
					+ " do not allow null keys.");
		}
		if (value == null) {
			map.remove(key);
		} else {
			map.put(key, value);
		}
	}
}
