package tourGuide;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import tourGuide.model.Attraction;
import tourGuide.model.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tourGuide.proxy.RewardCentralProxy;
import tourGuide.config.InternalTestHelper;
import tourGuide.proxy.GpsUtilProxy;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.model.User;
import tourGuide.model.UserReward;

@SpringBootTest
public class TestRewardsService {

	@Autowired
	private final GpsUtilProxy gpsUtilProxy;
	@Autowired
	private RewardCentralProxy rewardCentralProxy;

	public TestRewardsService(GpsUtilProxy gpsUtilProxy) {
		this.gpsUtilProxy = gpsUtilProxy;
	}

	@Test
	public void userGetRewards() {
		RewardsService rewardsService = new RewardsService(gpsUtilProxy, rewardCentralProxy);

		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilProxy, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Attraction attraction = gpsUtilProxy.getAttractions().get(0);
		user.addToVisitedLocation(new VisitedLocation(user.getUserId(), attraction, new Date()));
		tourGuideService.trackUserLocation(user);
		List<UserReward> userRewards = user.getUserRewards();
		tourGuideService.tracker.stopTracking();
		assertTrue(userRewards.size() == 1);
	}
	
	@Test
	public void isWithinAttractionProximity() {
		RewardsService rewardsService = new RewardsService(gpsUtilProxy, rewardCentralProxy);
		Attraction attraction = gpsUtilProxy.getAttractions().get(0);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}
	
	// Needs fixed - can throw ConcurrentModificationException
	@Test
	public void nearAllAttractions() {
		RewardsService rewardsService = new RewardsService(gpsUtilProxy, rewardCentralProxy);
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);

		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilProxy, rewardsService);
		
		rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0));
		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));
		tourGuideService.tracker.stopTracking();

		assertEquals(gpsUtilProxy.getAttractions().size(), userRewards.size());
	}
	
}
