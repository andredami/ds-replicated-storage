/**
 * 
 */
package repst;

import java.io.Serializable;

/**
 * @author Andrea
 *
 */
public class Message {
	
	private static Long nextId = (long) 0;
	
	private Serializable object;
	/**
	 * Acquired via config phase.
	 */
	private Long processId;
	/**
	 * Auto-incremented from nextId
	 */
	private Long messageId;
	
	/**
	 * Ack on Piggyback 
	 */
	private Long lastSequence;
}
