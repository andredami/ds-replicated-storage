package repst.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import repst.Message;

public class MessageTest {

	@Test
	public void testEqualsObject() {
		Message msg1a = new Message("testPayload",1,0);
		Message msg1b = new Message("testPayload",1,0);
		
		Message msg1c = new Message(msg1a);
		
		
		assertFalse(msg1a.equals(msg1b));
		assertTrue(msg1a.equals(msg1c));
	}

}
