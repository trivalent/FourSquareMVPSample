package in.co.shuklarahul.locationmvp.location;

public interface AppLocationManager {

    void startLocationUpdates(LocationAwarePresenter forThis);
    void stopLocationUpdates(LocationAwarePresenter forThis);
    void onPermissionGranted();
    void destroy();
}
