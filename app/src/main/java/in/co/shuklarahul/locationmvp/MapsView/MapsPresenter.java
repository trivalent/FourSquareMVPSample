package in.co.shuklarahul.locationmvp.MapsView;

import android.content.Context;

public interface MapsPresenter {

    void onMapReady();
    void destroy();
    void start(Context context);
    void onPermissionsResolved();
}
