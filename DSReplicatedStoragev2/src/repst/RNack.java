package repst;

public class RNack extends RMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2341941951035024229L;

	int frompid;
	public RNack(int frompid,int procid, long clock) {
		super(procid, clock);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Rnack for:"+super.toString();
	}

}
