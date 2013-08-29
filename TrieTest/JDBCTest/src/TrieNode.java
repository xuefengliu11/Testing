import java.sql.*;
import java.util.ArrayList;

public class TrieNode {

	//
	// models the relationship between Trie, TrieNode and related subscription elements
	// All nodes are stored in allTrieNodes list. Search is linear.
	// Search to be replaced by binary or hash
	//
	
	public int _trieNodeId;
	public int _trieId; // the trie where the node instance belongs
	public int _subElemId;
	public int _terminates;
	
	public static ArrayList<TrieNode> allTrieNodes = new ArrayList();
	
	public TrieNode (int trieNodeId, int trieId, int subElemId, int terminates) {
		_trieNodeId = trieNodeId;
		_trieId = trieId;
		_subElemId = subElemId;
		_terminates = terminates;
		
	}
	public void  addSubElement() {
		allTrieNodes.add(this);
	}
	
	public static void loadTrieNodes(DbConnection c) throws SQLException, ClassNotFoundException {
		String q = 
				"Select NODEID, INSTANCEID, SUBELEMID, TERMINATES from BES_TRIENODE order by INSTANCEID, NODEID";
		ResultSet r = c.execQuery(q);
		while (r.next()) {
			int nodeid = r.getInt("NODEID");
			int instanceid = r.getInt("INSTANCEID");
			int subelemid = r.getInt("SUBELEMID");
			int term = r.getInt("TERMINATES");
			
			TrieNode tn = new TrieNode(nodeid, instanceid, subelemid, term);
			tn.addSubElement();
			
		}
	}
	
	public void printTrieNode() {
		
		System.out.println(
				"NODEID:" + this._trieNodeId + " TRIEINSTANCEID:" + this._trieId + " SUBELEMID:" + this._subElemId + " TERMINATES:" + this._terminates);
	}
		
	public static void printTrieNodeList() {
		for (TrieNode tn : allTrieNodes) {
			tn.printTrieNode();
		}
	}
		
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
		DbConnection c = new DbConnection();
		loadTrieNodes(c);
		printTrieNodeList();
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}

}
