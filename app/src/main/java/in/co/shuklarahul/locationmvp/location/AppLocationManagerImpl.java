package in.co.shuklarahul.locationmvp.location;

import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import in.co.shuklarahul.locationmvp.Model.UserLocation;
import in.co.shuklarahul.locationmvp.utils.AppExecutors;
import timber.log.Timber;


public class AppLocationManagerImpl implements AppLocationManager, OnCompleteListener<LocationSettingsResponse> {

    private static final long UPDATE_INTERVAL = 10 * 1000;    //10 secs update interval

    private LocationAwarePresenter mPresenter = null;
    private FusedLocationProviderClient mFusedLocationClient = null;
    private SettingsClient mSettingsClient = null;
    private LocationRequest mLocationRequest = null;
    private static AppLocationManager INSTANCE = null;
    private HandlerThread mLocationBackgroundThread = null;
    private LocationHandler mLocationThread = null;
    private Handler mainHandler = null;
    private boolean mTriggerGPS = true;

    private class LocationHandler extends Handler {

        LocationHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Message newMsg = Message.obtain(msg);
            mainHandler.sendMessage(newMsg);
        }
    }

    private LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Timber.e("LOcation received -> Length = %d, location = %s",
                    locationResult.getLocations().size(),
                    locationResult.getLocations().get(0).toString());

            Message msg = Message.obtain();
            msg.obj = locationResult.getLocations().get(0);
            mLocationThread.sendMessage(msg);

            if (mTriggerGPS) {

                mTriggerGPS = false;

                // we have got the high accuracy location. Now modify the request for a power balanced
                //location fetching
                mFusedLocationClient.removeLocationUpdates(this);

                //modify the request
                mLocationRequest = new LocationRequest()
                        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                        .setInterval(UPDATE_INTERVAL)
                        .setFastestInterval(UPDATE_INTERVAL);

                //request location updates with modified request
                try {
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                            locationCallback,
                            mLocationThread.getLooper());
                }catch(SecurityException e) {
                    if (mPresenter != null) {
                        mPresenter.checkForPermission();
                    }
                }
            }
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }
    };

    public static AppLocationManager getInstance(@Nullable Context context) {
        if( INSTANCE == null) {
            assert context != null;
            INSTANCE = new AppLocationManagerImpl(context);
        }
        return INSTANCE;
    }

    private AppLocationManagerImpl(Context context) {

        //initialise the fused client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        //initialise the settings client
        mSettingsClient = LocationServices.getSettingsClient(context);

        //prepare the location request
        mLocationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(UPDATE_INTERVAL);

        //prepare a background handler thread. The location callbacks will be executed
        //on this thread
        mLocationBackgroundThread = new HandlerThread("LocationThread", Process.THREAD_PRIORITY_BACKGROUND);

        //start the thread
        mLocationBackgroundThread.start();

        //create background thread handler
        //location updates will be posted to this handler
        mLocationThread = new LocationHandler(mLocationBackgroundThread.getLooper());

        //create handler on the main thread, we will receive the location updates
        //on this handler. The calls to LocationAwarePresenter will be executed on main thread
        mainHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Location loc = (Location)msg.obj;
                mPresenter.onLocationUpdated(new UserLocation(loc.getLatitude(), loc.getLongitude()));
                return true;
            }
        });
    }

    @Override
    public void startLocationUpdates(LocationAwarePresenter forThis) {
        Timber.d("Starting location updates");
        this.mPresenter = forThis;
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mSettingsClient.checkLocationSettings(builder.build())
                .addOnCompleteListener(AppExecutors.getInstance().networkIO(),
                        this);
    }

    @Override
    public void stopLocationUpdates(LocationAwarePresenter forThis) {
        //stop only if the requesting presenter is the current one listening to
        if(this.mPresenter == forThis) {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
            mainHandler.removeCallbacks(null);
            mLocationThread.removeCallbacks(null);
            this.mPresenter = null;
        }
    }

    @Override
    public void onPermissionGranted() {

    }

    @Override
    public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
        try {
            task.getResult(ApiException.class);
            if(mPresenter == null) {
                return;
            }
            try {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, mLocationThread.getLooper());
            }catch(SecurityException e) {
                mPresenter.checkForPermission();
            }

        } catch (ApiException e) {
            switch(e.getStatusCode()) {
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                {
                    try {
                        if(mPresenter != null) {
                            mPresenter.locationSettingsError((ResolvableApiException)e);
                        }
                    } catch(ClassCastException classCastException) {
                        classCastException.printStackTrace();
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }
                }
                break;
                default:{
                    if (mPresenter != null) {
                        mPresenter.showSettingsErrorDialog();
                    }
                }
                break;
            }
        }
    }

    @Override
    public void destroy() {
        mainHandler.removeCallbacks(null);
        mLocationThread.removeCallbacks(null);
        if(mLocationBackgroundThread != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mLocationBackgroundThread.quitSafely();
            }else {
                mLocationBackgroundThread.quit();
            }
        }
        locationCallback = null;
        mSettingsClient = null;
        mPresenter = null;
        mFusedLocationClient = null;
        INSTANCE = null;
    }
}
