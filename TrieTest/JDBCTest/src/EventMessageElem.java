
import java.util.ArrayList;
import java.util.HashMap;

public class EventMessageElem {

	/**
	 * Simulates incoming messages
	 * It is a set of (attribute-value) objects
	 * For efficiency it should be implemented as a hashmap on attribute id
	 */
	
	public int _attributeId;
	public int _ival;
	public String _sval;
	public ValType _t;
	
	public EventMessageElem(int attributeId, int ival, String sval, ValType t) {
		_attributeId = attributeId;
		_ival = ival;
		_sval = sval;
		_t = t;
	}
	
	public boolean matches (SubAttribute sa) {
		if (sa._t == _t) {
			switch (_t) {
				case INTEGER_VAL: 
					if (_ival == sa._ival) return true;
					break;
				case STRING_VAL:
					if (_sval.equals(sa._sval)) return true;
					break;
			}
		}
		return false;
	}
	
	
	// 
	// converts the List to HashMap for efficiency. Will use it in v2 of the code
	//
	public HashMap<Integer, EventMessageElem> toHash(ArrayList<EventMessageElem> ml) {
		HashMap<Integer, EventMessageElem> hm = new HashMap<Integer, EventMessageElem>();
		
		for (EventMessageElem m : ml) {
			hm.put(new Integer(m._attributeId), m);
		}
		return hm;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
