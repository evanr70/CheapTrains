package com.example.evan.cheaptrains;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.evan.cheaptrains.R;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<Train> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        // Each item is just a string in this case
        TextView timeText;
        TextView typeText;
        TextView priceText;
        TextView dateText;
        android.support.constraint.ConstraintLayout parentLayout;

        ViewHolder(View itemView){
            super(itemView);
            timeText = itemView.findViewById(R.id.time);
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
        android.support.constraint.ConstraintLayout v = (android.support.constraint.ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.my_text_view, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element;
        final Train train = mDataset.get(position);
        holder.timeText.setText(train.getTime());
        holder.typeText.setText(train.getType());
        holder.priceText.setText(train.getPrice());
        holder.dateText.setText(train.getDate());

        // TODO: Implement an OnClickListener for items in RecyclerView
//
//        holder.parentLayout.
//
//        View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(train.getUrl()));
//                return false;
//            }
//        };
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
