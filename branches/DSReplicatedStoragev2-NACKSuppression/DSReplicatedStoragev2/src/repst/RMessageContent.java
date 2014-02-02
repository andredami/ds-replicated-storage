package repst;

import java.io.Serializable;

public class RMessageContent extends RMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5368891778641907372L;
	
	VectorAck v;
	Serializable payload;

	public RMessageContent(Serializable m, int procid, long clock,
			VectorAck clone) {
		super(procid, clock);
		payload=m;
		v=clone;

	}

	public Serializable getPayLoad() {
		// TODO Auto-generated method stub
		return payload;
	}

	public VectorAck getPiggyBackAcks() {

		return v;
	}

	public void setPiggyBackAcks(VectorAck clone) {
		v=clone;
		
	}

}
