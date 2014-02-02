package repst;

public class RNack extends RMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2341941951035024229L;

	int frompid;
	
	/**
	 * procid and clock are used to refer the message the nack is asking for
	 * 
	 * @param frompid
	 * @param procid
	 * @param clock
	 */
	public RNack(int frompid,int procid, long clock) {
		super(procid, clock);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Rnack for:"+super.toString();
	}
	
	/*
	 * see equals in RMessage
	 */

}
