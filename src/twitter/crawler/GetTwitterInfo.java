package twitter.crawler;

import java.sql.Connection;
import java.sql.Statement;
import twitter.crawler.util.DBUtil;
import twitter4j.IDs;
import twitter4j.PagableResponseList;
import twitter4j.RateLimitStatus;
import twitter4j.RateLimitStatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class GetTwitterInfo {
	private static Twitter twitter;
	
	public void getSpecifiedUser(String screenName){
		OAuthTwitter oAuthTwitter=new OAuthTwitter();
		twitter=oAuthTwitter.oAuthLogin();
		RateLimitStatusListener listener=new RateLimitStatusListenerImpl();
		twitter.addRateLimitStatusListener(listener);
		try {
			User user=twitter.showUser(screenName);
			System.out.println(screenName+"'s URL:"+user.getURL());
			System.out.println(screenName+"'s id:"+user.getId());
			System.out.println(screenName+"'s Name:"+user.getName());
			System.out.println(screenName+"'s CreateAt:"+user.getCreatedAt());
			System.out.println(screenName+"'s Location:"+user.getLocation());
			System.out.println(screenName+"'s Lang:"+user.getLang());
			System.out.println(screenName+"'s Description:"+user.getDescription());
			System.out.println(screenName+"'s StatusesCount:"+user.getStatusesCount());
			System.out.println(screenName+"'s FollowersCount:"+user.getFollowersCount());
			System.out.println(screenName+"'s FriendsCount:"+user.getFriendsCount());
			System.out.println(screenName+"'s FavouritesCount:"+user.getFavouritesCount());
			System.out.println(screenName+"'s CurrentStatus:"+user.getStatus().getText());
			
			long cursor = -1;
			PagableResponseList<User> followers;
			int num=0;
			do {
				 checkRateLimitStatus();
			     followers = twitter.getFollowersList("kaifulee", cursor);
			     System.out.println("kaifulee followers: " + followers.size());
			     for (User follower : followers) {
			        // TODO: Collect top 10 followers here
			        System.out.println(++num + "\t" + follower.getName() + " has " + follower.getFollowersCount() + " follower(s)");
			        checkRateLimitStatus();
			        if (num %100 == 0) {
						try {
							Thread.sleep(600*1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
			     }
			} while ((cursor = followers.getNextCursor()) != 0);
			
			/*try {
	            Query query = new Query("±°Œı¿¥");
	            QueryResult result;
	            int num=0;
	            do {
	                result = twitter.search(query);
	                List<Status> tweets = result.getTweets();
	                for (Status tweet : tweets) {
	                    System.out.println(++num + "\t" +"@" + tweet.getUser().getScreenName() + " - " + tweet.getText());
	                }
	            } while ((query = result.nextQuery()) != null);
	            System.exit(0);
	        } catch (TwitterException te) {
	            te.printStackTrace();
	            System.out.println("Failed to search tweets: " + te.getMessage());
	            System.exit(-1);
	        }*/
			
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void checkRateLimitStatus()  {
		try {
		RateLimitStatus limit = twitter.getRateLimitStatus().get("/application/rate_limit_status");
		System.out.print("- limit: "+limit.getRemaining() +"\n");
		if (limit.getRemaining() <= 2) {
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
	public void getRateLimitStatus() throws TwitterException{
		OAuthTwitter oAuthTwitter=new OAuthTwitter();
		twitter=oAuthTwitter.oAuthLogin();
		RateLimitStatusListener listener=new RateLimitStatusListenerImpl();
		twitter.addRateLimitStatusListener(listener);
		
		try {
			//twitter.showUser("xieyi64");
			IDs users;
			long cursor=-1;
			Connection conn=DBUtil.getConn();
			Statement stmt=DBUtil.createStmt(conn);
			int j=0;
			do{
				//checkRateLimitStatus();
				if (j==0)
					users=twitter.getFollowersIDs("kaifulee",-1);
				else
					users=twitter.getFollowersIDs("kaifulee", cursor);
				
				long[] ids=users.getIDs();
				for (int i=0;i<ids.length;i++) {
					String sql="insert into kaifulee(followerId,cursorStr) values("+ids[i]+",'"+cursor+"')";
					DBUtil.update(sql, stmt);
					System.out.println("update database success: "+(j*5000+i));
				}
				cursor=users.getNextCursor();
				j++;
				
				checkRateLimitStatus();

			}while (users.hasNext());
		} catch (TwitterException e) {
			e.printStackTrace();
		}finally{
		}
		
		
	}
	public static void main(String[] args) throws TwitterException {
		GetTwitterInfo getTwitterInfo=new GetTwitterInfo();
		//getTwitterInfo.getRateLimitStatus();
		getTwitterInfo.getSpecifiedUser("kaifulee");
	}
}
