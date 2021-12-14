package tourGuide.proxy;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "GpsUtil", url = ":localhost:7911")
public interface GpsUtilProxy {

    @GetMapping(value="/attractions")
    List<Attraction> getAttractions();

    @GetMapping(value="/userLocation")
    VisitedLocation getUserLocation(@RequestParam("userUuid") UUID userUuid);
}
