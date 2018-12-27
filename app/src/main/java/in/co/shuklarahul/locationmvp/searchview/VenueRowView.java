package in.co.shuklarahul.locationmvp.searchview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.co.shuklarahul.locationmvp.R;

public class VenueRowView extends RecyclerView.ViewHolder implements SearchRowView, View.OnClickListener {

    public interface OnItemClickListener {
        void onItemClicked(int index);
    }

    @BindView(R.id.venuename)
    TextView venueName;

    @BindView(R.id.venueaddress)
    TextView venueAddress;

    @BindView(R.id.venuedistance)
    TextView venueDistance;

    OnItemClickListener listener;

    public VenueRowView(@NonNull View itemView, OnItemClickListener listener) {
        super(itemView);
        this.listener = listener;
        itemView.setOnClickListener(this);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void setName(String name) {
        venueName.setText(name);
    }

    @Override
    public void setAddress(String address) {
        venueAddress.setText(address);
    }

    @Override
    public void setDistance(long distance) {
        venueDistance.setText(itemView.getContext().getString(R.string.venue_distance_in_km, distance));
    }

    @Override
    public void onClick(View v) {
        listener.onItemClicked(getAdapterPosition());
    }
}
