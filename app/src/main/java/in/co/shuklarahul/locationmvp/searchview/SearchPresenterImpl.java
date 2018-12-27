package in.co.shuklarahul.locationmvp.searchview;

import java.util.ArrayList;
import java.util.List;

import in.co.shuklarahul.locationmvp.Model.FourSquareResponse;
import in.co.shuklarahul.locationmvp.Model.UserLocation;
import in.co.shuklarahul.locationmvp.Model.Venue;
import okhttp3.internal.http.RetryAndFollowUpInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class SearchPresenterImpl implements SearchPresenter, Callback<FourSquareResponse> {

    private static final String CLIENT_ID = "1NX5QJ1GTE5S2VNPWN22LBXOZTBSDW3TDRD12I55COUY0KTA";
    private static final String CLIENT_SECRET = "ONHXLMFGTIOZPB2Y41M2EUOMMPKRSV0EZRF0KYJNI30UU2S0";
    private static final String VERSION_DATE = "20181227";

    private SearchView mView;
    Call<FourSquareResponse> query;
    List<Venue> results;

    private FourSquareInterface fourSquareInterface;

    public SearchPresenterImpl(SearchView view) {
        this.mView = view;
        Retrofit retrofit = new Retrofit.Builder().baseUrl(FourSquareInterface.API_BASE)
                .addConverterFactory(GsonConverterFactory.create()).build();

        fourSquareInterface = retrofit.create(FourSquareInterface.class);
    }

    @Override
    public void search(UserLocation location, String userQuery) {
        Timber.d("Starting search for %s, at user location = %s", userQuery, location.toString());
        String loc = location.getLatitude() + "," + location.getLongitude();

        //cancel the already running query
        if(query != null) {
            query.cancel();
            mView.hideProgress();
        }

        //start the new query
        query = fourSquareInterface.requestSearch(CLIENT_ID, CLIENT_SECRET, VERSION_DATE, loc, userQuery);

        //enque callback
        query.enqueue(this);

        mView.showProgress();
    }

    @Override
    public void onResponse(Call<FourSquareResponse> call, Response<FourSquareResponse> response) {
        mView.hideProgress();
        if(response.isSuccessful()) {
            FourSquareResponse fourSquareResponse = response.body();
            in.co.shuklarahul.locationmvp.Model.Response r = fourSquareResponse.getResponse();
            results = r.getVenues();
            if(results.size() > 0) {
                mView.displayData(results);
            }else {
                mView.showError();
            }
        }else {
            if(!call.isCanceled()) {
                Timber.e(response.raw().toString());
                mView.showError();
            }
        }
        query = null;
    }

    @Override
    public void onFailure(Call<FourSquareResponse> call, Throwable t) {
        if(!call.isCanceled()) {
            t.printStackTrace();
            mView.showError();
        }
    }

    @Override
    public void destroy() {
        if(query != null) {
            query.cancel();
        }
        query = null;
    }

    @Override
    public void bindRowView(SearchRowView view, int index) {
        Venue venue = results.get(index);
        view.setName(venue.getName());
        view.setAddress(venue.getLocation().getAddress());
        view.setDistance(venue.getLocation().getDistance()/1000);
    }

    @Override
    public int getItemCount() {
        return results != null ? results.size() : 0;
    }

    @Override
    public void onItemClicked(int index) {
        mView.markSelectedVenue(results.get(index));
    }
}
