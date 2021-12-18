package tourGuide.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.model.Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tourGuide.proxy.RewardCentralProxy;
import tourGuide.config.InternalTestHelper;
import tourGuide.dto.NearAttractionDto;
import tourGuide.model.VisitedLocation;
import tourGuide.proxy.GpsUtilProxy;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.model.User;
import tourGuide.model.Provider;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestTourGuideService {

	@Autowired
	GpsUtilProxy gpsUtilProxy;
	@Autowired
	RewardCentralProxy rewardCentralProxy;

	private final Logger logger = LogManager.getLogger(TestTourGuideService.class);

	@Test
	public void shouldGetUserLocation() {

		RewardsService rewardsService = new RewardsService(gpsUtilProxy, rewardCentralProxy);
		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilProxy, rewardsService);
		
		User user = tourGuideService.getAllUsers().get(0);
		tourGuideService.trackUserLocation(user);

		tourGuideService.tracker.stopTracking();
		assertTrue(user.getVisitedLocations().get(0).userId.equals(user.getUserId()));
	}
	
	@Test
	public void shouldAddUser() {

		RewardsService rewardsService = new RewardsService(gpsUtilProxy, rewardCentralProxy);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilProxy, rewardsService);
		
		User firstUser = new User(UUID.randomUUID(), "johndoe", "000", "johndoe@tourGuide.com");
		User secondUser = new User(UUID.randomUUID(), "harrypotter", "000", "harrypotter@tourGuide.com");

		tourGuideService.addUser(firstUser);
		tourGuideService.addUser(secondUser);
		
		User retriedFirstUser = tourGuideService.getUser(firstUser.getUserName());
		User retriedSecondUser = tourGuideService.getUser(secondUser.getUserName());

		tourGuideService.tracker.stopTracking();
		assertEquals(firstUser, retriedFirstUser);
		assertEquals(secondUser, retriedSecondUser);
	}
	
	@Test
	public void getAllUsers() {
		RewardsService rewardsService = new RewardsService(gpsUtilProxy, rewardCentralProxy);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilProxy, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);
		
		List<User> allUsers = tourGuideService.getAllUsers();

		tourGuideService.tracker.stopTracking();
		
		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));
	}
	
	@Test
	public void trackUser() {
		RewardsService rewardsService = new RewardsService(gpsUtilProxy, rewardCentralProxy);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilProxy, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.trackUserLocation(user);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(user.getUserId(), user.getVisitedLocations().get(user.getVisitedLocations().size()-1).userId);
	}
	
	@Test
	public void getNearbyAttractions() {
		RewardsService rewardsService = new RewardsService(gpsUtilProxy, rewardCentralProxy);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilProxy, rewardsService);
		Random rand = new Random();

		User user = tourGuideService.getUser("internalUser" + rand.nextInt(99));
		tourGuideService.trackUserLocation(user);
		
		List<NearAttractionDto> attractions = tourGuideService.getNearByAttractions(tourGuideService.getUserLocation(tourGuideService.getUser("internalUser99")));
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(5, attractions.size());
	}

	@Test
	public void shouldGetAllRecentLocations() {

		RewardsService rewardsService = new RewardsService(gpsUtilProxy, rewardCentralProxy);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilProxy, rewardsService);
		Random rand = new Random();
		User user = tourGuideService.getUser("internalUser" + rand.nextInt(99));
		List<VisitedLocation> locations = new ArrayList<>();
		user.setVisitedLocations(new ArrayList<>());

		for (int i = 1 ; i < 10 ; i++) {
			locations.add(new VisitedLocation(user.getUserId(), new Location(99,99),
					Date.from(LocalDateTime.now().minusDays(i).toInstant(ZoneOffset.UTC))));
			logger.debug("Test: " + Date.from(LocalDateTime.now().minusDays(i).toInstant(ZoneOffset.UTC)));
		}
		user.setVisitedLocations(locations);
		tourGuideService.addUser(user);

		List<Location> recentLocations = tourGuideService.getUsersRecentLocations(7).stream()
				.filter(r -> r.getUserId().equals(user.getUserId()))
				.findFirst().get().getLocation();
		User updatedUser = tourGuideService.getUser(user.getUserName());

		assertEquals(6, recentLocations.size());
	}
	
	public void getTripDeals() {
		RewardsService rewardsService = new RewardsService(gpsUtilProxy, rewardCentralProxy);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilProxy, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		List<Provider> providers = tourGuideService.getTripDeals(user);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(10, providers.size());
	}
}
