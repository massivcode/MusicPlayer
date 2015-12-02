package com.massivcode.androidmusicplayer.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.adapters.PlaylistAdapter;
import com.massivcode.androidmusicplayer.database.MyPlaylistFacade;
import com.massivcode.androidmusicplayer.interfaces.Event;
import com.massivcode.androidmusicplayer.interfaces.MusicEvent;
import com.massivcode.androidmusicplayer.interfaces.PlayBack;
import com.massivcode.androidmusicplayer.interfaces.ReloadPlaylist;

import de.greenrobot.event.EventBus;

/**
 * Created by Ray Choe on 2015-11-23.
 */
public class PlaylistFragment extends Fragment {

    private static final String TAG = PlayerFragment.class.getSimpleName();
    private FloatingActionButton mFab;
    private TextView mNotifyNoDataTextView;
    private ExpandableListView mListView;

    private MyPlaylistFacade mFacade;
    private PlaylistAdapter mAdapter;

    public PlaylistFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFacade = new MyPlaylistFacade(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        mFab = (FloatingActionButton)view.findViewById(R.id.fab);
        mFab.setOnClickListener((View.OnClickListener) getActivity());

        mNotifyNoDataTextView = (TextView)view.findViewById(R.id.notify_noData_tv);
        mListView = (ExpandableListView)view.findViewById(R.id.playlist_listView);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);



        if(Build.VERSION.SDK_INT < 23) {
//            mAdapter = new ArtistAdapter(MusicInfoLoadUtil.getArtistInfo(getActivity()), getActivity(), true);
            mListView.setAdapter(mAdapter);
        } else {
            if(getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                if(mAdapter == null) {
                    if(mFacade.isAlreadyExist()) {
                        mNotifyNoDataTextView.setVisibility(View.GONE);
                        EventBus.getDefault().post(new ReloadPlaylist());
                    } else {
                        mNotifyNoDataTextView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }

        mListView.setOnChildClickListener((ExpandableListView.OnChildClickListener) getActivity());
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

        if (event instanceof MusicEvent) {
//            Log.d(TAG, "아티스트에서 뮤직이벤트를 받았습니다.");
            mAdapter.swapMusicEvent((MusicEvent) event);
            mAdapter.notifyDataSetChanged();
        } else if(event instanceof PlayBack) {
//            Log.d(TAG, "아티스트에서 플레이백이벤트를 받았습니다.");
            PlayBack playback = (PlayBack) event;
            if (mAdapter.getPlayback() == null || mAdapter.getPlayback().isPlaying() != playback.isPlaying()) {
                mAdapter.swapPlayback(playback);
                mAdapter.notifyDataSetChanged();
            }
        } else if(event instanceof ReloadPlaylist) {
            if(mAdapter == null) {
                mAdapter = new PlaylistAdapter(mFacade.getAllUserPlaylist(), getActivity(), true);
            } else {
                mAdapter.changeCursor(mFacade.getAllUserPlaylist());
                mAdapter.notifyDataSetChanged();
            }
            mListView.setAdapter(mAdapter);
        }

    }
}
