package repst;

public class LamportAck extends LMessage{

	public LamportAck(long processId, long lamportClock) {
		super(processId, lamportClock);
	}

}
