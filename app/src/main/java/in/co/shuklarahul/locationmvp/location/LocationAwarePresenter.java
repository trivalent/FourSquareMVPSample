package in.co.shuklarahul.locationmvp.location;

import android.content.IntentSender;

import com.google.android.gms.common.api.ResolvableApiException;

import in.co.shuklarahul.locationmvp.Model.UserLocation;

public interface LocationAwarePresenter {

    void onLocationUpdated(UserLocation newLocation);
    void checkForPermission();
    void locationSettingsError(ResolvableApiException e) throws IntentSender.SendIntentException;
    void showSettingsErrorDialog();

}
