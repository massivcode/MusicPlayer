/*
 * Copyright 2015. Pureum Choe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.massivcode.androidmusicplayer.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.adapters.ArtistAdapter;
import com.massivcode.androidmusicplayer.events.Event;
import com.massivcode.androidmusicplayer.events.InitEvent;
import com.massivcode.androidmusicplayer.events.MusicEvent;
import com.massivcode.androidmusicplayer.events.PlayBack;
import com.massivcode.androidmusicplayer.utils.MusicInfoLoadUtil;

import de.greenrobot.event.EventBus;


public class ArtistFragment extends Fragment {

    private static final String TAG = ArtistFragment.class.getSimpleName();
    private ExpandableListView mListView;
    private ArtistAdapter mAdapter;

    public ArtistFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist, container, false);
        mListView = (ExpandableListView)view.findViewById(R.id.artist_ExlistView);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(Build.VERSION.SDK_INT < 23) {
            mAdapter = new ArtistAdapter(MusicInfoLoadUtil.getArtistInfo(getActivity()), getActivity(), true);
            mListView.setAdapter(mAdapter);
        } else {
            if(getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                if(mAdapter == null) {
                    EventBus.getDefault().post(new InitEvent());
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
        } else if(event instanceof InitEvent) {
            Log.d(TAG, "InitEvent : ArtistFragment");
            mAdapter = new ArtistAdapter(MusicInfoLoadUtil.getArtistInfo(getActivity()), getActivity(), true);
            mListView.setAdapter(mAdapter);
        }

    }



}
