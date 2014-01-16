/**
 * 
 */
package repst;

import java.util.List;
import java.util.Map;

/**
 * @author Andrea
 *
 */
public class HistoryBuffer {
	private Map<Long, Message> buffer;
	/**
	 * Map<processId, lastSequence>
	 */
	private Map<Long, Long> lastAckVect;
	
	public void record(Long sequence, Message msg){
		// TODO Record Message and lastSequence
	}
	
	private void trim(){
		
	}
	
	public List<OrderedMessage> getAllLostMsgAfter(Long lastSequence){
		return null;
		
	}
	
	public void record(Long process, Long lastSequence){
		// TODO updates only lastAckVect
	}
}
