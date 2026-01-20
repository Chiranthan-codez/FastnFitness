package com.easyfitness.graph;

import android.content.Context;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Graph {

    // ✅ ENUM used by FonteGraphFragment
    public enum zoomType {
        ZOOM_ALL,
        ZOOM_WEEK,
        ZOOM_MONTH,
        ZOOM_YEAR
    }

    private final LineChart mChart;
    private final String mChartName;
    private zoomType currentZoom = zoomType.ZOOM_ALL;

    public Graph(Context context, LineChart chart, String name) {
        mChart = chart;
        mChartName = name;

        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setHorizontalScrollBarEnabled(true);
        mChart.setVerticalScrollBarEnabled(true);
        mChart.setAutoScaleMinMaxEnabled(true);
        mChart.setDrawBorders(true);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);

        // ✅ Modern formatter (MPAndroidChart v3+)
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat format =
                new SimpleDateFormat("dd/MM/yy", Locale.getDefault());

            @Override
            public String getFormattedValue(float value) {
                return format.format(new Date((long) value));
            }
        });
    }

    // ✅ Called from FonteGraphFragment
    public void setZoom(zoomType zoom) {
        currentZoom = zoom;

        // Reset zoom first
        mChart.fitScreen();

        switch (zoom) {
            case ZOOM_WEEK:
                mChart.zoom(7f, 1f, 0f, 0f);
                break;

            case ZOOM_MONTH:
                mChart.zoom(30f, 1f, 0f, 0f);
                break;

            case ZOOM_YEAR:
                mChart.zoom(365f, 1f, 0f, 0f);
                break;

            case ZOOM_ALL:
            default:
                // No zoom (full range)
                break;
        }

        mChart.invalidate();
    }

    public void draw(List<Entry> entries) {
        if (entries == null || entries.isEmpty()) return;

        LineDataSet dataSet = new LineDataSet(entries, mChartName);
        LineData data = new LineData(dataSet);

        // ✅ Modern formatter
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        mChart.setData(data);
        mChart.invalidate();
    }

    public LineChart getLineChart() {
        return mChart;
    }
}

