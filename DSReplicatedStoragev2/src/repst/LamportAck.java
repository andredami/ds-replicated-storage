package repst;

public class LamportAck extends LMessage{

	/**
	 * 
	 */
	private static final long serialVersionUID = 722359936312486841L;

	public LamportAck(long processId, long lamportClock) {
		super(processId, lamportClock);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Lack for: "+super.toString();
	}

}
