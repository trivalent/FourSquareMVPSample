package in.co.shuklarahul.locationmvp.searchview;

import in.co.shuklarahul.locationmvp.Model.FourSquareResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FourSquareInterface {

    String API_BASE = "https://api.foursquare.com/v2/";

    @GET("venues/search")
    Call<FourSquareResponse> requestSearch(
            @Query("client_id") String clientId,
            @Query("client_secret") String clientSecret,
            @Query("v") String v,
            @Query("ll") String userLatLng,
            @Query("query") String query);

}
