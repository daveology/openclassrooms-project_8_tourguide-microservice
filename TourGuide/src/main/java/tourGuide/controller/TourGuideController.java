package tourGuide.controller;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import gpsUtil.location.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.VisitedLocation;
import tourGuide.dto.RecentLocationDto;
import tourGuide.service.TourGuideService;
import tourGuide.model.User;
import tripPricer.Provider;

/** Responsible for processing tourguide and reward services.
 */
@RestController
public class TourGuideController {

	@Autowired
	TourGuideService tourGuideService;

    /** Homepage's endpoint.
     * @return Return to the homepage
     */
    @RequestMapping("/")
    public String index() {

        return "Greetings from TourGuide!";
    }

    /** User's location endpoint.
     * @param userName String containing the username.
     * @return Return the model actual location.
     */
    @RequestMapping("/getLocation") 
    public String getLocation(@RequestParam String userName) {

    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));

		return JsonStream.serialize(visitedLocation.location);
    }
    
    /** User's closest attraction endpoint.
     * @param userName String containing the username.
     * @return Return the closest attraction to the model.
     */
    @RequestMapping("/getNearbyAttractions") 
    public String getNearbyAttractions(@RequestParam String userName) {

    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));

    	return JsonStream.serialize(tourGuideService.getNearByAttractions(visitedLocation));
    }

    /** User's rewards endpoint.
     * @param userName String containing the username.
     * @return Return the model's rewards.
     */
    @RequestMapping("/getRewards") 
    public String getRewards(@RequestParam String userName) {

    	return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
    }

    /** User's recent locations endpoint.
     * @return Return the model's recent locations.
     */
    @RequestMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() {

        List<RecentLocationDto> recentLocations = tourGuideService.getUsersRecentLocations(7);
    	
    	return JsonStream.serialize(recentLocations);
    }

    /** User's trip deals endpoint.
     * @param userName String containing the username.
     * @return Return the model's trip deals.
     */
    @RequestMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String userName) {

    	List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));

    	return JsonStream.serialize(providers);
    }

    /** User's endpoint.
     * @param userName String containing the username.
     * @return Return the model.
     */
    private User getUser(String userName) {

    	return tourGuideService.getUser(userName);
    }
}
