package repst;

import java.io.Serializable;

public class VectorAck implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int[] ids;
	long[] clocks;
	
	public VectorAck(int numberOfMember,long yourID) {
		ids=new int[numberOfMember];
		for (int i =0,m=0;i<numberOfMember;i++,m++){	
				ids[i]=m;
		}
		clocks=new long[numberOfMember];
	}
	private VectorAck(){}
	
	public synchronized void update(int id,long clock){
		int index=getIndexOf(id);
		clocks[index]=clock;		
	}
	
	@Override
	public Object clone() {
		
		VectorAck cloned=new VectorAck();
		cloned.ids=ids.clone();
		cloned.clocks=clocks.clone();
		
		return cloned;
	}
	public synchronized boolean updateIfCorrect(int msgProcid, long msgclock) {
		int index = getIndexOf(msgProcid);
		if(clocks[index]+1==msgclock){
			clocks[index]=msgclock;
			return true;
		}
		return false;
	}
	
	private int getIndexOf(int msgProcid) {
		int index=0;
		while(index<ids.length ){
			if(msgProcid==ids[index]){
				break;
			}else{
				index++;
			}
		}
		return index;
	}
	
	public synchronized long getLastClockOf(int procid) {
		int index=getIndexOf(procid);
		
		return clocks[index];
	}
		
}
