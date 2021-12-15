package tourGuide.service;

import java.util.*;
import java.util.concurrent.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.proxy.RewardCentralProxy;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.proxy.GpsUtilProxy;

/** Provides the rewards functionalities.
 */
@Service
public class RewardsService {

	private Logger logger = LogManager.getLogger(RewardsService.class);

	private final ExecutorService executor = Executors.newFixedThreadPool(100);

	private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;

	@Autowired
	private final GpsUtilProxy gpsUtilProxy;
	@Autowired
	private final RewardCentralProxy rewardsCentralProxy;

	public RewardsService(GpsUtilProxy gpsUtilProxy, RewardCentralProxy rewardsCentralProxy) {

		this.gpsUtilProxy = gpsUtilProxy;
		this.rewardsCentralProxy = rewardsCentralProxy;
	}
	
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	/** Add the user a reward if an attraction is visited.
	 * @param user User object.
	 */
	public CompletableFuture<?> calculateRewards(User user) {

		return CompletableFuture.runAsync(() -> {
		Queue<Attraction> attractionsList = new ConcurrentLinkedQueue<>();
		attractionsList.addAll(gpsUtilProxy.getAttractions());
		Queue<VisitedLocation> userVisitedLocations = new ConcurrentLinkedQueue<>();
		userVisitedLocations.addAll(user.getVisitedLocations());

			userVisitedLocations.stream().forEach(visitedLocation -> {
					attractionsList.stream()
							.filter(attraction -> nearAttraction(visitedLocation, attraction))
							.forEach(attraction -> {
								if(user.getUserRewards().parallelStream().noneMatch(r ->
										r.attraction.attractionName.equals(attraction.attractionName))) {
									user.addUserReward(new UserReward(visitedLocation,
											attraction,
											getRewardPoints(attraction, user)));
								}
							});
			});
		}, executor);
	}

	/** Tell if the an attraction is close.
	 * @param attraction Attraction object
	 * @param location User's location
	 * @return Return boolean for confirmation.
	 */
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {

		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}

	/** Tell if the user is enough close to the attraction to be visited.
	 * @param visitedLocation User's location
	 * @param attraction Attraction object
	 * @return Return boolean for confirmation.
	 */
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {

		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}

	public int getRewardPoints(Attraction attraction, User user) {

		return rewardsCentralProxy.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}

	/** Calculate the distance in miles.
	 * @param loc1 User's location
	 * @param loc2 Attraction's location
	 * @return Return the distance between the two location.
	 */
	public double getDistance(Location loc1, Location loc2) {

        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;

        return statuteMiles;
	}
}
