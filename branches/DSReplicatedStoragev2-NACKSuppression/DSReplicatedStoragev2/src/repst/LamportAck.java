package repst;

public class LamportAck extends LMessage{

	/**
	 * 
	 */
	private static final long serialVersionUID = 722359936312486841L;
	long forprocid;
	long forlamportclock;
	
	public LamportAck(long processId, long lamportClock,long forprocid,long forlamportclock) {
		super(processId, lamportClock);
		this.forprocid=forprocid;
		this.forlamportclock=forlamportclock;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Lack for: "+forlamportclock+"."+forprocid;
	}

	public boolean isFor(LamportMessage topMsg) {
		return (forlamportclock==topMsg.lamportclock&&forprocid==topMsg.processId);
	}

}
