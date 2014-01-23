package repst.tests;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import repst.HistoryBuffer;
import repst.Message;

public class HistoryBufferTest {

	HistoryBuffer buffer;
	
	@Before
	public void setup(){
		buffer = new HistoryBuffer(4);
	}
	
	@Test
	public void testRecordAndRetrieve1() {
		Message[] msg = {new Message("testPayload",1,0), new Message("testPayload",1,0), new Message("testPayload",1,0)};
		
		buffer.record(1l, msg[0]);
		buffer.record(2l, msg[1]);
		buffer.record(3l, msg[2]);
		
		Message[] storedMessages = new Message[3];
		buffer.getAllLostMsgAfter(0l).toArray(storedMessages);
		
		for(int i=0; i<3; i++){
			assertTrue(equalsForTest(storedMessages[i], msg[i]));
		}
	}
	
	@Test
	public void testRecordAndRetrieve2() {
		Message[] msg = {new Message("testPayload",1,0), new Message("testPayload",2,0), new Message("testPayload",3,0)};
		
		buffer.record(1l, msg[0]);
		buffer.record(2l, msg[1]);

		Message[] storedMessages1 = new Message[2];
		buffer.getAllLostMsgAfter(0l).toArray(storedMessages1);
		
		buffer.record(3l, msg[2]);
		
		Message[] storedMessages2 = new Message[3];
		buffer.getAllLostMsgAfter(0l).toArray(storedMessages2);

		
		for(int i=0; i<2; i++){
			assertTrue(equalsForTest(storedMessages1[i], storedMessages2[i]));
		}
		
		assertTrue(equalsForTest(storedMessages2[2], msg[2]));
	}
	
	@Test
	public void testRecordAndRetrieve3() {
		Message[] msg = {new Message("testPayload",1,0), new Message("testPayload",2,1), new Message("testPayload",1,0)};
		
		buffer.record(1l, msg[0]);
		buffer.record(2l, msg[1]);

		Message[] storedMessages1 = new Message[2];
		buffer.getAllLostMsgAfter(1l).toArray(storedMessages1);
		
		buffer.record(3l, msg[2]);
		
		Message[] storedMessages2 = new Message[3];
		buffer.getAllLostMsgAfter(0l).toArray(storedMessages2);

		
		assertNull(storedMessages1[1]);
		assertTrue(equalsForTest(storedMessages1[0], storedMessages2[1]));
	}
	
	@Test
	public void testRecordAndRetrieve4() {
		Message[] msg = {new Message("testPayload",1,0), new Message("testPayload",2,1), new Message("testPayload",1,0)};
		
		buffer.record(1l, msg[0]);
		buffer.record(1l, msg[1]);

		Message[] storedMessages1 = new Message[2];
		buffer.getAllLostMsgAfter(1l).toArray(storedMessages1);
		
		assertNull(storedMessages1[1]);
	}

	private boolean equalsForTest(Message arg0, Message arg1){
		return (arg0.payload.equals(arg1.payload) &&
		arg0.processId == arg1.processId &&
		arg0.messageId == arg1.messageId &&
		arg0.lastSequence == arg1.lastSequence);
	}
}
