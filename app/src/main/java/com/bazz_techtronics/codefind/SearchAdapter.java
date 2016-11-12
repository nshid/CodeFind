package com.bazz_techtronics.codefind;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * {@link SearchAdapter} exposes a list of code topics
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */

public class SearchAdapter extends CursorAdapter {
    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_NORMAL = 0;
    private static final int VIEW_TYPE_TABLET = 1;

    // Flag to determine if we want to use a separate view for "tablets".
    private boolean mUseNormalLayout = true;

    /**
     * Cache of the children views for a search list item.
     */
    public static class ViewHolder {
        public ViewHolder(View view) {
        }
    }

    public SearchAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;

        View view =  LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getPosition());
    }

    public void setUseNormalLayout(boolean useNormalLayout) {
        mUseNormalLayout = useNormalLayout;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseNormalLayout) ? VIEW_TYPE_NORMAL : VIEW_TYPE_TABLET;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
}
