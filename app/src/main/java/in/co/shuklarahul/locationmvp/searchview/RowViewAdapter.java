package in.co.shuklarahul.locationmvp.searchview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import in.co.shuklarahul.locationmvp.R;

public class RowViewAdapter extends RecyclerView.Adapter<VenueRowView> implements VenueRowView.OnItemClickListener {

    SearchPresenter presenter;

    public RowViewAdapter(SearchPresenter presenter) {
        this.presenter = presenter;
    }

    @NonNull
    @Override
    public VenueRowView onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new VenueRowView(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.venuerowview, viewGroup, false), this);
    }

    @Override
    public void onBindViewHolder(@NonNull VenueRowView venueRowView, int i) {
        presenter.bindRowView(venueRowView, i);
    }

    @Override
    public int getItemCount() {
        return presenter.getItemCount();
    }

    @Override
    public void onItemClicked(int index) {
        presenter.onItemClicked(index);
    }
}
