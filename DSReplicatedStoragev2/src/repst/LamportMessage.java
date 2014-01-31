package repst;

import java.io.Serializable;

public class LamportMessage extends LMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4595885519563413434L;
	
	public LamportMessage(Payload payload, int processId, long assignedClock) {
		super(processId, assignedClock);
		this.payload=payload;
	}
	Serializable payload;
	
}
