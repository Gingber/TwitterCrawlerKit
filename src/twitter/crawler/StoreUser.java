package twitter.crawler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import twitter.crawler.util.DBUtil;
import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class StoreUser {
	private int page=1;
	private int rp=5000;
	
	private Twitter twitter;
	
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getRp() {
		return rp;
	}
	public void setRp(int rp) {
		this.rp = rp;
	}
	public void storeByIDs() throws SQLException, TwitterException {
		OAuthTwitter oAuthTwitter=new OAuthTwitter();
		twitter=oAuthTwitter.oAuthLogin();
		Connection conn = DBUtil.getConn();
		Statement stmt = DBUtil.createStmt(conn);
		String sql="select followerId from kaifulee";
		ResultSet rs = DBUtil.query(sql, stmt);
		rs.last();
		long total = rs.getRow();
		
		do{	
			int usrCounter = 0;
			Statement stmtPage = DBUtil.createStmt(conn);
			sql = "select followerId from kaifulee limit "+(page-1)*rp+","+rp;
			//sql = "select followerId from kaifulee limit "+5630+","+rp;
			ResultSet rsPage = DBUtil.query(sql, stmtPage);
			while(rsPage.next()) {
				Statement stmtInsert = DBUtil.createStmt(conn);
				Statement stmtDelete = DBUtil.createStmt(conn);
				//System.out.println(rsPage.getInt("followerId"));
				User user = twitter.showUser(rsPage.getInt("followerId"));
				//User user = twitter.showUser(16177512);
				StringBuffer sb=new StringBuffer();
				sb.append("insert ignore into user(" +
						"user_id," +
						"user_name," +
						"user_screenName," +
						"user_description," +
						"user_url," +
						"user_lang," +
						"user_location," +
						"user_statusesCount," +
						"user_followersCount," +
						"user_friendsCount," +
						"user_favouritesCount," +
						"user_listedCount," +
						"user_createdAt," +
						"user_profileImageUrl," +
						"user_profileTextColor," +
						"user_profileLinkColor," +
						"user_profileBackgroundColor," +
						"user_profileBackgroundImageUrl," +
						"user_profileBackgroundTitled," +
						"user_profileSidebarFillColor," +
						"user_profileSidebarBorderColor," +
						"user_utcOffset," +
						"user_timeZone," +
						"user_isGeoEnabled," +
						"user_isProtected," +
						"user_isVerified," +
						"user_isFollowRequestSent," +
						"user_isContributorsEnabled) ");
				sb.append("values(");
				sb.append(user.getId() + ",");
				sb.append("'"+user.getName().replace("'", "\\'") + "',");
				sb.append("'"+user.getScreenName().replace("'", "\\'") + "',");
				sb.append("'"+user.getDescription().replace("'", "\\'") + "',");
				sb.append("'"+user.getURL() + "',");
				sb.append("'"+user.getLang().replace("'", "\\'") + "',");
				sb.append("'"+user.getLocation().replace("'", "\\'") + "',");
				sb.append(user.getStatusesCount()+",");
				sb.append(user.getFollowersCount()+",");
				sb.append(user.getFriendsCount()+",");
				sb.append(user.getFavouritesCount()+",");
				sb.append(user.getListedCount()+",");
				sb.append("'"+new SimpleDateFormat("yy-MM-dd HH:mm:ss").format(user.getCreatedAt())+"',");
				sb.append("'"+user.getProfileImageURL() + "',");
				sb.append("'"+user.getProfileTextColor() + "',");
				sb.append("'"+user.getProfileLinkColor() + "',");
				sb.append("'"+user.getProfileBackgroundColor()+"',");
				sb.append("'"+user.getProfileBackgroundImageURL() + "',");
				sb.append("'"+user.isProfileBackgroundTiled()+"',");
				sb.append("'"+user.getProfileSidebarFillColor()+"',");
				sb.append("'"+user.getProfileSidebarBorderColor()+"',");
				sb.append(user.getUtcOffset()+",");
				sb.append("'"+user.getTimeZone()+"',");
				sb.append("'"+user.isGeoEnabled()+"',");
				sb.append("'"+user.isProtected()+"',");
				sb.append("'"+user.isVerified()+"',");
				sb.append("'"+user.isFollowRequestSent()+"',");
				sb.append("'"+user.isContributorsEnabled()+"'");
				sb.append(")");
				System.out.println(sb.toString());
				//int row = DBUtil.update(sb.toString(), stmtInsert);
				if (1 == DBUtil.update(sb.toString(), stmtInsert)) { // 返回成功插入的行数
					System.out.println("update twitter database successs in user_id " + user.getId());
					DBUtil.update("delete from kaifulee where followerId = " + rsPage.getInt("followerId"), stmtDelete);
				}
				else {
					System.out.println("update twitter database failure in user_id " + user.getId());
					DBUtil.update("delete from kaifulee where followerId = " + rsPage.getInt("followerId"), stmtDelete);
				}
				
				usrCounter++;
				if (usrCounter %100 == 0) {
					try {
						Thread.sleep(600*1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				stmtInsert.close();
				stmtDelete.close();
				checkRateLimitStatus();
			}
			System.out.println("update twitter database successs in page "+ page);
			page++;
			
			stmtPage.close();
		}while ((page-1)*rp > total);
	
		stmt.close();
		conn.close();
	
	}
	
	private void checkRateLimitStatus()  {
		try {
		RateLimitStatus limit = twitter.getRateLimitStatus().get("/application/rate_limit_status");
		System.out.print("- limit: "+limit.getRemaining() +"\n");
		if (limit.getRemaining() <= 5) {
			int remainingTime = limit.getSecondsUntilReset() + 10;
			System.out.println("Twitter request rate limit reached. Waiting "+remainingTime/60+" minutes to request again.");
			
			try {
				Thread.sleep(remainingTime*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		} catch (TwitterException te) {
			System.err.println(te.getMessage());
			if (te.getStatusCode()==503) {
				try {
					Thread.sleep(120*1000);// wait 2 minutes
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
			}
		}
		catch(Exception e) {
			System.err.println(e.getMessage());
			
		}
	}
	
	public static void main(String[] args) throws SQLException, TwitterException {
		StoreUser storeUser=new StoreUser();
		storeUser.storeByIDs();
	}
}
