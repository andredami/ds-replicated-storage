package repst.tests;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import repst.Message;
import repst.OrderedMessage;

public class OrderedMessageTest {

	private static OrderedMessage omsg1a;
	private static OrderedMessage omsg1b;
	
	private static OrderedMessage omsg2a;
	private static OrderedMessage omsg2b;
	
	private static OrderedMessage omsg3a;
	private static OrderedMessage omsg3b;
	
	private static OrderedMessage omsg4a;
	private static OrderedMessage omsg4b;
	
	private static OrderedMessage omsg5a;
	private static OrderedMessage omsg5b;
	
	@BeforeClass
	public static void setup(){
		Message msg1a = new Message("testPayload",1,0);
		Message msg1b = new Message(msg1a);
		Message msg2 = new Message("testPayload",2,0);
		
		omsg1a = new OrderedMessage(0l, msg1a);
		omsg1b = new OrderedMessage(0l, msg1a);
		
		omsg2a = new OrderedMessage(1l, msg1a);
		omsg2b = new OrderedMessage(1l, msg2);
		
		omsg3a = new OrderedMessage(1l, msg1a);
		omsg3b = new OrderedMessage(2l, msg1a);
		
		omsg4a = new OrderedMessage(1l, msg1a);
		omsg4b = new OrderedMessage(2l, msg1b);
		
		omsg5a = new OrderedMessage(0l, msg1a);
		omsg5b = new OrderedMessage(0l, msg1b);
	}
	
	@Test
	public void testEqualsObject() {
		assertTrue(omsg1a.equals(omsg1b));
		assertFalse(omsg2a.equals(omsg2b));
		assertFalse(omsg3a.equals(omsg3b));
		assertFalse(omsg4a.equals(omsg4b));
		assertTrue(omsg5a.equals(omsg5b));
	}

	@Test
	public void testEqualsSN() {
		assertTrue(omsg1a.equalsSN(omsg1b));
		assertTrue(omsg2a.equalsSN(omsg2b));
		assertFalse(omsg3a.equalsSN(omsg3b));
		assertFalse(omsg4a.equalsSN(omsg4b));
		assertTrue(omsg5a.equalsSN(omsg5b));
	}

}
