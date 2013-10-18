package twitter.crawler.util;
import java.sql.*;

public class DBUtil {

	private static String ip="127.0.0.1";
	
	private static String driver="com.mysql.jdbc.Driver";
	
	private static String user="root";
	
	private static String password="hadoop";
	
	private static String databaseName="twitter";
	
	private static String encode="utf-8";
	
	
	public static Connection getConn() {
		Connection conn = null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection("jdbc:mysql://"+ip+":3306/"+databaseName+"?useUnicode=true&characterEncoding="+encode, user , password);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}
	
	public static Statement createStmt(Connection conn) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return stmt;
	}
	
	public static ResultSet query(String sql,Statement stmt) {
		ResultSet rs = null;
		try {
			rs = stmt.executeQuery(sql);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	public static int update(String sql,Statement stmt) {
		int ret = 0;
		try {
			ret = stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return ret;
	}
	
	public static void close(Connection conn,Statement stmt,ResultSet rs)
	{
		close(rs);
		close(stmt);
		close(conn);
	}
	private static void close(Connection conn) {
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			conn = null;
		}
	}
	
	private static void close(Statement stmt) {
		if(stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			stmt = null;
		}
	}
	
	private static void close(ResultSet rs) {
		if(rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			rs = null;
		}
	}
	
	public static void main(String [] args)
	{
		
		Connection conn=DBUtil.getConn();
		Statement stmt=DBUtil.createStmt(conn);
		String sql="select count(*) from user";
		ResultSet rs=DBUtil.query(sql, stmt);
		try {
			while(rs.next())
			{
				System.out.println(rs.getInt(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DBUtil.close(conn, stmt, rs);
	
		String sql1="insert into equipcategory(eqca_name) values('电脑配件')";
//		DBUtil.update(sql1);
	}
}
