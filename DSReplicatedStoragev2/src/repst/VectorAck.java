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
	
	public void update(int id,long clock){
		int index=-1;
		for (int i=0;i<ids.length;i++){
			if(ids[i]==id){
				index=i;
				break;
			}
		}
		clocks[index]=clock;		
	}
	
	@Override
	public Object clone() {
		
		VectorAck cloned=new VectorAck();
		cloned.ids=ids.clone();
		cloned.clocks=clocks.clone();
		
		return cloned;
	}
	public boolean updateIfCorrect(int msgProcid, long msgclock) {
		int index;
		for( index = 0;index<ids.length;index++ ){
			if(msgProcid==ids[index]){
				break;
			}
		}
		if(clocks[index]+1==msgclock){
			clocks[index]=msgclock;
			return true;
		}
		return false;
	}
	public long getLastClockOf(long procid) {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getProcessId() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
