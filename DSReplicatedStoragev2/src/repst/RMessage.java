package repst;

import java.io.Serializable;

public class RMessage implements Serializable {
	int procid;
	long clock;
	/**
	 * 
	 */
	private static final long serialVersionUID = -867826020191611865L;

	public RMessage(int procid, long clock) {
		super();
		this.procid = procid;
		this.clock = clock;
	}

	public int getProcessId() {
		return procid;
	}

	public long getClock() {
		return clock;
	}

	
	

}
