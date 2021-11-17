package tourGuide.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gpsUtil.GpsUtil;
import rewardCentral.RewardCentral;
import tourGuide.service.RewardsService;

/** TourGuide modules configurer.
 */
@Configuration
public class TourGuideModule {
	
	@Bean
	public GpsUtil getGpsUtil() {

		return new GpsUtil();
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
