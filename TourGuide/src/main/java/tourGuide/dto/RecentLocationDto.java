package tourGuide.dto;

import gpsUtil.location.Location;

import java.util.List;
import java.util.UUID;

public class RecentLocationDto {

    public UUID userId;
    public List<Location> location;
}
