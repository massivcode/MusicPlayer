package com.massivcode.androidmusicplayer.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.massivcode.androidmusicplayer.R;

/**
 * Created by Ray Choe on 2015-11-25.
 */
public class CurrentPlaylistFragment extends DialogFragment implements View.OnClickListener {

    private static final String TAG = CurrentPlaylistFragment.class.getSimpleName();
    private ListView mCurrentPlaylistListView;
    private Button mCurrentPlaylistCloseButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_playlist, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.BLACK));

        setCancelable(false);

        initView(view);


        return view;
    }


    private void initView(View view) {
        mCurrentPlaylistListView = (ListView)view.findViewById(R.id.current_playlistView);
        mCurrentPlaylistCloseButton = (Button)view.findViewById(R.id.current_playlist_closeBtn);

        mCurrentPlaylistCloseButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.current_playlist_closeBtn:
                dismiss();
                break;
        }
    }

    private class CurrentPlaylistAdapter extends BaseAdapter {

        public CurrentPlaylistAdapter() {
        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }
    }


}
