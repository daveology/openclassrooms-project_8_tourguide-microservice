package tourGuide.dto;

import gpsUtil.location.Location;

import java.util.List;
import java.util.UUID;

public class RecentLocationDto {

    public UUID userId;
    public List<Location> location;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public List<Location> getLocation() {
        return location;
    }

    public void setLocation(List<Location> location) {
        this.location = location;
    }
}
