package in.co.shuklarahul.locationmvp.MapsView;

import android.content.Context;
import android.content.IntentSender;
import android.support.annotation.MainThread;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.api.ResolvableApiException;

import java.util.logging.Handler;

import javax.security.auth.callback.Callback;

import in.co.shuklarahul.locationmvp.Model.UserLocation;
import in.co.shuklarahul.locationmvp.location.AppLocationManager;
import in.co.shuklarahul.locationmvp.location.AppLocationManagerImpl;
import in.co.shuklarahul.locationmvp.location.LocationAwarePresenter;
import timber.log.Timber;

public class MapsPresenterImpl implements MapsPresenter, LocationAwarePresenter {

    private MapsView mView;
    private boolean isListeningForUpdates = false;

    public MapsPresenterImpl(MapsView view, FragmentActivity activity, Context context) {
        assert view != null;
        assert activity != null;
        assert context != null;
        this.mView = view;
        mView.initMap();
    }

    @Override
    public void onMapReady() {
    }

    @Override
    public void destroy() {
        if (isListeningForUpdates) {
            AppLocationManagerImpl.getInstance(null).stopLocationUpdates(this);
        }
        isListeningForUpdates = false;
    }

    @Override
    public void start(Context context) {
        AppLocationManagerImpl.getInstance(context).startLocationUpdates(this);
        isListeningForUpdates = true;
    }

    @Override
    public void onLocationUpdated(UserLocation newLocation) {
        mView.updateUserLocation(newLocation);
    }

    @Override
    public void checkForPermission() {
        mView.resolveLocationPermission();
    }

    @Override
    public void locationSettingsError(ResolvableApiException e) throws IntentSender.SendIntentException {
        mView.resolveApiError(e);
    }

    @Override
    public void showSettingsErrorDialog() {
        Timber.d("Asked to show settings error dialog");
    }

    @Override
    public void onPermissionsResolved() {
        AppLocationManagerImpl.getInstance(null).onPermissionGranted();
    }
}
