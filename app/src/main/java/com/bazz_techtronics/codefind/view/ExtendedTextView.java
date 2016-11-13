package com.bazz_techtronics.codefind.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by Nshidbaby on 11/12/2016.
 */

public class ExtendedTextView extends TextView {
    public ExtendedTextView(final Context context) {
        super(context);
    }

    public ExtendedTextView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtendedTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            final CharSequence text = getText();
            setText(null);
            setText(text);
        }
        return super.dispatchTouchEvent(event);
    }
}