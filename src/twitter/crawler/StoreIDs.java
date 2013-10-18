package twitter.crawler;

import twitter4j.Twitter;

public class StoreIDs {
	public void StoreFollowersId(String screenName){
		OAuthTwitter oAuthTwitter=new OAuthTwitter();
		Twitter twitter=oAuthTwitter.oAuthLogin();
		twitter.addRateLimitStatusListener(new RateLimitStatusListenerImpl());
		
	}
	
	public void StoreFollowersInd(int id){
		
	}
}
