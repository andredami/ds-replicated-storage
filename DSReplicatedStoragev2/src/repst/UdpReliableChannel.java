package repst;

import java.io.Serializable;
import java.util.ArrayList;

public class UdpReliableChannel {
	
	long procid;
	int numOfMember;
	HistoryBuffer history;
	ArrayList<Serializable> delivery=new ArrayList<Serializable>();
	
	public void initialize(int processId, int numberOfmember) {
		procid=processId;
		numOfMember=numberOfmember;	
	}

	public LamportMessage read() {
		// TODO Auto-generated method stub
		return null;
	}

	public void write(LMessage m) {
		// TODO Auto-generated method stub
		
	}

}
