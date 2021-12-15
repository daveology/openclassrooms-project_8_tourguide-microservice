package tourGuide.model;

import java.util.List;
import java.util.concurrent.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.util.StopWatch;
import tourGuide.proxy.GpsUtilProxy;
import tourGuide.service.TourGuideService;

public class Tracker extends Thread {

	private Logger logger = LogManager.getLogger(Tracker.class);
	private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private GpsUtilProxy gpsUtilProxy;
	private final TourGuideService tourGuideService;
	private boolean stop = false;

	/** Represents the tracker thread.
	 * @param tourGuideService TourGuideService class.
	 */
	public Tracker(TourGuideService tourGuideService) {

		this.gpsUtilProxy = gpsUtilProxy;
		this.tourGuideService = tourGuideService;
		executorService.submit(this);
	}
	
	/** Assures to shut down the tracker thread
	 */
	public void stopTracking() {

		stop = true;
		executorService.shutdownNow();
	}

	/** Tracker thread runner.
	 */
	@Override
	public void run() {

		//=== SERVICES ===
		StopWatch stopWatch = new StopWatch();

		while(true) {
			if(Thread.currentThread().isInterrupted() || stop) {
				logger.debug("Tracker stopping");
				break;
			}

			// Retrieves the users
			List<User> users = tourGuideService.getAllUsers();
			logger.debug("Begin Tracker. Tracking " + users.size() + " users.");

			//=== TIMER START ===
			stopWatch.start();

			// Tracks asynchronously each user's location
			CompletableFuture<?>[] trackFutureUserLocations = new
					CompletableFuture<?>[users.size()];
			for (int i = 0; i<users.size(); i++) {
				VisitedLocation visitedLocation = tourGuideService.trackUserLocation(users.get(i));
				trackFutureUserLocations[i] = CompletableFuture.supplyAsync(() -> visitedLocation);
			}
			CompletableFuture.allOf(trackFutureUserLocations).join();

			stopWatch.stop();
			//=== TIMER END ===

			// Reset the timer and print the time result
			logger.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTotalTimeMillis()) + " seconds.");
			stopWatch.stop();

			// Sleeps 5 seconds
			try {
				logger.debug("Tracker sleeping");
				TimeUnit.SECONDS.sleep(trackingPollingInterval);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
}
