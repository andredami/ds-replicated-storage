package repst;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface SequencerRemoteInterface extends Remote {
	
	public Long getNewProcessId() throws RemoteException;
	
	public void forwardMessage(Message msg) throws RemoteException;
	
	public void recordHeartbeat(Message msg) throws RemoteException;
	
	public List<OrderedMessage> getLostMessages(Long afterSequence) throws RemoteException;

}
