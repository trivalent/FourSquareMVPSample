package in.co.shuklarahul.locationmvp.searchview;

import in.co.shuklarahul.locationmvp.Model.UserLocation;

public interface SearchPresenter {

    void search(UserLocation location, String userQuery);
    int getItemCount();
    void bindRowView(SearchRowView view, int index);
    void onItemClicked(int index);
    void destroy();
}
