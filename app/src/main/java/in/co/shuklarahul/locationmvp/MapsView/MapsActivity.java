package in.co.shuklarahul.locationmvp.MapsView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.co.shuklarahul.locationmvp.Model.UserLocation;
import in.co.shuklarahul.locationmvp.Model.Venue;
import in.co.shuklarahul.locationmvp.R;
import in.co.shuklarahul.locationmvp.searchview.RowViewAdapter;
import in.co.shuklarahul.locationmvp.searchview.SearchPresenter;
import in.co.shuklarahul.locationmvp.searchview.SearchPresenterImpl;
import in.co.shuklarahul.locationmvp.searchview.SearchView;
import in.co.shuklarahul.locationmvp.utils.SpacingDecorator;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class MapsActivity extends FragmentActivity implements MapsView, OnMapReadyCallback,
        EasyPermissions.PermissionCallbacks, SearchView {

    private static final int REQUEST_CHECK_SETTINGS = 0x011;
    private static final int REQUEST_CHECK_PERMISSIONS = 0x012;
    private static final int REQUEST_SETTINGS_PERMISSIONS = 0x013;

    private static final String[] permissions = new String[]
            {Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION};


    @BindView(R.id.searchBarText)
    EditText mSearchText;

    @BindView(R.id.searchcontainer)
    CardView mSearchContainerView;

    @BindView(R.id.bottom_sheet)
    FrameLayout mBottomSheet;

    @BindView(R.id.listView)
    RecyclerView mSearchResultsRecyclerView;

    @BindView(R.id.searchButton)
    View mSearchButton;

    @BindView(R.id.progress)
    ProgressBar mProgressBar;

    private GoogleMap mMap;
    private MapsPresenter mPresenter;
    private SearchPresenter mSearchPresenter;
    private Marker mUserMarker = null;
    private UserLocation mUserLocation;
    private RowViewAdapter mRowViewAdapter;
    private BottomSheetBehavior mBottomSheetBehavior;
    private Marker mSelectedVenueMarker = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        mPresenter = new MapsPresenterImpl(this, this, this);
        mSearchPresenter = new SearchPresenterImpl(this);

        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSearchPresenter.search(mUserLocation, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mSearchResultsRecyclerView.setLayoutManager(manager);
        mRowViewAdapter = new RowViewAdapter(mSearchPresenter);
        mSearchResultsRecyclerView.setAdapter(mRowViewAdapter);
        mSearchResultsRecyclerView.addItemDecoration(new SpacingDecorator(
                getResources().getDimensionPixelSize(R.dimen.item_spacing)
        ));
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        mSearchButton.setVisibility(View.GONE);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //notify presenter of map ready
        mPresenter.onMapReady();
        resolveLocationPermission();
    }

    @Override
    public void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    public void updateUserLocation(UserLocation location) {
        if (mUserMarker != null) {
            mUserMarker.remove();
        }
        mUserLocation = location;
        mUserMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mUserMarker.getPosition(), 11));
        mPresenter.destroy();
        mSearchButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void resolveLocationPermission() {
        if (EasyPermissions.hasPermissions(this, permissions)) {
            mPresenter.start(this);
        } else {
            EasyPermissions.requestPermissions(this,
                    getString(R.string.request_location_permission), REQUEST_CHECK_PERMISSIONS,
                    permissions);
        }
    }

    @AfterPermissionGranted(REQUEST_CHECK_PERMISSIONS)
    public void onPermissionResolved() {
        Timber.d("All permissions have been granted");
        mPresenter.start(this);
    }

    @Override
    public void resolveApiError(ResolvableApiException e) throws IntentSender.SendIntentException {
        e.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
    }

    @Override
    protected void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        resolveLocationPermission();
                        break;
                    case Activity.RESULT_CANCELED:
                        showErrorBar(false);
                        break;
                    default:
                        break;
                }
                break;
            case REQUEST_SETTINGS_PERMISSIONS: {
                resolveLocationPermission();
            }
            break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Timber.e("Permissions granted");
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        showErrorBar(EasyPermissions.somePermissionPermanentlyDenied(this, Arrays.asList(permissions)));
    }

    private void showErrorBar(final boolean showSettings) {
        if (showSettings) {
            new AppSettingsDialog.Builder(MapsActivity.this)
                    .setRationale(R.string.request_location_permission)
                    .setPositiveButton(R.string.allow)
                    .setNegativeButton(R.string.cancel)
                    .setRequestCode(REQUEST_SETTINGS_PERMISSIONS)
                    .build().show();

            return;
        }
        Snackbar.make(findViewById(R.id.parent), R.string.request_location_permission, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.allow, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        resolveLocationPermission();
                    }
                }).show();
    }

    @Override
    public void showProgress() {
        if (mProgressBar.getVisibility() == View.VISIBLE)
            return;
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void showError() {
        hideProgress();
        hideKeyboard(mSearchText);
        showSearchError();
    }

    private void showSearchError() {
        Snackbar.make(findViewById(R.id.parent), R.string.error_no_search_results, Snackbar.LENGTH_LONG)
                .setAction(R.string.ok, null)
                .show();
    }

    @Override
    public void displayData(List<Venue> data) {
        hideProgress();
        hideKeyboard(mSearchText);
        mRowViewAdapter.notifyDataSetChanged();
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
    }

    @Override
    public void markSelectedVenue(Venue venue) {
        if (mSelectedVenueMarker != null) {
            mSelectedVenueMarker.remove();
        }

        mSelectedVenueMarker = mMap.addMarker(new MarkerOptions().position(
                new LatLng(venue.getLocation().getLat(), venue.getLocation().getLng()))
                .snippet(venue.getLocation().getAddress())
                .title(venue.getName())
        );

        LatLngBounds bounds = new LatLngBounds.Builder().include(mUserMarker.getPosition())
                .include(mSelectedVenueMarker.getPosition()).build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }

    @OnClick(R.id.searchButton)
    public void onFabClicked() {
        int nextState = mSearchContainerView.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE;
        mSearchContainerView.setVisibility(nextState);
        if (nextState == View.VISIBLE) {
            mSearchText.requestFocus();
            showKeyboard(mSearchText);
        }
    }

    private void showKeyboard(@NotNull EditText view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard(@NotNull EditText view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        if(mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            return;
        }
        super.onBackPressed();
    }
}
