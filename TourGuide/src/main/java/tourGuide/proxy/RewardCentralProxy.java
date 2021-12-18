package tourGuide.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "RewardCentral", url = "localhost:7922")
public interface RewardCentralProxy {

    @GetMapping(value="/attractionRewardsPoints")
    Integer getAttractionRewardPoints(@RequestParam("userUuid") UUID userUuid, @RequestParam("attractionUuid") UUID attractionUuid);
}
