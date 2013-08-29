import java.sql.*;

import java.util.ArrayList;

public class SubAttribute {

	//
	// Stores the membership of attributes to Subscription Elements
	// along with the attribute value. For now atomic values only, to be enhanced
	// with sets of values and regular expressions
	//Attribute nodes are cached in allSubAttributes list.
	// Search is currently linear, to be replaced by by binary search or hash-based cache access.
	//
	public int _attributeId;
	public int _subElemId;
	public int _seqNo;
	public int _ival;
	public String _sval;
	public ValType _t;
	
	static ArrayList<SubAttribute> allSubAttributes = new ArrayList();
	
	public SubAttribute(int id, int subEl, int seq, int ival, String sval, ValType t ) {
		_attributeId = id;
		_subElemId = subEl;
		_seqNo = seq;
		_ival = ival;
		_sval = sval;
		_t = t;
	}
	
	public void  addSubAttribute() {
		allSubAttributes.add(this);
	}
	
	public static void loadSubAttributes(DbConnection c) throws SQLException, ClassNotFoundException {
		String q = 
				"Select SUBELEMID, ATTRIBUTEID, SEQNO, EXACTTXT, EXACTINT from BES_SUBATTRIBUTE";
		ResultSet r = c.execQuery(q);
		while (r.next()) {
			int subelem = r.getInt("SUBELEMID");
			int attid = r.getInt("ATTRIBUTEID");
			int seq = r.getInt("SEQNO");
			String sval = r.getString("EXACTTXT");
			ValType t;
			if (r.wasNull()) t = ValType.INTEGER_VAL;
			else t = ValType.STRING_VAL;
			int ival = r.getInt("EXACTINT");
			//if (r.wasNull()) ival = -1;
			SubAttribute sa = new SubAttribute(attid, subelem, seq, ival, sval, t);
			sa.addSubAttribute();
			
		}
	}
	
	public void printSubAttribute() {
		
		System.out.println(
				"ATTR ID:" + this._attributeId + " SUB ELEM:" + this._subElemId + " TYPE:"+ _t + " VAL:" + ((this._sval == null)? _ival:_sval));
	}
		
	public static void printSubAttributeList() {
		for (SubAttribute a : allSubAttributes) {
			a.printSubAttribute();
		}
	}
		
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
		DbConnection c = new DbConnection();
		loadSubAttributes(c);
		printSubAttributeList();
		}
		catch (Exception e) {
			System.out.println(e);
		}
		
	}
	
}
