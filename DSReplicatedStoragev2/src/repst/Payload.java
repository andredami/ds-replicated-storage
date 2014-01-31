package repst;

import java.io.Serializable;

public class Payload implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Integer key;
	public Integer value;

	public Payload(Integer key, Integer value) {
		this.key = key;
		this.value = value;
	}
}
