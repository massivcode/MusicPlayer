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

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.events.Event;
import com.massivcode.androidmusicplayer.events.MusicEvent;
import com.massivcode.androidmusicplayer.events.PlayBack;
import com.massivcode.androidmusicplayer.events.Restore;
import com.massivcode.androidmusicplayer.events.SaveState;
import com.massivcode.androidmusicplayer.models.MusicInfo;
import com.massivcode.androidmusicplayer.utils.MusicInfoLoadUtil;

import de.greenrobot.event.EventBus;


public class MiniPlayerFragment extends Fragment {

    private static final String TAG = MiniPlayerFragment.class.getSimpleName();
    private ImageView mPlayerMiniAlbumArtImageView;
    private TextView mPlayerTitleTextView;
    private TextView mPlayerArtistTextView;
    private ImageButton mPlayerPreviousImageButton;
    private ImageButton mPlayerPlayImageButton;
    private ImageButton mPlayerNextImageButton;

    private SaveState mSaveState;
    private PlayBack mPlayBack;

    public MiniPlayerFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_miniplayer, container, false);

        initView(view);

        return view;
    }

    private void initView(View v) {
        mPlayerMiniAlbumArtImageView = (ImageView)v.findViewById(R.id.player_miniAlbumArt_iv);
        mPlayerTitleTextView = (TextView)v.findViewById(R.id.player_title_tv);
        mPlayerArtistTextView = (TextView)v.findViewById(R.id.player_artist_tv);
        mPlayerPreviousImageButton = (ImageButton)v.findViewById(R.id.player_previous_ib);
        mPlayerPlayImageButton = (ImageButton)v.findViewById(R.id.player_play_ib);
        mPlayerNextImageButton = (ImageButton)v.findViewById(R.id.player_next_ib);

        mPlayerMiniAlbumArtImageView.setOnClickListener((View.OnClickListener)getActivity());
        mPlayerTitleTextView.setOnClickListener((View.OnClickListener)getActivity());
        mPlayerArtistTextView.setOnClickListener((View.OnClickListener)getActivity());
        mPlayerPreviousImageButton.setOnClickListener((View.OnClickListener)getActivity());
        mPlayerPlayImageButton.setOnClickListener((View.OnClickListener)getActivity());
        mPlayerNextImageButton.setOnClickListener((View.OnClickListener) getActivity());

        refreshViewWhenActivityForcedTerminated();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Log.d(TAG, "MiniPlayerFragment onAttach");
        // EventBus 등록이 되어서 모든 이벤트를 수신 가능
        EventBus.getDefault().register(this);
        EventBus.getDefault().post(new Restore());
    }

    @Override
    public void onDetach() {
        super.onDetach();

        Log.d(TAG, "MiniPlayerFragment onDetach");
        // 해제 꼭 해주세요
        EventBus.getDefault().unregister(this);
    }

    // EventBus 용 이벤트 수신
    public void onEvent(Event event) {
        // 미니 플레이어 : 앨범아트, 제목, 아티스트 -> 최초 업데이트

        if(event instanceof MusicEvent) {
            MusicEvent musicEvent = (MusicEvent)event;
            Log.d(TAG, "MiniPlayerFragment : get Music Event");
            if(musicEvent.getMusicInfo() != null) {
                MusicInfo musicInfo = musicEvent.getMusicInfo();
                mPlayerMiniAlbumArtImageView.setImageBitmap(MusicInfoLoadUtil.getBitmap(getActivity(), musicInfo.getUri(), 4));
                mPlayerArtistTextView.setText(musicInfo.getArtist());
                mPlayerTitleTextView.setText(musicInfo.getTitle());

            }

        } else if(event instanceof PlayBack) {
//            Log.d(TAG, "PlayBack is coming");
            final PlayBack playback = (PlayBack) event;

            if (mPlayBack == null || mPlayBack.isPlaying() != playback.isPlaying()) {
                mPlayBack = playback;
                mPlayerPlayImageButton.setSelected(playback.isPlaying());
            }

        } else if(event instanceof SaveState) {
            if(((SaveState) event).getMusicInfo() != null) {
                Log.d(TAG, "MiniPlayerFragment : get Save State");
                mSaveState = (SaveState) event;
            }
        }


    }

    private void refreshViewWhenActivityForcedTerminated() {
        if(mSaveState != null) {
            MusicInfo musicInfo = mSaveState.getMusicInfo();
            mPlayerTitleTextView.setText(musicInfo.getTitle());
            mPlayerArtistTextView.setText(musicInfo.getArtist());
            mPlayerMiniAlbumArtImageView.setImageBitmap(MusicInfoLoadUtil.getBitmap(getActivity(), musicInfo.getUri(), 4));
        }

        if(getActivity().getIntent() != null) {
            MusicInfo musicInfo = getActivity().getIntent().getParcelableExtra("restore");
            if(musicInfo != null) {
                mPlayerTitleTextView.setText(musicInfo.getTitle());
                mPlayerArtistTextView.setText(musicInfo.getArtist());
                mPlayerMiniAlbumArtImageView.setImageBitmap(MusicInfoLoadUtil.getBitmap(getActivity(), musicInfo.getUri(), 4));
            }
        }
    }

}
