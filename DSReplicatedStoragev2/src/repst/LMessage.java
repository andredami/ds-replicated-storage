package repst;

import java.io.Serializable;

public class LMessage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6551761422395005658L;

	public LMessage(long processId, long lamportclock) {
		super();
		this.processId = processId;
		this.lamportclock = lamportclock;
	}

	long lamportclock;
	long processId;

	public long getProcessId() {
		return processId;
	}

	public void setProcessId(long processId) {
		this.processId = processId;
	}

	public long getLamportClock() {
		return lamportclock;
	}

	public void setLamportClock(long lamportclock) {
		this.lamportclock = lamportclock;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "lMsg:" + lamportclock + "." + processId;
	}

}
