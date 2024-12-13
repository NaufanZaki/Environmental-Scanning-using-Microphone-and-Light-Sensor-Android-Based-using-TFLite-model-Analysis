package com.example.fpmobile;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

public class CustomMarkerView extends MarkerView {

    private final TextView tvContent;

    public CustomMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        // Find the TextView inside the custom layout
        tvContent = findViewById(R.id.tvContent);
    }

    // Refresh content whenever the marker is redrawn
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        if (e != null) {
            tvContent.setText(String.format("Value: %.2f", e.getY())); // Update content based on the Entry
        }
        super.refreshContent(e, highlight);
    }


    // Adjust the marker's position
    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2f), -getHeight());
    }
}
