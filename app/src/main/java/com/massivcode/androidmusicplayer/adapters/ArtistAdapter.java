package com.massivcode.androidmusicplayer.adapters;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.massivcode.androidmusicplayer.R;


/**
 * Created by massivcode on 15. 10. 8.
 */
public class ArtistAdapter extends CursorAdapter {

    private LayoutInflater mInflater;

    public ArtistAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ViewHolder mHolder = new ViewHolder();

        View view = mInflater.inflate(R.layout.item_artist, parent, false);
        mHolder.ArtistTextView = (TextView)view.findViewById(R.id.tv_artist_item);
        mHolder.TotalTextView = (TextView)view.findViewById(R.id.tv_artist_total_item);


        view.setTag(mHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder mHolder = (ViewHolder)view.getTag();
        mHolder.ArtistTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)));
        mHolder.TotalTextView.setText("총 " + cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS))
        + "곡");
    }

    static class ViewHolder {
        TextView ArtistTextView, TotalTextView;
    }
}
