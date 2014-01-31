package repst;

import java.io.Serializable;
import java.util.ArrayList;

public class VectorAck implements Serializable{
	
	int[] ids;
	long[] clocks;
	
	public VectorAck(int numberOfMember,long yourID) {
		ids=new int[numberOfMember-1];
		for (int i =0,m=0;i<numberOfMember-1;i++,m++){
			if(i!=yourID){
				ids[i]=m;
			}else{
				m++;
				ids[i]=m;		
			}
		}
		clocks=new long[numberOfMember-1];
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
		int index;
		for( index = 0;index<ids.length;index++ ){
			if(msgProcid==ids[index]){
				break;
			}
		}
		return index;
	}
	
	public synchronized long getLastClockOf(int procid) {
		int index=getIndexOf(procid);
		
		return clocks[index];
	}
		
}
