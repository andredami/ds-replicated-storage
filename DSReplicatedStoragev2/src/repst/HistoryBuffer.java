/**
 * 
 */
package repst;

/**
 * Keeps the messages sent from this host. for each of the messages it keeps if it is acked by all. nack are not sequenced
 **/
public class HistoryBuffer {

	public HistoryBuffer(int processId, int numberOfmember) {
		// TODO Auto-generated constructor stub
	}

	public void add(RMessageContent rMsg) {
		// TODO Auto-generated method stub
		
	}
	
	public RMessageContent get(long clock){
		return null;
		//TODO
	}

	public void trimIfYouCan(int pid, long lastSeen) {
		// TODO Auto-generated method stub
		
	}
	
}
