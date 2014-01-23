package repst;

import java.util.Comparator;

/**
 * Utility Class: A comparator to compare {@link OrderedMessage}s with respect
 * to sequence number
 * 
 * @author Andrea
 * 
 */
public class OrderedMessageComparator implements Comparator<OrderedMessage> {

	@Override
	public int compare(OrderedMessage arg0, OrderedMessage arg1) {
		return (int) (arg0.sequenceNumber - arg1.sequenceNumber);
	}

}
