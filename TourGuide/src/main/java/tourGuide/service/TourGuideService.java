package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

/** Provides the TourGuide functionalities.
 */
@Service
public class TourGuideService {

	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;

	/** Service test configuration.
	 */
	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {

		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;
		
		if(testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}

	/** User's rewards.
	 * @param user User object.
	 * @return Return the user's rewards.
	 */
	public List<UserReward> getUserRewards(User user) {

		return user.getUserRewards();
	}

	/** User's location.
	 * @param user User object.
	 * @return Return the user's location.
	 */
	public VisitedLocation getUserLocation(User user) {

		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
			user.getLastVisitedLocation() :
			trackUserLocation(user);

		return visitedLocation;
	}

	/** User.
	 * @param userName String containing the username.
	 * @return Return the user.
	 */
	public User getUser(String userName) {

		return internalUserMap.get(userName);
	}

	/** Users.
	 * @return Return the users list.
	 */
	public List<User> getAllUsers() {

		return internalUserMap.values().stream().collect(Collectors.toList());
	}

	/** User's creation.
	 * @param user User object.
	 */
	public void addUser(User user) {

		if(!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	/** User's trip deals.
	 * @param user User object.
	 * @return Return user's trip deals.
	 */
	public List<Provider> getTripDeals(User user) {

		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(), 
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);

		return providers;
	}

	/** User's location.
	 * @param user User object.
	 * @return Return user's location.
	 */
	public VisitedLocation trackUserLocation(User user) {

		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);

		return visitedLocation;
	}

	/** User's closest attraction.
	 * @param visitedLocation VisitedLocation object.
	 * @return Return the closest attraction to the user.
	 */
	public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {

		List<Attraction> nearbyAttractions = new ArrayList<>();
		for(Attraction attraction : gpsUtil.getAttractions()) {
			if(rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
				nearbyAttractions.add(attraction);
			}
		}
		
		return nearbyAttractions;
	}

	/** Shutting down the service.
	 */
	private void addShutDownHook() {

		Runtime.getRuntime().addShutdownHook(new Thread() { 
		      public void run() {
		        tracker.stopTracking();
		      } 
		    }); 
	}
	
	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();

	/** Users testing initializer.
	 */
	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);
			
			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}

	/** User's location history generator.
	 * @param user User object.
	 */
	private void generateUserLocationHistory(User user) {

		IntStream.range(0, 3).forEach(i-> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	/** Random location's logitude.
	 * @return Return random location logitude.
	 */
	private double generateRandomLongitude() {

		double leftLimit = -180;
	    double rightLimit = 180;

	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	/** Random location's latitude.
	 * @return Return random location latitude.
	 */
	private double generateRandomLatitude() {

		double leftLimit = -85.05112878;
	    double rightLimit = 85.05112878;

	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	/** Random time.
	 * @return Return random timestamp.
	 */
	private Date getRandomTime() {

		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
		
	    return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}
}
