/**
 * 
 */
package repst;

import java.io.Serializable;

/**
 * Represents an immutable Member Message sent Point-To-Point to the {@link Sequencer}
 * 
 * @author Andrea
 * 
 */
public class LamportMessage extends LMessage implements Serializable {

	public LamportMessage(Payload payload2, long processId, long assignedClock) {
		super(processId, assignedClock);
	}

	public Serializable payload;

	

	
}
