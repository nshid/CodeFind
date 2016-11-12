package com.bazz_techtronics.codefind.data;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout;
import android.text.style.AlignmentSpan;
import android.text.style.ReplacementSpan;
import android.widget.GridLayout;

/**
 * Created by Nshidbaby on 11/9/2016.
 */

public class SquaredBackgroundSpan extends ReplacementSpan implements AlignmentSpan {

    //private static int CORNER_RADIUS = 8;
    private int textColor = 0;
//    private boolean hasBorder;
    private Tuple backgroundColor;
    private Tuple borderColor;
//    private int maxCharacters = 0;
    private final Paint paintBorder;
    private String alignment;

    public SquaredBackgroundSpan(Context context, String alignment, int textColor, Tuple backgroundColor, Tuple borderColor) {
        super();
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.textColor = textColor;
        this.alignment = alignment.trim();

        this.paintBorder = new Paint();
        this.paintBorder.setStyle(Paint.Style.STROKE);
        this.paintBorder.setAntiAlias(true);
        if (borderColor != null)
            this.paintBorder.setColor((int) Math.round((int)borderColor.getObj()));
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        RectF rect = new RectF(x, top, x + measureText(paint, text, start, end), bottom);
        if (backgroundColor != null) {
            paint.setColor((int) backgroundColor.getObj());
            if ((boolean) backgroundColor.getKey()) {
                canvas.drawRect(rect, paint);
            }
        }
        if (borderColor != null && (boolean)borderColor.getKey())
            canvas.drawRect(rect, paintBorder);

        paint.setColor(textColor);
        canvas.drawText(text, start, end, x, y, paint);
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return Math.round(paint.measureText(text, start, end));
    }

    @Override
    public Layout.Alignment getAlignment() {
        return Layout.Alignment.valueOf(alignment);
    }

    private float measureText(Paint paint, CharSequence text, int start, int end) {
        return paint.measureText(text, start, end);
    }
}
