package com.example.expensly.expense_tracker;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class PieChartView extends View {

    // ── Animation ─────────────────────────────────────────────────────────────
    private static final int ANIMATION_DURATION_MS = 1200;

    private float animationProgress = 1f;

    // ── Data ──────────────────────────────────────────────────────────────────
    private final List<Slice> slices = new ArrayList<>();
    private String centerLabel = "";

    // ── Default slice colours ─────────────────────────────────────────────────
    private static final int[] SLICE_COLORS = {
            0xFFE53935, // Red
            0xFF1E88E5, // Blue
            0xFF43A047, // Green
            0xFFFB8C00, // Orange
            0xFF8E24AA, // Purple
            0xFF00ACC1, // Cyan
            0xFFFFB300, // Amber
            0xFF6D4C41, // Brown
            0xFF546E7A, // Blue-grey
            0xFF00897B  // Teal
    };

    // ── Paints ────────────────────────────────────────────────────────────────
    private final Paint slicePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ── Drawing bounds ────────────────────────────────────────────────────────
    private final RectF  oval        = new RectF();
    private static final float STROKE_WIDTH   = 4f;
    private static final float HOLE_RATIO     = 0.45f; // donut hole size

    // ── Constructor overloads (required for XML inflation) ────────────────────

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(STROKE_WIDTH);

        labelPaint.setColor(Color.WHITE);
        labelPaint.setTypeface(Typeface.DEFAULT_BOLD);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        centerPaint.setColor(Color.parseColor("#1E1E1E"));
        centerPaint.setStyle(Paint.Style.FILL);

        // center text paint
        slicePaint.setStyle(Paint.Style.FILL);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void setData(List<Expense> expenses, double savings) {
        Map<String, Double> data = new LinkedHashMap<>();
        for (Expense e : expenses) {
            // Using title + category to keep it unique if needed
            String key = e.getTitle() + " (" + e.getCategory() + ")";
            data.put(key, e.getAmount());
        }
        if (savings > 0) {
            data.put("Savings", savings);
        }
        setData(data);
    }

    /**
     * Feeds the chart with category totals from the repository.
     * Call {@link #startAnimation()} afterwards to animate it in.
     */
    public void setData(Map<String, Double> data) {
        slices.clear();
        if (data == null || data.isEmpty()) {
            invalidate();
            return;
        }

        double total = 0;
        for (double v : data.values()) total += v;

        if (total <= 0) {
            slices.clear();
            centerLabel = "€0.00";
            invalidate();
            return;
        }

        int colorIndex = 0;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            float sweep = (float) (entry.getValue() / total * 360f);
            int color;
            if (entry.getKey().equals("Savings")) {
                color = 0xFF4CAF50; // Distinct Green for Savings
            } else {
                color = SLICE_COLORS[colorIndex % SLICE_COLORS.length];
                colorIndex++;
            }
            slices.add(new Slice(entry.getKey(), entry.getValue(), sweep, color));
        }

        // build center label
        centerLabel = String.format(java.util.Locale.getDefault(), "€%.2f", total);
        invalidate();
    }

    /**
     * Plays the draw-in animation (arc sweeps from 0° to full circle).
     * Safe to call multiple times — restarts the animation each time.
     */
    public void startAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(ANIMATION_DURATION_MS);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            animationProgress = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        float padding = STROKE_WIDTH * 2;
        float size    = Math.min(w, h) - padding * 2;
        float left    = (w - size) / 2f;
        float top     = (h - size) / 2f;
        oval.set(left, top, left + size, top + size);

        labelPaint.setTextSize(size * 0.045f); // Slightly smaller to fit name + %
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (slices.isEmpty()) {
            drawEmptyState(canvas);
            return;
        }

        float totalSweep = 360f * animationProgress;
        float startAngle = -90f; // start at the top

        for (Slice slice : slices) {
            float sweep = Math.min(slice.sweep, totalSweep);
            if (sweep <= 0) break;

            slicePaint.setColor(slice.color);
            canvas.drawArc(oval, startAngle, sweep, true, slicePaint);
            canvas.drawArc(oval, startAngle, sweep, true, borderPaint);

            // draw labels if slice is large enough
            if (sweep > 30f && animationProgress > 0.8f) {
                float midAngle = (float) Math.toRadians(startAngle + sweep / 2f);
                float radius = oval.width() / 2f * 0.72f;
                float cx = oval.centerX() + radius * (float) Math.cos(midAngle);
                float cy = oval.centerY() + radius * (float) Math.sin(midAngle);

                labelPaint.setAlpha((int) ((animationProgress - 0.8f) / 0.2f * 255));

                String percentage = String.format(java.util.Locale.getDefault(), "%.0f%%", slice.sweep / 360f * 100f);
                String labelText = slice.category + "\n" + percentage;

                // Drawing multiline text
                String[] lines = labelText.split("\n");
                float yOffset = cy - ((lines.length - 1) * labelPaint.getTextSize() / 2f);
                for (String line : lines) {
                    canvas.drawText(line, cx, yOffset + labelPaint.getTextSize() / 3f, labelPaint);
                    yOffset += labelPaint.getTextSize();
                }
            }

            startAngle += sweep;
            totalSweep -= sweep;
            if (totalSweep <= 0) break;
        }

        // draw donut hole
        float holeRadius = oval.width() / 2f * HOLE_RATIO;
        canvas.drawCircle(oval.centerX(), oval.centerY(), holeRadius, centerPaint);

        // draw total amount in center
        if (animationProgress > 0.6f) {
            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(Color.WHITE);
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(holeRadius * 0.38f);
            textPaint.setAlpha((int) ((animationProgress - 0.6f) / 0.4f * 255));
            canvas.drawText(centerLabel,
                    oval.centerX(),
                    oval.centerY() + textPaint.getTextSize() / 3f,
                    textPaint);
        }
    }

    private void drawEmptyState(Canvas canvas) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.LTGRAY);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(8f);
        canvas.drawArc(oval, 0, 360, false, p);

        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.GRAY);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(36f);
        canvas.drawText("No data", oval.centerX(), oval.centerY(), p);
    }

    // ── Inner types ───────────────────────────────────────────────────────────

    /** Immutable data holder for a single pie slice. */
    private static class Slice {
        final String category;
        final double amount;
        final float  sweep;   // degrees
        final int    color;

        Slice(String category, double amount, float sweep, int color) {
            this.category = category;
            this.amount   = amount;
            this.sweep    = sweep;
            this.color    = color;
        }
    }
}
