package in.co.shuklarahul.locationmvp.MapsView;

import android.content.IntentSender;

import com.google.android.gms.common.api.ResolvableApiException;

import in.co.shuklarahul.locationmvp.Model.UserLocation;

public interface MapsView {

    void initMap();
    void updateUserLocation(UserLocation location);
    void resolveApiError(ResolvableApiException e) throws IntentSender.SendIntentException;
    void resolveLocationPermission();


}
