package com.oxlon.evan.cheaptrains;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import static android.content.ContentValues.TAG;

public class ExpandableCard extends CardView {
    public ExpandableCard(@NonNull Context context) {
        super(context);
    }

    public ExpandableCard(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void expand() {
        final int initialHeight = getHeight();

        measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int targetHeight = getMeasuredHeight();

        final int distanceToExpand = targetHeight - initialHeight;

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1){
                    // do this after expanded
                    int a = 1;
                }

                getLayoutParams().height = (int) (initialHeight + (distanceToExpand * interpolatedTime));
                requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((long) distanceToExpand);
        startAnimation(a);
    }

    public void collapse(int collapsedHeight) {
        final int initialHeight = getMeasuredHeight();

        final int distanceToCollapse = (int) (initialHeight - collapsedHeight);

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1){
                    // do this after collapsed
                    int a = 1;
                }

                Log.i(TAG, "Collapse | InterpolatedTime = " + interpolatedTime);

                getLayoutParams().height = (int) (initialHeight - (distanceToCollapse * interpolatedTime));
                requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }

        };

        a.setDuration((long) distanceToCollapse);
        startAnimation(a);
    }













}
