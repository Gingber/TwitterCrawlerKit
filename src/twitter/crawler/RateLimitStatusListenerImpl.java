package twitter.crawler;

import java.text.SimpleDateFormat;
import java.util.Date;

import twitter4j.RateLimitStatus;
import twitter4j.RateLimitStatusEvent;
import twitter4j.RateLimitStatusListener;
import twitter4j.TwitterException;

public class RateLimitStatusListenerImpl implements RateLimitStatusListener {

	@Override
	public void onRateLimitReached(RateLimitStatusEvent event) {
	
		System.out.println("the account or IP address is hitting the rate limit.");
		System.out.println("the tread will sleep until ratelimit destory");
		SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
		int resetDate = event.getRateLimitStatus().getResetTimeInSeconds();
		System.out.println("the reset time is " + timeFormat.format(resetDate));
		Date currentDate = new Date();
		System.out.println("the current time is " + timeFormat.format(currentDate));
		/*long interval = timeFormat.format(currentDate). - currentDate.getTime();
		System.out.println("the interval is "+interval);*/
		try {
			Thread.sleep(120*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onRateLimitStatus(RateLimitStatusEvent event) {
		// TODO Auto-generated method stub
//		System.out.println("the response contains rate limit status.");
	}

}
