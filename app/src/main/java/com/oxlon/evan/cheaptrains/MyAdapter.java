package com.oxlon.evan.cheaptrains;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
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

        ConstraintLayout constraintLayout;
        ExpandableCard parentLayout;

        TextView durationText;
        TextView changesText;

        ViewHolder(View itemView){
            super(itemView);
            arrivalTimeText = itemView.findViewById(R.id.arrival_time);
            arrow = itemView.findViewById(R.id.arrow);
            departureTimeText = itemView.findViewById(R.id.time);
            typeText = itemView.findViewById(R.id.type);
            priceText = itemView.findViewById(R.id.price);
            dateText = itemView.findViewById(R.id.date);

            durationText = itemView.findViewById(R.id.duration);
            changesText = itemView.findViewById(R.id.changes);

            constraintLayout = itemView.findViewById(R.id.constraint_layout);
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
        ExpandableCard v = (ExpandableCard) LayoutInflater.from(parent.getContext()).inflate(R.layout.my_text_view, parent, false);

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element;
        final Train train = mDataset.get(position);
        holder.arrivalTimeText.setText(train.getArrivalTime());
        holder.departureTimeText.setText(train.getDepartureTime());
        holder.typeText.setText(train.getType());
        holder.priceText.setText(train.getPrice());
        holder.dateText.setText(train.getDate());
        holder.arrow.getDrawable().setColorFilter(holder.arrivalTimeText.getTextColors().getDefaultColor(), PorterDuff.Mode.SRC_IN);

        holder.durationText.setText(train.getDuration());
        holder.changesText.setText(train.getChanges() + " changes");

        if (!train.isExpanded()) {

            holder.durationText.setVisibility(TextView.GONE);
            holder.changesText.setVisibility(TextView.GONE);

            ConstraintSet set = new ConstraintSet();
            set.clone(holder.constraintLayout);

            set.connect(R.id.type, ConstraintSet.TOP, R.id.changes, ConstraintSet.BOTTOM);
            set.applyTo(holder.constraintLayout);

            holder.parentLayout.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int targetHeight = holder.parentLayout.getMeasuredHeight();

            holder.parentLayout.collapse(targetHeight);
        } else {

            holder.durationText.setVisibility(TextView.VISIBLE);
            holder.changesText.setVisibility(TextView.VISIBLE);

            ConstraintSet set = new ConstraintSet();
            set.clone(holder.constraintLayout);

            set.connect(R.id.type, ConstraintSet.TOP, R.id.changes, ConstraintSet.BOTTOM);
            set.applyTo(holder.constraintLayout);

            holder.parentLayout.expand();
        }

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                train.setExpanded(!train.isExpanded());

                if (train.isExpanded()) {
                    holder.durationText.setVisibility(TextView.VISIBLE);
                    holder.changesText.setVisibility(TextView.VISIBLE);
                } else {
                    holder.durationText.setVisibility(TextView.GONE);
                    holder.changesText.setVisibility(TextView.GONE);
                }

                ConstraintSet set = new ConstraintSet();
                set.clone(holder.constraintLayout);

                if (train.isExpanded()) {
                    set.connect(R.id.type, ConstraintSet.TOP, R.id.changes, ConstraintSet.BOTTOM);
                    set.applyTo(holder.constraintLayout);
                    holder.parentLayout.expand();
                } else {
                    holder.durationText.setVisibility(TextView.GONE);
                    holder.changesText.setVisibility(TextView.GONE);

                    set.clone(holder.constraintLayout);

                    set.connect(R.id.type, ConstraintSet.TOP, R.id.changes, ConstraintSet.BOTTOM);
                    set.applyTo(holder.constraintLayout);

                    holder.parentLayout.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    int targetHeight = holder.parentLayout.getMeasuredHeight();

                    holder.parentLayout.collapse(targetHeight);
                }


            }
        });

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
