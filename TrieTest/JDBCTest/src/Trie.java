import java.sql.*;
import java.util.ArrayList;

public class Trie {

	//
	// a Trie has a root.
	// all Trie Nodes have a parent, a list of children, an attribute that applies to that node, and
	// a list of subscriptions that are applicable to the node, as they include the node attribute.
	// All tries are stored in the allTries list
	//
	private int _trieId;
	private int _trieNodeId;
	private boolean _isRoot;
	private ArrayList<SubAttribute> _subAttributes;
	private ArrayList<Integer> _subElements;
	private Trie _parent;
	private ArrayList<Trie> _children;
	private int _attributeId;
	private int _depth;
	
	static public ArrayList<Trie> allTries = new ArrayList();
	
	public Trie(int trieId, int trieNodeId, boolean isRoot, int parentNodeId, int attributeId, int depth ) {
		_trieId = trieId;
		_trieNodeId = trieNodeId;
		_isRoot = isRoot;
		//
		// populate list of applicable subscription elements
		//
		_subElements = null;
		for (TrieNode tn: TrieNode.allTrieNodes) {
			if (tn._trieId == trieId && tn._trieNodeId == trieNodeId) {
				int subElemId = tn._subElemId;
				if (_subElements == null) _subElements = new ArrayList<Integer>();
				_subElements.add(new Integer(subElemId));
			}
		}
		//
		// populate list of applicable SubAttributes
		//
		_subAttributes = null;
		for (SubAttribute sa: SubAttribute.allSubAttributes) {
			if (attributeId == sa._attributeId) {
				Integer sid = new Integer(sa._subElemId);
				if (_subElements != null && _subElements.contains(sid)) {
					if (_subAttributes == null) _subAttributes = new ArrayList<SubAttribute>();
					_subAttributes.add(sa);
				}
			}
		}
		//
		// identify parent node
		//
		if (parentNodeId != -1) {
			for (Trie t: allTries) {
				if (t._trieNodeId == parentNodeId && t._trieId == trieId)  {
					_parent = t;
					if (t._children == null) t._children = new ArrayList<Trie>();
					t._children.add(this);
				}
			}
		}
		else {
			_parent = null;
		}
		_attributeId = attributeId;
		_depth = depth;
		_children = null;
	}
	
	public void  addTrie() {
		allTries.add(this);
	}
	
	public static void loadTries(DbConnection c) throws SQLException, ClassNotFoundException {
		String q = 
				"Select INSTANCEID, NODEID,PARENTNODEID, ATTRIBUTEID, DEPTH from BES_TRIE order by INSTANCEID, NODEID";
		ResultSet r = c.execQuery(q);
		int i = 0;
		int j = 0;
		while (r.next()) {
			int instanceId = r.getInt("INSTANCEID");
			int nodeId = r.getInt("NODEID");
			int parentId = r.getInt("PARENTNODEID");
			boolean isRoot = false;
			if (r.wasNull()) {
				isRoot = true;
				parentId = -1;
			}
			int attributeId = r.getInt("ATTRIBUTEID");
			if (r.wasNull()) attributeId = -1;
			int depth = r.getInt("DEPTH");			
			//System.out.println(i++);
			Trie t = new Trie(instanceId, nodeId, isRoot, parentId, attributeId, depth);
			t.addTrie();
			//System.out.println(j++);
			
		}
	}
	
	public void printTrie() {
		
		System.out.println(
				"TRIEID:" + this._trieId + " NODEID:" + this._trieNodeId + " PARENT:" + ((this._parent == null)? "NULL": this._parent._trieNodeId) + " ATTRIBUTE: " + this._attributeId + " DEPTH:" + this._depth);
		if (_subElements != null) {
			for (Integer se: this._subElements) System.out.println("SUBELEMENT: " + se);
		}
		if (_subAttributes != null) {
			for (SubAttribute sa: _subAttributes) {
				sa.printSubAttribute();
			}
		}
	}
		
	public static void printTries() {
		int i = 0;
		
		for (Trie t : allTries) {
			t.printTrie();
		}
	}
		
	public static void traverseTree(Trie root) {
		if (root == null) return;
		root.printTrie();
		if (root._children == null)return;
		
		for (Trie t: root._children) {
			//t.printTrie();
			traverseTree(t);
		}
	}
	
	//
	// Input: a message as a list of (attribute, value) pairs
	// Function: Matches a message against the trie.
	// Will traverse all paths in the trie and match against incoming message.
	// All Subscription Elements that match will be accumulated. 
	// Output: a list of matching subscriptions
	// 
	public static ArrayList<Integer> matchMessage(ArrayList<EventMessageElem> message) {
		ArrayList<Integer> matchedSubElem = null;
		
		for (Trie t: allTries) {
			if (t._isRoot) {
				t.printTrie();
				for (Trie tchild: t._children) {
					ArrayList<Integer> childSubElem = tchild.pathMatch(message, 
							t._subElements);
					matchedSubElem = mergeLists(matchedSubElem, childSubElem);
				}
			}
		}
		if (matchedSubElem != null) for (Integer se: matchedSubElem) System.out.println("***** " + se.intValue());
		ArrayList<Integer> subList = mapToSub(matchedSubElem);
		return subList;
	}
	
