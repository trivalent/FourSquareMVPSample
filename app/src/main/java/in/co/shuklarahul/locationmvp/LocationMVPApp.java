package in.co.shuklarahul.locationmvp;

import android.app.Application;

import in.co.shuklarahul.locationmvp.location.AppLocationManagerImpl;
import timber.log.Timber;

public class LocationMVPApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
    }
}
