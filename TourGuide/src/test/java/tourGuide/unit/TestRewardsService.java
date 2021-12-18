package tourGuide.unit;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
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

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestRewardsService {

	@Autowired
	GpsUtilProxy gpsUtilProxy;
	@Autowired
	RewardCentralProxy rewardCentralProxy;

	@Test
	public void shouldGetUserRewards() {

		RewardsService rewardsService = new RewardsService(gpsUtilProxy, rewardCentralProxy);
		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilProxy, rewardsService);
		
		User user = tourGuideService.getUser("internalUser0");
		Attraction attraction = gpsUtilProxy.getAttractions().get(0);
		user.addToVisitedLocation(new VisitedLocation(user.getUserId(), attraction, new Date()));
		rewardsService.calculateRewards(user).join();

		List<UserReward> userRewards = user.getUserRewards();
		tourGuideService.tracker.stopTracking();
		assertEquals(1, userRewards.size());
	}
	
	@Test
	public void isWithinAttractionProximity() {
		RewardsService rewardsService = new RewardsService(gpsUtilProxy, rewardCentralProxy);
		Attraction attraction = gpsUtilProxy.getAttractions().get(0);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}
	
	@Test
	public void shouldBeNearAllAttractions() {

		RewardsService rewardsService = new RewardsService(gpsUtilProxy, rewardCentralProxy);
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);
		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilProxy, rewardsService);

		rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0)).join();
		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));

		tourGuideService.tracker.stopTracking();
		assertEquals(gpsUtilProxy.getAttractions().size(), userRewards.size());
	}
	
}
