package handlers;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/*
* Create by: 	Connor Morley
* Date: 		July 2016
* Title: 		HealthChecker
* Version:		0.9
* Description:	Database interface handling for communication with an associated MySQL database that is used as a store and
* 				reference for monitored xpaths and the hits that are detected for them for future reporting/analysis. Controls
* 				include checks, additions, updates, removals and cleanups.
*/

public class DBHandler {

	 private static String address = "jdbc:mysql://192.168.10.128/healthtest?user=root&password=iPods123";
	  private static Connection conn = null;
	  private static Statement stmt = null;
	  private static ResultSet res = null;

	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////
	  
	  public static void SQLConnect(){
	    try {
	      // This will load the MySQL driver, each DB has its own driver
	      Class.forName("com.mysql.jdbc.Driver");
	      // Setup the connection with the DB
	      conn = DriverManager
	          .getConnection(address);
	    }
	    catch(Exception e)
	    {
	        System.out.println(e);
	        throw new RuntimeException();
	    }
	  }



public static void addEntry(String entry, String xpath) throws SQLException {	//CREATE STORED PROCEDURE
	SQLConnect();
	try{
	stmt = conn.createStatement();
	stmt.executeUpdate("insert into paths (gateway, xpath, runinstance) values ('" + MainAutoCheck.gateway + "', '" + entry + "', " + MainAutoCheck.runInstance + ");" );
} catch (SQLException e) {
	close();
	e.printStackTrace();
	throw new RuntimeException(e);
} 
	catch(Exception e)
	{
		e.printStackTrace();
	}
	close();
	return;
}

public static void updateEntry(String entry) throws SQLException {	//CREATE STORED PROCEDURE
	SQLConnect();
	try{
	stmt = conn.createStatement();
	stmt.executeUpdate("update paths set runinstance="+ MainAutoCheck.runInstance +" where xpath like '" + entry + "';");
} catch (SQLException e) {
	close();
	e.printStackTrace();
	throw new RuntimeException(e);
} 
	close();
	return;
}

public static int checkRemovals() throws SQLException {	//CREATE STORED PROCEDURE
	SQLConnect();
	int result = 0;
	try{
	stmt = conn.createStatement();
	res = stmt.executeQuery("select count(id) from paths where runinstance <> " + MainAutoCheck.runInstance + ";");
	while(res.next())
	{
		result = res.getInt(1);
	}
} catch (SQLException e) {
	close();
	e.printStackTrace();
	throw new RuntimeException(e);
} 
	close();
	return result;
}

public static void cleanUp() throws SQLException {	//CREATE STORED PROCEDURE
	SQLConnect();
	try{
	stmt = conn.createStatement();
	stmt.executeQuery("call sp_cleanup("+ MainAutoCheck.runInstance + ", '"+ MainAutoCheck.gateway + "');");
} catch (SQLException e) {
	close();
	e.printStackTrace();
	throw new RuntimeException(e);
} 
	close();
	return;
}


public static int checkEntry(String entry) {
	SQLConnect();
	int result;
	try {
		stmt = conn.createStatement();
		res = stmt
				.executeQuery("select id from paths where xpath like '" + entry + "';");
		if (!res.isBeforeFirst()) 
		{
			result = 2;
		} 
		else 
		{
			result = 1;
		}
	} catch (SQLException e) {
		close();
		throw new RuntimeException(e);
	} 
		close();
		return result;
	

}

public static ArrayList<String> getCurrentXpaths(){
	SQLConnect();
	ArrayList<String> result = new ArrayList<String>();
	try{
	stmt = conn.createStatement();
	res = stmt.executeQuery("select xpath from paths where gateway like '" + MainAutoCheck.gateway + "';");
	while(res.next())
	{
		result.add(res.getString(1));
	}
} catch (SQLException e) {
	close();
	e.printStackTrace();
	throw new RuntimeException(e);
} 
	close();
	return result;
}

public static void updateHits(Map<String, Integer> updates){
	SQLConnect();
	try{
	stmt = conn.createStatement();
	for(Map.Entry<String,Integer> entry : updates.entrySet())
	{
		stmt.addBatch("call sp_updatehits('" + entry.getKey() + "' , " + entry.getValue() + ");");
	}
	stmt.executeBatch();
} catch (SQLException e) {
	close();
	e.printStackTrace();
	throw new RuntimeException(e);
} 
	close();
	return;
}


public static void close() {
    try {
      if (res != null) {
        res.close();
      }

      if (stmt != null) {
        stmt.close();
      }

      if (conn != null) {
        conn.close();
      }
    } catch (Exception e) {
    	e.printStackTrace();

    }
  }


}
