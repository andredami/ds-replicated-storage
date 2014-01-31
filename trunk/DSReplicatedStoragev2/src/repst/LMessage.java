package repst;

import java.io.Serializable;

public class LMessage implements Serializable{
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

}
