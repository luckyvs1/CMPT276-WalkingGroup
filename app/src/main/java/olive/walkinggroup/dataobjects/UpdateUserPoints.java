package olive.walkinggroup.dataobjects;

import android.app.Activity;

import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class UpdateUserPoints {

    private static final int EARTH_RADIUS_METERS = 6371000;
    private Activity activity;
    private Model instance;
    private User user;
    private Group activeGroup;
    private CurrentLocationHelper currentLocationHelper;

    public UpdateUserPoints(Activity activity) {
        this.activity = activity;
        instance = Model.getInstance();
        user = instance.getCurrentUser();
        activeGroup = instance.getActiveGroup();
    }

    private double getDistanceBetweenTwoPointsInM(GpsLocation firstPoint, GpsLocation secondPoint) {

        double destinationLatitude = secondPoint.getLat();
        double destinationLongitude = secondPoint.getLng();
        double meetingLatitude = firstPoint.getLat();
        double meetingLongitude = firstPoint.getLng();

        //Get the distance of the walk
        // Haversine pseudocode from:https://community.esri.com/groups/coordinate-reference-systems/blog/2017/10/05/haversine-formula
        Double phi_1 = Math.toRadians(destinationLatitude);
        Double phi_2 = Math.toRadians(meetingLatitude);

        Double delta_phi = Math.toRadians(destinationLatitude - meetingLatitude);
        Double delta_lambda = Math.toRadians(destinationLongitude - meetingLongitude);


        //a = sin²(φB - φA/2) + cos φA * cos φB * sin²(λB - λA/2)
        //c = 2 * atan2( √a, √(1−a) )
        //d = R ⋅ c -- distanceInMeters

        Double a = Math.pow(Math.sin(delta_phi / 2.0), 2) + Math.cos(phi_1) * Math.cos(phi_2) * Math.pow(Math.sin(delta_lambda / 2.0), 2);

        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        Double distanceInMeters = EARTH_RADIUS_METERS * c;

        return distanceInMeters;
    }

    private void updateUserPointsEarned(Double distanceInMeters) {
        Integer accumulatedPoints = distanceInMeters.intValue();

        Integer currentPoints = instance.getCurrentUser().getCurrentPoints();
        Integer totalPoints = instance.getCurrentUser().getTotalPointsEarned();

        currentPoints += accumulatedPoints;
        totalPoints += accumulatedPoints;

        instance.getCurrentUser().setTotalPointsEarned(totalPoints);
        instance.getCurrentUser().setCurrentPoints(currentPoints);

        Call<User> caller = instance.getProxy().updateUser(instance.getCurrentUser().getId(), instance.getCurrentUser());
        ProxyBuilder.callProxy(activity, caller, updatedUser -> updateUserResponse(updatedUser));
    }

    private void updateUserResponse(User updatedUser) {
        instance.setCurrentUser(updatedUser);
    }


}
