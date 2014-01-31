package repst;

import java.io.Serializable;

public class LamportMessage extends LMessage {

	public LamportMessage(Payload payload, int processId, long assignedClock) {
		super(processId, assignedClock);
		this.payload=payload;
	}
	Serializable payload;
	
}
