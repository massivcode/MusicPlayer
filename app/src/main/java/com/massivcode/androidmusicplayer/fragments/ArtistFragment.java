package com.massivcode.androidmusicplayer.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.adapters.ArtistAdapter;
import com.massivcode.androidmusicplayer.interfaces.Event;
import com.massivcode.androidmusicplayer.util.MusicInfoUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by Ray Choe on 2015-11-23.
 */
public class ArtistFragment extends Fragment {

    private static final String TAG = ArtistFragment.class.getSimpleName();
    private ExpandableListView mListView;
    private ArtistAdapter mAdapter;

    public ArtistFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ArtistAdapter(MusicInfoUtil.getArtistInfo(getActivity()), getActivity(), true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist, container, false);
        mListView = (ExpandableListView)view.findViewById(R.id.artist_ExlistView);
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    // EventBus 용 이벤트 수신
    public void onEvent(Event event) {

//        if (event instanceof MusicEvent) {
//            Log.d(TAG, "재생목록에서 뮤직이벤트를 받았습니다.");
//            mAdapter.swapMusicEvent((MusicEvent) event);
//            mAdapter.notifyDataSetChanged();
//        } else if(event instanceof Playback) {
//            Log.d(TAG, "재생목록에서 플레이백이벤트를 받았습니다.");
//            mAdapter.swapPlayback((Playback) event);
//            mAdapter.notifyDataSetChanged();
//        }

    }


}
