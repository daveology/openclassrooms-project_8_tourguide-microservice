package tourGuide;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import rewardCentral.RewardCentral;
import tourGuide.proxy.GpsUtilProxy;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.model.User;

@SpringBootTest
public class TestPerformance {

	@Autowired
	GpsUtilProxy gpsUtilProxy;
	
	/*
	 * A note on performance improvements:
	 *     
	 *     The number of users generated for the high volume tests can be easily adjusted via this method:
	 *     
	 *     		InternalTestHelper.setInternalUserNumber(100000);
	 *     
	 *     
	 *     These tests can be modified to suit new solutions, just as long as the performance metrics
	 *     at the end of the tests remains consistent. 
	 * 
	 *     These are performance metrics that we are trying to hit:
	 *     
	 *     highVolumeTrackLocation: 100,000 users within 15 minutes:
	 *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     *     highVolumeGetRewards: 100,000 users within 20 minutes:
	 *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */

	/**
	 * Users should be incremented up to 100,000, and test finishes within 15 minutes
	 */
	@Test
	public void highVolumeTrackLocation() {

		//=== SERVICES ===
		RewardsService rewardsService = new RewardsService(gpsUtilProxy, new RewardCentral());
		TourGuideService tourGuideService = new TourGuideService(gpsUtilProxy, rewardsService);
		StopWatch stopWatch = new StopWatch();

		//=== TEST SUBJECTS ===
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();

		//=== TIMER START===

		stopWatch.start();
		allUsers.forEach(user -> tourGuideService.trackUserLocation(user));

		stopWatch.stop();
		tourGuideService.tracker.stopTracking();
		//=== TIMER END ===

		// Print the time result
		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 

		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	/**
	 * Users should be incremented up to 100 000, and test finishes within 20 minutes
	 */
	@Test
	public void highVolumeGetRewards() {

		//=== SERVICES ===
		RewardsService rewardsService = new RewardsService(gpsUtilProxy, new RewardCentral());
		StopWatch stopWatch = new StopWatch();

		//=== TIMER START===
		stopWatch.start();

		//=== TEST SUBJECTS ===
		TourGuideService tourGuideService = new TourGuideService(gpsUtilProxy, rewardsService);
	    Attraction attraction = gpsUtilProxy.getAttractions().get(0);
		List<User> allUsers = tourGuideService.getAllUsers();
		// Add a visited location for each user
		allUsers.forEach(u -> u.addToVisitedLocation(new VisitedLocation(u.getUserId(), attraction, new Date())));

		// Add asynchronously rewards to the users for their visited locations
		CompletableFuture<?>[] calculateFutureRewards =
				allUsers.stream()
				.map(rewardsService::calculateRewards)
				.toArray(CompletableFuture[]::new);
		CompletableFuture.allOf(calculateFutureRewards).join();

		// Test if each user received the rewards
		allUsers.forEach(user -> assertTrue(user.getUserRewards().size() > 0));

		stopWatch.stop();
		tourGuideService.tracker.stopTracking();
		//=== TIMER END ===

		// Print the time result
		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 

		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

}
