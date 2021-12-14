package tourGuide.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import rewardCentral.RewardCentral;
import tourGuide.proxy.GpsUtilProxy;
import tourGuide.service.RewardsService;

/** TourGuide modules configurer.
 */
@Configuration
public class TourGuideModule {

	@Bean
	public RewardCentral getRewardCentral() {

		return new RewardCentral();
	}
	
}