	private static ArrayList<Integer> mapToSub(ArrayList<Integer> subElemList) {
		ArrayList<Integer> result = null;
		
		if (subElemList == null) return null;
		for (Integer se: subElemList) {
			for (SubElement s: SubElement.allSubElements) {
				if (s._subElemId == se.intValue()) {
					if (result == null) result = new ArrayList<Integer>();
					Integer sub = new Integer(s._subId);
					if (!result.contains(sub)) result.add(sub);
				}
			}
		}
		return result;
	}
	
	//
	// matches a message against all paths and calculates the list of matching subscription elements
	// It starts from the current node and goes down the tree on all paths.
	// It gets a subscription element context as input, which is the set of subscription elements 
	// that are a match for the upstream path (parent, grandparent, etc.). All other subscriptions 
	// have failed and are not considered in the matching process.
	//

	private ArrayList<Integer> pathMatch(ArrayList<EventMessageElem> message, ArrayList<Integer> subElContext) {
		ArrayList<Integer> result = null;
		
		System.out.println("Node: " + this._trieId + " " + this._trieNodeId);
		if (subElContext == null) return null;
		if (message == null) return null;
		
		for (EventMessageElem el: message) {
			if (el._attributeId == _attributeId) { 
				ArrayList<Integer> newSubElContext = null;
				// 
				// match input context against _subAttributes
				// and restrict context
				//
				if (_subAttributes == null) return null;
				for (SubAttribute sa: _subAttributes) {
					if (el.matches(sa)) {
						if (subElContext.contains(new Integer(sa._subElemId))) {
							if (newSubElContext == null) newSubElContext = new ArrayList<Integer> ();
							newSubElContext.add(new Integer(sa._subElemId));
						}
					
					}
				}
				
				if (newSubElContext == null) return null; // found no match
				if (_children == null) return newSubElContext; // no children to continue down the tree
				
				// continue matching process with children
				for (Trie tchild: this._children) {
					ArrayList<Integer> childResult = tchild.pathMatch(message, newSubElContext);
					result = mergeLists(result, childResult);
				}
			}
		}
		return result;
	}
	
	public static ArrayList<Integer> mergeLists(ArrayList<Integer> list1, ArrayList<Integer> list2) {
	
		if (list1 == null && list2 == null) return null;
		
		ArrayList<Integer> result = new ArrayList<Integer>();

		if (list1 == null) { // clone list2
			for (Integer se2: list2) {
				result.add(se2);
			}
			return result;
		}
		if (list2 == null) { // clone list1
			for (Integer se1: list1) {
				result.add(se1);
			}
			return result;
		}
		
		// else merge lists
		for (Integer se1:list1) result.add(se1);
		for (Integer se2: list2) {
			if (!result.contains(se2)) result.add(se2);
		}
		return result;
	}
		
	public static void main(String[] args) {
		
		try {
			DbConnection c = new DbConnection();
			SubAttribute.loadSubAttributes(c);
			System.out.println("Loaded SubAttributes");
			SubElement.loadSubElements(c);
			System.out.println("Loaded SubElements");
			TrieNode.loadTrieNodes(c);
			System.out.println("Loaded TreeNodes");
			Trie.loadTries(c);
			System.out.println("Loaded Tries");
			long start = System.currentTimeMillis();
			//printTries();
			//for (int i = 0; i < 5000; i++)
			
			/*
			for (Trie t: Trie.allTries) {
				if (t._isRoot) traverseTree(t);
			}
			*/
			
			//
			// create message
			//
			EventMessageElem e1 = new EventMessageElem(1032, 15, null, ValType.INTEGER_VAL);
			EventMessageElem e2 = new EventMessageElem(1028, 0, "US LDO", ValType.STRING_VAL);
			//EventMessageElem e3 = new EventMessageElem(103, 12, null, ValType.INTEGER_VAL);
			for (int j = 0; j < 10000; j++){
				ArrayList<EventMessageElem> message = new ArrayList<EventMessageElem>();
				message.add(e1);
				message.add(e2);
			
				ArrayList<Integer> subs = matchMessage(message);
				if (subs!= null) for (Integer s:subs) System.out.println("SUBS +++ " + s.intValue());
				for (Integer s: subs) System.out.println(s.intValue());
			}
			
			long end = System.currentTimeMillis();
			
			System.out.println("==> End and Start Times: " + end + " : " + start);
					
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}

}
