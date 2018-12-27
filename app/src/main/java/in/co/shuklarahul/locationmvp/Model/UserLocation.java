package in.co.shuklarahul.locationmvp.Model;

import android.support.annotation.NonNull;

public class UserLocation {
    private double latitude;
    private double longitude;

    public UserLocation(double lat, double lng) {
        this.latitude = lat;
        this.longitude = lng;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void updateLocation(double newLat, double newLng) {
        this.latitude = newLat;
        this.longitude = newLng;
    }

    @NonNull
    @Override
    public String toString() {
        return "UserLoc [" + String.valueOf(latitude) + ", " + String.valueOf(longitude) + "]";
    }
}
