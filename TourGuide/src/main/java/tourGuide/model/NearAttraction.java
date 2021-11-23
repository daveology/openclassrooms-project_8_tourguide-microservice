package tourGuide.model;

public class NearAttraction {

    public String attractionName;
    public double attractionLatitude;
    public double attractionLongitude;
    public double userLatitude;
    public double userLongitude;
    public double milesDistance;
    public int rewardPoints;

    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    public double getAttractionLatitude() {
        return attractionLatitude;
    }

    public void setAttractionLatitude(double attractionLatitude) {
        this.attractionLatitude = attractionLatitude;
    }

    public double getAttractionLongitude() {
        return attractionLongitude;
    }

    public void setAttractionLongitude(double attractionLongitude) {
        this.attractionLongitude = attractionLongitude;
    }

    public double getUserLatitude() {
        return userLatitude;
    }

    public void setUserLatitude(double userLatitude) {
        this.userLatitude = userLatitude;
    }

    public double getUserLongitude() {
        return userLongitude;
    }

    public void setUserLongitude(double userLongitude) {
        this.userLongitude = userLongitude;
    }

    public double getMilesDistance() {
        return milesDistance;
    }

    public void setMilesDistance(double milesDistance) {
        this.milesDistance = milesDistance;
    }

    public int getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(int rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    public NearAttraction(String attractionName, double attractionLatitude, double attractionLongitude,
                          double userLatitude, double userLongitude, double milesDistance, int rewardPoints) {
        this.attractionName = attractionName;
        this.attractionLatitude = attractionLatitude;
        this.attractionLongitude = attractionLongitude;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
        this.milesDistance = milesDistance;
        this.rewardPoints = rewardPoints;
    }
}
