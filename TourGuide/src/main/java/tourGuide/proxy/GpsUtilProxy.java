package tourGuide.proxy;

import gpsUtil.location.Attraction;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "GpsMicroservice", url = ":localhost:7911")
public interface GpsUtilProxy {

    @GetMapping(value="/attractions")
    List<Attraction> getAttractions();
}
