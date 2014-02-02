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

	
	@Override
	public String toString() {
		return "rMsg: "+clock+"."+procid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (clock ^ (clock >>> 32));
		result = prime * result + procid;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
//		if (getClass() != obj.getClass())
//			return false;
		RMessage other = (RMessage) obj;
		if (clock != other.clock)
			return false;
		if (procid != other.procid)
			return false;
		return true;
	}
	
	
}
