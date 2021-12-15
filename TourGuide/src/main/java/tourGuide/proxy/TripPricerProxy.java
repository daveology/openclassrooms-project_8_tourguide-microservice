package tourGuide.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tourGuide.model.Provider;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "TripPricer", url = "localhost:7933")
public interface TripPricerProxy {

    @GetMapping(value="/getPrice")
    List<Provider> getPrice(@RequestParam String key,
                                   @RequestParam ("attractionUuid") UUID attractionUuid,
                                   @RequestParam ("adultsCount") int adultsCount,
                                   @RequestParam ("childrenCount") int childrenCount,
                                   @RequestParam ("nightsNumber") int nightsNumber,
                                   @RequestParam ("rewardsPoints") int rewardsPoints);
}
