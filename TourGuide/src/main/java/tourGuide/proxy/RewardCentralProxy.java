package tourGuide.proxy;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "RewardCentral", url = "localhost:7922")
public interface ReWardCentralProxy {


}
