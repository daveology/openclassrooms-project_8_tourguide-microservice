package tourGuide.service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.config.InternalTestHelper;
import tourGuide.dto.NearAttractionDto;
import tourGuide.dto.RecentLocationDto;
import tourGuide.model.Tracker;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.proxy.GpsUtilProxy;
import tourGuide.model.Provider;
import tourGuide.proxy.TripPricerProxy;

/** Provides the TourGuide functionalities.
 */
@Service
public class TourGuideService {

	private final ExecutorService executor = Executors.newFixedThreadPool(50);
	private Logger logger = LogManager.getLogger(TourGuideService.class);
	private final RewardsService rewardsService;
	public final Tracker tracker;
	boolean testMode = true;

	@Autowired
	private GpsUtilProxy gpsUtilProxy;
	@Autowired
	private TripPricerProxy tripPricerProxy;

	/** Service test configuration.
	 */
	public TourGuideService(GpsUtilProxy gpsUtilProxy, RewardsService rewardsService, TripPricerProxy tripPricerProxy) {

		this.gpsUtilProxy = gpsUtilProxy;
		this.rewardsService = rewardsService;
		this.tripPricerProxy = tripPricerProxy;
		
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
	 * @return Return the model's rewards.
	 */
	public List<UserReward> getUserRewards(User user) {

		return user.getUserRewards();
	}

	/** User's location.
	 * @param user User object.
	 * @return Return the model's location.
	 */
	public VisitedLocation getUserLocation(User user) {

		if (user.getVisitedLocations().size() == 0) { trackUserLocation(user); }

		return user.getLastVisitedLocation();
	}

	/** User.
	 * @param userName String containing the username.
	 * @return Return the model.
	 */
	public User getUser(String userName) {

		return internalUserMap.get(userName);
	}

	public User getUserById(UUID id) {

		return internalUserMap.values().stream()
				.filter(entry -> entry.getUserId().equals(id))
				.findFirst().get();
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
	 * @return Return model's trip deals.
	 */
	public List<Provider> getTripDeals(User user, UUID attractionUuid) {

		int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		user = getUser(user.getUserName());
		List<Provider> providers = tripPricerProxy.getPrice(tripPricerApiKey, attractionUuid, user.getUserPreferences().getNumberOfAdults(),
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulativeRewardPoints);

		user.setTripDeals(providers);

		return providers;
	}

	/** User's location.
	 * @param user User object.
	 * @return Return model's location.
	 */
	public CompletableFuture<?> trackUserLocation(User user) {

		 return CompletableFuture.supplyAsync(() -> gpsUtilProxy.getUserLocation(user.getUserId()), executor)
				.thenAccept(visitedLocation -> {
					user.addToVisitedLocation(visitedLocation);
					rewardsService.calculateRewards(user);
				});
	}

	/** User's closest attractions.
	 * @param visitedLocation VisitedLocation object.
	 * @return Return the closest attraction to the model.
	 */
	public List<NearAttractionDto> getNearByAttractions(VisitedLocation visitedLocation) {

		List<Attraction> attractions = gpsUtilProxy.getAttractions();
		User user = getUserById(visitedLocation.userId);
		List<NearAttractionDto> nearbyAttractions = new ArrayList<>();
		NearAttractionDto nearAttraction = new NearAttractionDto();
		for(Attraction attraction : attractions) {
			if(rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
				if (nearbyAttractions.size() == 5) {
					return nearbyAttractions;
				}  else {
					nearAttraction.setAttractionName(attraction.attractionName);
					nearAttraction.setAttractionLatitude(attraction.latitude);
					nearAttraction.setAttractionLongitude(attraction.longitude);
					nearAttraction.setAttractionLatitude(visitedLocation.location.latitude);
					nearAttraction.setAttractionLongitude(visitedLocation.location.longitude);
					nearAttraction.setMilesDistance(rewardsService.getDistance(visitedLocation.location,
							new Location(nearAttraction.getAttractionLatitude(),
									nearAttraction.getAttractionLongitude())));
					nearAttraction.setRewardPoints(rewardsService.getRewardPoints(attraction, user));
					nearbyAttractions.add(nearAttraction);
				}
			}
		}

		Random random = new Random();
		if (nearbyAttractions.size() < 5) {
			while(nearbyAttractions.size() < 5) {
				Attraction attraction = attractions.get(random.nextInt(attractions.size()));
				nearAttraction = new NearAttractionDto();
				nearAttraction.setAttractionName(attraction.attractionName);
				nearAttraction.setAttractionLatitude(attraction.latitude);
				nearAttraction.setAttractionLongitude(attraction.longitude);
				nearAttraction.setAttractionLatitude(visitedLocation.location.latitude);
				nearAttraction.setAttractionLongitude(visitedLocation.location.longitude);
				nearAttraction.setMilesDistance(rewardsService.getDistance(visitedLocation.location,
						new Location(nearAttraction.getAttractionLatitude(),
								nearAttraction.getAttractionLongitude())));
				nearAttraction.setRewardPoints(rewardsService.getRewardPoints(attraction, user));
				nearbyAttractions.add(nearAttraction);
			}
		}

		return nearbyAttractions;
	}

	public List<RecentLocationDto> getUsersRecentLocations(int withinDays) {

		List<RecentLocationDto> recentLocations = new ArrayList<>();

		internalUserMap.values().stream().forEach(u -> {
			List<Location> locations = new ArrayList<>();

			u.getVisitedLocations().stream().forEach(v -> {
				//logger.debug("Test: " + LocalDateTime.now().toInstant(ZoneOffset.UTC) + " " + v.timeVisited.toInstant());
				//logger.debug("Test: " + LocalDate.now().until(v.timeVisited.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), ChronoUnit.DAYS) + " days");
				if (Math.abs(LocalDate.now().until(v.timeVisited.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), ChronoUnit.DAYS)) < withinDays) {
					locations.add(v.location);
				}
			});

			recentLocations.add(new RecentLocationDto(u.getUserId(), locations));
		});

		return recentLocations;
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
	public final Map<String, User> internalUserMap = new HashMap<>();

	/** Users testing initializer.
	 */
	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			//logger.debug("Create User: " + user.getUserName());
			generateUserLocationHistory(user);
			
			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}

	/** User's location history generator.
	 * @param user User object.
	 */
	public void generateUserLocationHistory(User user) {

		IntStream.range(0, 3).forEach(i-> {
			user.addToVisitedLocation(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
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
