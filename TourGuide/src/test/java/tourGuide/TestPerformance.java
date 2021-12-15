package tourGuide;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;
import tourGuide.model.Attraction;
import tourGuide.model.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tourGuide.proxy.RewardCentralProxy;
import tourGuide.proxy.GpsUtilProxy;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.model.User;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestPerformance {

	private final Logger logger = LogManager.getLogger(RewardsService.class);

	@Autowired
	GpsUtilProxy gpsUtilProxy;
	@Autowired
	RewardCentralProxy rewardCentralProxy;
	
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
		RewardsService rewardsService = new RewardsService(gpsUtilProxy, rewardCentralProxy);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilProxy, rewardsService);
		StopWatch stopWatch = new StopWatch();

		//=== TEST SUBJECTS ===
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();

		//=== TIMER START===

		stopWatch.start();

		CompletableFuture<?>[] calculateFutureLocations = new
				CompletableFuture<?>[allUsers.size()];
		for (int i = 0; i<allUsers.size(); i++) {
			calculateFutureLocations[i] = tourGuideService.trackUserLocation(allUsers.get(i));
		}
		CompletableFuture.allOf(calculateFutureLocations).join();

		stopWatch.stop();
		tourGuideService.tracker.stopTracking();
		//=== TIMER END ===

		// Print the time result
		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTotalTimeMillis()) + " seconds.");

		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTotalTimeMillis()));
	}

	/**
	 * Users should be incremented up to 100 000, and test finishes within 20 minutes
	 */
	@Test
	public void highVolumeGetRewards() {

		//=== SERVICES ===
		RewardsService rewardsService = new RewardsService(gpsUtilProxy, rewardCentralProxy);
		StopWatch stopWatch = new StopWatch();

		//=== TIMER START===
		stopWatch.start();

		//=== TEST SUBJECTS ===
		TourGuideService tourGuideService = new TourGuideService(gpsUtilProxy, rewardsService);
		logger.debug("RESULT: " + gpsUtilProxy.getAttractions().get(0));
	    Attraction attraction = gpsUtilProxy.getAttractions().get(0);
		List<User> allUsers = tourGuideService.getAllUsers();
		// Add a visited location for each user
		allUsers.forEach(u -> u.addToVisitedLocation(new VisitedLocation(u.getUserId(), attraction, new Date())));

		// Add asynchronously rewards to the users for their visited locations
		CompletableFuture<?>[] calculateFutureRewards = new
				CompletableFuture<?>[allUsers.size()];
		for (int i = 0; i<allUsers.size(); i++) {
			calculateFutureRewards[i] = rewardsService.calculateRewards(allUsers.get(i));
		}
		CompletableFuture.allOf(calculateFutureRewards).join();

		// Test if each user received the rewards
		allUsers.forEach(user -> assertTrue(user.getUserRewards().size() > 0));

		stopWatch.stop();
		tourGuideService.tracker.stopTracking();
		//=== TIMER END ===

		// Print the time result
		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTotalTimeMillis()) + " seconds.");

		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTotalTimeMillis()));
	}

}
