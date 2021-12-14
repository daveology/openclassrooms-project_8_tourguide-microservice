package tourGuide.proxy;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "GpsMicroservice", url = ":localhost:7911")
public class GpsUtilProxy {


}
