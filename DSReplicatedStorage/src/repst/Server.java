/**
 * 
 */
package repst;

import java.util.Set;

/**
 * @author Jacopo
 *
 */
public class Server implements ServerRemoteInterface {

	private StorageMap storage;

	// TODO main - createChannel calls initialize
	
	/* (non-Javadoc)
	 * @see repst.ServerRemoteInterface#readValue(java.lang.Integer)
	 */
	@Override
	public Integer readValue(Integer key) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see repst.ServerRemoteInterface#writeValue(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public void writeValue(Integer key, Integer value) {
		// TODO Calls forward

	}
	
	public void fetchFormChannel(){
		// TODO pops from Channel and puts into storage.
	}

}
