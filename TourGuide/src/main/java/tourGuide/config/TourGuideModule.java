package tourGuide.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import rewardCentral.RewardCentral;
import tourGuide.proxy.GpsUtilProxy;
import tourGuide.service.RewardsService;

/** TourGuide modules configurer.
 */
@Configuration
public class TourGuideModule {

	@Autowired
	private final GpsUtilProxy gpsUtilProxy;

	public TourGuideModule(GpsUtilProxy gpsUtilProxy) {
		this.gpsUtilProxy = gpsUtilProxy;
	}

	@Bean
	public GpsUtilProxy getGpsUtil() {

		return gpsUtilProxy;
	}
	
	@Bean
	public RewardsService getRewardsService() {

		return new RewardsService(getGpsUtil(), getRewardCentral());
	}
	
	@Bean
	public RewardCentral getRewardCentral() {

		return new RewardCentral();
	}
	
}
