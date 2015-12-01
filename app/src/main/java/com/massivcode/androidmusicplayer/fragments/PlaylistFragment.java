package com.massivcode.androidmusicplayer.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.massivcode.androidmusicplayer.R;

/**
 * Created by Ray Choe on 2015-11-23.
 */
public class PlaylistFragment extends Fragment {

    private FloatingActionButton mFab;
    private TextView mNotifyNoDataTextView;

    public PlaylistFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        mFab = (FloatingActionButton)view.findViewById(R.id.fab);
        mFab.setOnClickListener((View.OnClickListener) getActivity());

        mNotifyNoDataTextView = (TextView)view.findViewById(R.id.notify_noData_tv);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
