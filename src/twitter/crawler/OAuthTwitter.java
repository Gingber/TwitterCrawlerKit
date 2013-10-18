package twitter.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthSupport;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.conf.PropertyConfiguration;
import twitter4j.QueryResult;

public class OAuthTwitter{
	private static Twitter twitter;
	private static final String PROPERTIES_FILE_NAME="oauth.properties";
	private static final String TWITTER4J_PORPERTIES_FILE_NAME="twitter4j.properties";
	private String consumerKey;
	private String consumerSecret;
	private String requestTokenKey;
	private String requestTokenSecret;
	private String accessTokenKey;
	private String accessTokenSecret;
	private String screenName;
	public OAuthTwitter(String fileName){
		loadProperties(fileName);
	}
	public OAuthTwitter(){
		loadProperties(PROPERTIES_FILE_NAME);
	}
	public String getConsumerKey() {
		return consumerKey;
	}
	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}
	public String getConsumerSecret() {
		return consumerSecret;
	}
	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}
	public String getRequestTokenKey() {
		return requestTokenKey;
	}
	public void setRequestTokenKey(String requestTokenKey) {
		this.requestTokenKey = requestTokenKey;
	}
	public String getRequestTokenSecret() {
		return requestTokenSecret;
	}
	public void setRequestTokenSecret(String requestTokenSecret) {
		this.requestTokenSecret = requestTokenSecret;
	}
	public String getAccessTokenKey() {
		return accessTokenKey;
	}
	public void setAccessTokenKey(String accessTokenKey) {
		this.accessTokenKey = accessTokenKey;
	}
	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}
	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}
	public String getScreenName() {
		return screenName;
	}
	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}
	public Properties loaProperties(){
		return loadProperties(PROPERTIES_FILE_NAME);
	}
	public Properties loadProperties(String fileName){
		Properties properties=new Properties();
		try {
			properties.load(ClassLoader.getSystemResourceAsStream(fileName));
			consumerKey=properties.getProperty("consumer.key");
			consumerSecret=properties.getProperty("consumer.secret");
			requestTokenKey=properties.getProperty("request.token.key");
			requestTokenSecret=properties.getProperty("request.token.secret");
			accessTokenKey=properties.getProperty("access.token.key");
			accessTokenSecret=properties.getProperty("access.token.secret");
			screenName=properties.getProperty("screen.name");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return properties;
	}
	public RequestToken acquireRequestToken(OAuthSupport oAuthSupport) throws TwitterException{
		return oAuthSupport.getOAuthRequestToken();
	}
	public RequestToken acquireRequestToken(Properties properties){
		return new RequestToken(properties.getProperty("access.token.key"),properties.getProperty("access.token.secret"));
	}
	public RequestToken acquireRequestToken(){
		return new RequestToken(requestTokenKey,requestTokenSecret);
	}
	public AccessToken acquireAccessToken(OAuthSupport oAuthSupport,String oauthVerifier) throws TwitterException{
		return oAuthSupport.getOAuthAccessToken(oauthVerifier);
	}
	public AccessToken acquireAccessToken(Properties properties){
		return new AccessToken(properties.getProperty("access.token.key"), properties.getProperty("access.token.secret"));
	}
	public AccessToken acquireAccessToken(){
		return new AccessToken(accessTokenKey,accessTokenSecret);
	}
	public Twitter oAuthFirstLogin(){
		Twitter twitter=new TwitterFactory(new PropertyConfiguration(ClassLoader.getSystemResourceAsStream(TWITTER4J_PORPERTIES_FILE_NAME))).getInstance();
		twitter.setOAuthConsumer(consumerKey, consumerSecret);
		try {
			RequestToken requestToken=acquireRequestToken(twitter);
			System.out.println("Got request token.");
            System.out.println("Request token: "+ requestToken.getToken());
            System.out.println("Request token secret: "+ requestToken.getTokenSecret());
            AccessToken accessToken = null;

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (null == accessToken) {
                System.out.println("Open the following URL and grant access to your account:");
                System.out.println(requestToken.getAuthorizationURL());
                System.out.print("Enter the PIN(if available) and hit enter after you granted access.[PIN]:");
                String pin = br.readLine();
                try{
                    if(pin.length() > 0){
                        accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                    }else{
                        accessToken = twitter.getOAuthAccessToken(requestToken);
                    }
                } catch (TwitterException te) {
                    if(401 == te.getStatusCode()){
                        System.out.println("Unable to get the access token.");
                    }else{
                        te.printStackTrace();
                    }
                }
            }
            System.out.println("Got access token.");
            System.out.println("Access token: "+ accessToken.getToken());
            System.out.println("Access token secret: "+ accessToken.getTokenSecret());
//            Status status = twitter.updateStatus("鍐嶆娴嬭瘯鐢╰witter4j鍙戞帹锛�);
//            System.out.println("Successfully updated the status to [" + status.getText() + "].");
		} catch (TwitterException te) {
			System.out.println("Failed to get timeline: " + te.getMessage());
            System.exit( -1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return twitter;
	}
	public Twitter oAuthLogin(){
		// here's the difference
		Twitter twitter = new TwitterFactory().getInstance();
    	twitter.setOAuthConsumer(consumerKey, consumerSecret);
    	AccessToken accessToken=acquireAccessToken();
		twitter.setOAuthAccessToken(accessToken);
    	// end of difference
		
		return twitter;	
	}
	public boolean oAuthUpdate(Twitter twitter,String updatesString){
		try {
			twitter.updateStatus(updatesString);
		} catch (TwitterException e) {
			System.out.println("Failed to update the status");
			e.printStackTrace();
			return false;
		}
		System.out.println("Successfully updated the status to [" + updatesString + "].");
		return true;
	}
	
	private static void checkRateLimitStatus()  {	
		try {
			Map<String ,RateLimitStatus> rateLimitStatus = twitter.getRateLimitStatus();
            for (String endpoint : rateLimitStatus.keySet()) {
                RateLimitStatus status = rateLimitStatus.get(endpoint);
                System.out.println("Endpoint: " + endpoint);
                System.out.println(" Limit: " + status.getLimit());
                System.out.println(" Remaining: " + status.getRemaining());
                System.out.println(" ResetTimeInSeconds: " + status.getResetTimeInSeconds());
                System.out.println(" SecondsUntilReset: " + status.getSecondsUntilReset());
            }
            //System.exit(0);
			RateLimitStatus rateLimit = twitter.getRateLimitStatus("application").get("/application/rate_limit_status");
			System.out.println("Application Remaining RateLimit =" + rateLimit.getRemaining());
			if (rateLimit != null && rateLimit.getRemaining() <= 2) {
				System.out.println("*** You hit your rate limit. ***");
				int remainingTime = rateLimit.getSecondsUntilReset();
				System.out.println("Twitter request rate limit reached. Waiting " + remainingTime + " seconds ( " + remainingTime/60.0 + " minutes) for rate limit reset.");
				
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
	
	public static void main(String[] args) {
		OAuthTwitter oAuthTwitter=new OAuthTwitter("snscrawler.properties");
		//oAuthTwitter.oAuthUpdate(oAuthTwitter.oAuthLogin(), "@BGingber");
		
		 try {  
	            
			 ConfigurationBuilder cb = new ConfigurationBuilder();  
	            
			 cb.setDebugEnabled(true)      
			 	.setOAuthConsumerKey("Kyy34TMm7ItnLUJPDqKhGw")    
			 	.setOAuthConsumerSecret("ZlCKHZTiCNjrHQCCO8QY29QkwUrVQNJtkuxdr50ao")     
			 	.setOAuthAccessToken("1001113153-17ULhUKJaD4J9Z0dlGywdbyuTb1nz6tFqeQrDk8")    
			 	.setOAuthAccessTokenSecret("IwyjCc3lxlCrAt0QbXytrLlqcZlP8fPsORgB3rXeI");  
			 cb.setJSONStoreEnabled(true);  
	              
			 TwitterFactory tf = new TwitterFactory(cb.build());           
			 twitter = tf.getInstance();
	            
			 //Twitter twitter = new TwitterFactory().getInstance();  
             long cursor = -1;  
             IDs ids;  
             IDs id2s;  
             System.out.println("Listing followers's ids.");  
             try {     
            	 /*ids = twitter.getFollowersIDs("BGingber", -1);                  
            	 id2s = twitter.getFriendsIDs("BGingber", -1);*/
            	 //User user=twitter.showUser("BGingber");
            	 String ScreenName = "ddlovato";
            	 User user = twitter.showUser(ScreenName);     
            	 // 获取用户推文个数
            	 System.out.println(user.getStatusesCount());
            	 int PageCount = user.getStatusesCount()/200 + 1;
            	 int i = 0;
            	 for(int page = 1; page <= PageCount; page++) {
            		 Paging paging = new Paging(page, 200); 
            		 List<Status> statuses = twitter.getUserTimeline(ScreenName, paging);
                	 for (Status each : statuses) {
                		 System.out.println("Sent by: @" + each.getUser().getScreenName() + " - " + each.getUser().getName() + "\n" 
                				 + ++i + "\t" 
                	    		 + each.getId() + "\t" 
                	    		 + each.getText() + "\t" 
                	    		 + each.getRetweetCount() + "\t"
                	     		 + each.getContributors() + "\t"
                	     		 + each.getCreatedAt() + "\t"
                	     		 + each.getAccessLevel() + "\t"
                	    		 + "\n");
                	 }
                	 checkRateLimitStatus();
                	
            	 }
                 
                /* long[] length = id2s.getIDs();  
                 int count = length.length;  
                 System.out.println(count);    
                 for (long id : id2s.getIDs()) {   
                	 System.out.println(id);     
                	 System.out.println(twitter.showUser(id));             
                 }  */
             } catch (Exception e) {  
                e.printStackTrace();  
             }   	 
		 } catch (Exception e) {  
            e.printStackTrace(); 
		 }   
	}
}	
