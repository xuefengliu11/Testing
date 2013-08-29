import java.sql.*;
import java.util.ArrayList;

public class SubElement {

	//
	// subscription element -- subscription relationship
	// loads the  data from database and caches it in allSubElements collection
	// Cache linear search (performs in O(n)) to be replaced by hash or binary search
	//
	public int _subElemId;
	public int _subId;
	
	public static ArrayList<SubElement> allSubElements = new ArrayList();
	
	public SubElement(int subElemid, int subid ) {
		_subElemId = subElemid;
		_subId = subid;
	}
	
	public void  addSubElement() {
		allSubElements.add(this);
	}
	
	public static void loadSubElements(DbConnection c) throws SQLException, ClassNotFoundException {
		String q = 
				"Select SUBELEMID, SUBSCRIPTIONID from BES_SUBELEM";
		ResultSet r = c.execQuery(q);
		while (r.next()) {
			int subelem = r.getInt("SUBELEMID");
			int s = r.getInt("SUBSCRIPTIONID");
			
			SubElement se = new SubElement(subelem, s);
			se.addSubElement();
			
		}
	}
	
	public void printSubElement() {
		
		System.out.println(
				"SUBELEMID:" + this._subElemId + " SUBID:" + this._subId);
	}
		
	public static void printSubElementList() {
		for (SubElement se : allSubElements) {
			se.printSubElement();
		}
	}
		
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
		DbConnection c = new DbConnection();
		loadSubElements(c);
		printSubElementList();
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}

}
