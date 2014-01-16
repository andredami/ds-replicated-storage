/**
 * 
 */
package repst;

import java.io.Serializable;

/**
 * @author Jacopo
 *
 */
public class Channel {
	
	private Long lastSequence;
	private LinkedList<Message> queue;
	private Long processId;
	
	public void initialize(){
		// TODO remote call to sequencer getNewProcessId() and stores it in processId
	}
	
	public void forward(Serializable obj){
		//TODO creates Message and sends it to Sequencer
	}
	
	private void sendNack(){
		// TODO calls remote getLost... on Sequencer
	}
	
	public void onMulticastReceived(OrderedMessage msg){
		if(msg.sequence == lastSequence+1){
			// TODO push in queue if not duplicated
		} else {
			// TODO discard, pause socket and sendNack()
		}
	}
	
	public Serializable pop(){
		// TODO return the obj in the head message to the server and deletes it.
	}
	
}
