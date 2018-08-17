package com.oxlon.evan.cheaptrains;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<Train> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        // Each item is just a string in this case
        TextView arrivalTimeText;
        ImageView arrow;
        TextView departureTimeText;
        TextView typeText;
        TextView priceText;
        TextView dateText;
        CardView parentLayout;

        ViewHolder(View itemView){
            super(itemView);
            arrivalTimeText = itemView.findViewById(R.id.arrival_time);
            arrow = itemView.findViewById(R.id.arrow);
            departureTimeText = itemView.findViewById(R.id.time);
            typeText = itemView.findViewById(R.id.type);
            priceText = itemView.findViewById(R.id.price);
            dateText = itemView.findViewById(R.id.date);

            parentLayout = itemView.findViewById(R.id.parent_layout);

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    MyAdapter(List<Train> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager
    @NonNull
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.my_text_view, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element;
        final Train train = mDataset.get(position);
        holder.arrivalTimeText.setText(train.getArrivalTime());
        holder.departureTimeText.setText(train.getDepartureTime());
        holder.typeText.setText(train.getType());
        holder.priceText.setText(train.getPrice());
        holder.dateText.setText(train.getDate());
        holder.arrow.getDrawable().setColorFilter(holder.arrivalTimeText.getTextColors().getDefaultColor(), PorterDuff.Mode.SRC_IN);

        holder.parentLayout.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(train.getUrl()));
                // true says the event has been handled
                v.getContext().startActivity(i);
                return true;
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
