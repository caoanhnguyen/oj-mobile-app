package com.kma.oj_app_mobile.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class BarChartView extends View {

    public static class BarEntry {
        public String label;
        public double value;
        public BarEntry(String label, double value) {
            this.label = label;
            this.value = value;
        }
    }

    private List<BarEntry> entries = new ArrayList<>();
    private int barColor = Color.parseColor("#ffa116");
    // Slot width so ~5 bars are visible per screen
    private float slotWidthPx = 0f;

    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        // 5 bars per screen: slotWidth = screenWidth - margin/padding / 5
        float screenWidth = getResources().getDisplayMetrics().widthPixels;
        slotWidthPx = (screenWidth - 56 * density) / 5f;

        gridPaint.setColor(Color.parseColor("#333333"));
        gridPaint.setStrokeWidth(1f);
        gridPaint.setStyle(Paint.Style.STROKE);

        axisPaint.setColor(Color.parseColor("#555555"));
        axisPaint.setTextSize(10 * density);
        axisPaint.setAntiAlias(true);

        barPaint.setStyle(Paint.Style.FILL);

        labelPaint.setColor(Color.parseColor("#8a8a8a"));
        labelPaint.setTextSize(10 * density);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setAntiAlias(true);

        valuePaint.setColor(Color.WHITE);
        valuePaint.setTextSize(11 * density);
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setAntiAlias(true);
    }

    public void setData(List<BarEntry> entries, int barColor) {
        this.entries = entries;
        this.barColor = barColor;
        barPaint.setColor(barColor);
        requestLayout(); // re-measure width
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Expand width to fit all bars (so parent HorizontalScrollView can scroll)
        int n = entries != null ? entries.size() : 0;
        int desiredWidth = (int) (16 * getResources().getDisplayMetrics().density + slotWidthPx * Math.max(n, 5));
        int w = resolveSize(desiredWidth, widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(Math.max(w, desiredWidth), h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (entries == null || entries.isEmpty()) return;

        float density = getResources().getDisplayMetrics().density;

        float width = getWidth();
        float height = getHeight();

        // Layout constants (all in px)
        float paddingLeft = 8 * density;    // symmetrical padding
        float paddingRight = 8 * density;
        float paddingTop = 24 * density;    // space for value labels on top of bars
        float paddingBottom = 28 * density; // space for X axis labels

        float chartLeft = paddingLeft;
        float chartRight = width - paddingRight;
        float chartTop = paddingTop;
        float chartBottom = height - paddingBottom;
        float chartHeight = chartBottom - chartTop;
        
        int n = entries.size();
        // Use slotWidthPx regardless of actual view width, so bars stay consistent
        float chartWidth = slotWidthPx * n;

        // Find max value
        double maxValue = 1;
        for (BarEntry e : entries) {
            if (e.value > maxValue) maxValue = e.value;
        }

        // Round max up to a nice number
        double nicedMax = niceMax(maxValue);

        // Draw Y-axis grid lines: 0, mid, max
        double[] gridValues = {0, nicedMax / 2.0, nicedMax};
        for (double gv : gridValues) {
            float y = chartBottom - (float) (gv / nicedMax) * chartHeight;

            // Grid line
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint);

            // Y-axis label floats above the line
            String label = formatValue(gv);
            canvas.drawText(label, chartLeft + 2 * density, y - 4 * density, axisPaint);
        }

        // Draw bars
        float totalBarArea = slotWidthPx * n; // total drawing width
        float barWidth = Math.min(slotWidthPx * 0.5f, 28 * density);
        float slotWidth = slotWidthPx;

        for (int i = 0; i < n; i++) {
            BarEntry entry = entries.get(i);
            double value = entry.value;

            float slotCenter = chartLeft + slotWidth * i + slotWidth / 2f;
            float barLeft = slotCenter - barWidth / 2f;
            float barRight = slotCenter + barWidth / 2f;

            float barHeight = (float) (value / nicedMax) * chartHeight;
            if (barHeight < 2 * density) barHeight = 2 * density; // min visible height

            float barTop = chartBottom - barHeight;

            // Draw bar with rounded top corners
            RectF rect = new RectF(barLeft, barTop, barRight, chartBottom);
            float cornerRadius = 4 * density;
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, barPaint);

            // Draw value label on top of bar
            String valLabel = formatValue(value);
            canvas.drawText(valLabel, slotCenter, barTop - 4 * density, valuePaint);

            // Draw X-axis label (username)
            String name = entry.label != null ? entry.label : "";
            if (name.length() > 10) name = name.substring(0, 9) + "…";
            canvas.drawText(name, slotCenter, chartBottom + 16 * density, labelPaint);
        }
    }

    /** Round up maximum value to a nice human-readable number */
    private double niceMax(double rawMax) {
        if (rawMax <= 0) return 1;
        double magnitude = Math.pow(10, Math.floor(Math.log10(rawMax)));
        double normalized = rawMax / magnitude;
        double nice;
        if (normalized <= 1) nice = 1;
        else if (normalized <= 2) nice = 2;
        else if (normalized <= 5) nice = 5;
        else nice = 10;
        return nice * magnitude;
    }

    private String formatValue(double val) {
        if (val == (long) val) return String.format("%d", (long) val);
        return String.format("%.1f", val);
    }
}
