package in.co.shuklarahul.locationmvp.searchview;

import java.util.List;

import in.co.shuklarahul.locationmvp.Model.Venue;

public interface SearchView {

    void showProgress();
    void hideProgress();
    void showError();
    void displayData(List<Venue> data);
    void markSelectedVenue(Venue venue);
}
