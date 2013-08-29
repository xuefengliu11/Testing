import java.sql.*;

public class DbConnection {
	private Connection _con;
	
	public Connection getConnection() {
		return _con;
	}
	public void setConnection(Connection c) {
		_con = c;
	}
	
	public DbConnection() throws SQLException, ClassNotFoundException {
		
		Class.forName ("oracle.jdbc.driver.OracleDriver");

		   _con = DriverManager.getConnection
		     ("jdbc:oracle:thin:@173.167.217.171:5791:tm", "LEGATO", "Mulan123");
	
	}
	public ResultSet execQuery(String q) throws SQLException, ClassNotFoundException  {
		Statement s = _con.createStatement(); 
		if (s == null) return null;
		ResultSet rs = s.executeQuery(q);
		return rs;
	}
	
	
  public static void main (String[] args) throws Exception
  {
	  try {
	  DbConnection c = new DbConnection();
	  System.out.println("Connection opened successfully");
	  String trieQ = 
			  "select INSTANCEID, NODEID, PARENTNODEID, ATTRIBUTEID, DEPTH from BES_TRIE";
	  
	  ResultSet rs = c.execQuery(trieQ);
	  while (rs.next()) {
		  int iid = rs.getInt("INSTANCEID");
		  int nid = rs.getInt("NODEID");
		  int pid = rs.getInt("PARENTNODEID");
		  if (rs.wasNull()) pid = -1;
		  int aid = rs.getInt("ATTRIBUTEID");
		  if (rs.wasNull()) aid = -1;
		  int d = rs.getInt("DEPTH");
		  if (rs.wasNull())  d = -1;
		  System.out.println("TRIE ID: " + iid + " NODE ID:" + nid + " PARENTNODE ID:" + pid + " ATTRIBUTEID:" + aid);
	  }
	  }
	  catch (Exception e) {
		  System.out.println(e);
	  }
  }
}


