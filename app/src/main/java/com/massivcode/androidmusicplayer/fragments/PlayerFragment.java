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
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.database.MyPlaylistFacade;
import com.massivcode.androidmusicplayer.events.Event;
import com.massivcode.androidmusicplayer.events.MusicEvent;
import com.massivcode.androidmusicplayer.events.PlayBack;
import com.massivcode.androidmusicplayer.events.ReloadPlaylist;
import com.massivcode.androidmusicplayer.events.RequestMusicEvent;
import com.massivcode.androidmusicplayer.events.Restore;
import com.massivcode.androidmusicplayer.events.SaveState;
import com.massivcode.androidmusicplayer.models.MusicInfo;
import com.massivcode.androidmusicplayer.utils.DataBackupUtil;
import com.massivcode.androidmusicplayer.utils.MusicInfoLoadUtil;

import de.greenrobot.event.EventBus;


public class PlayerFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = PlayerFragment.class.getSimpleName();

    private ImageView mPlayerAlbumArtImageView;
    private ImageButton mPlayerRepeatImageButton;
    private ImageButton mPlayerFavoriteImageButton;
    private ImageButton mPlayerShuffleImageButton;
    private SeekBar mPlayerSeekBar;
    private TextView mPlayerCurrentTimeTextView;
    private TextView mPlayerDurationTextView;

    private MediaPlayer mMediaPlayer;
    private SaveState mSaveState;
    private MyPlaylistFacade mFacade;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFacade = new MyPlaylistFacade(getActivity());

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        initView(view);

        return view;
    }

    private void initView(View v) {
        mPlayerAlbumArtImageView = (ImageView) v.findViewById(R.id.player_albumArt_iv);
        mPlayerRepeatImageButton = (ImageButton) v.findViewById(R.id.player_repeat_ib);
        mPlayerFavoriteImageButton = (ImageButton) v.findViewById(R.id.player_favorite_ib);
        mPlayerShuffleImageButton = (ImageButton) v.findViewById(R.id.player_shuffle_ib);
        mPlayerSeekBar = (SeekBar) v.findViewById(R.id.player_seekbar);
        mPlayerCurrentTimeTextView = (TextView) v.findViewById(R.id.player_current_time_tv);
        mPlayerDurationTextView = (TextView) v.findViewById(R.id.player_duration_tv);

        mPlayerRepeatImageButton.setOnClickListener((View.OnClickListener) getActivity());
        mPlayerFavoriteImageButton.setOnClickListener((View.OnClickListener) getActivity());
        mPlayerShuffleImageButton.setOnClickListener((View.OnClickListener) getActivity());

        mPlayerSeekBar.setOnSeekBarChangeListener(this);
        toggleControl();
        EventBus.getDefault().post(new RequestMusicEvent());
        refreshViewWhenActivityForcedTerminated();
    }

    private void refreshViewWhenActivityForcedTerminated() {
        if(mSaveState != null) {
            MusicInfo musicInfo = mSaveState.getMusicInfo();
            mPlayerSeekBar.setMax(musicInfo.getDuration());
            mPlayerDurationTextView.setText(MusicInfoLoadUtil.getTime(String.valueOf(musicInfo.getDuration())));
            mPlayerAlbumArtImageView.setImageBitmap(MusicInfoLoadUtil.getBitmap(getActivity(), musicInfo.getUri(), 1));
            mPlayerCurrentTimeTextView.setText(MusicInfoLoadUtil.getTime(String.valueOf(mSaveState.getCurrentPlayTime())));
            mPlayerSeekBar.setProgress(mSaveState.getCurrentPlayTime());
            toggleFavorite(musicInfo.get_id());
        }

        if(getActivity().getIntent() != null) {
            MusicInfo musicInfo = getActivity().getIntent().getParcelableExtra("restore");
            if(musicInfo != null) {
                mPlayerSeekBar.setMax(musicInfo.getDuration());
                mPlayerDurationTextView.setText(MusicInfoLoadUtil.getTime(String.valueOf(musicInfo.getDuration())));
                mPlayerAlbumArtImageView.setImageBitmap(MusicInfoLoadUtil.getBitmap(getActivity(), musicInfo.getUri(), 1));
                toggleFavorite(musicInfo.get_id());
            }
        }
    }

    private void toggleControl() {
        boolean isShuffle = DataBackupUtil.getInstance(getActivity()).loadIsShuffle();
        if(isShuffle) {
            mPlayerShuffleImageButton.setSelected(true);
        } else {
            mPlayerShuffleImageButton.setSelected(false);
        }
        boolean isRepeat = DataBackupUtil.getInstance(getActivity()).loadIsRepeat();
        if(isRepeat) {
            mPlayerRepeatImageButton.setSelected(true);
        } else {
            mPlayerRepeatImageButton.setSelected(false);
        }
    }

    private void toggleFavorite(long id) {
        boolean isFavorited = mFacade.isFavoritted(id);
        Log.d(TAG, "toggleFavorite @ Player : " + isFavorited);
        mPlayerFavoriteImageButton.setSelected(isFavorited);
        EventBus.getDefault().post(new ReloadPlaylist());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Log.d(TAG, "PlayerFragment.onAttach");

        // EventBus 등록이 되어서 모든 이벤트를 수신 가능
        EventBus.getDefault().register(this);
        EventBus.getDefault().post(new Restore());

    }

    @Override
    public void onDetach() {
        super.onDetach();

        Log.d(TAG, "PlayerFragment.onDetach");

        // 해제 꼭 해주세요
        EventBus.getDefault().unregister(this);
    }

    // EventBus 용 이벤트 수신
    public void onEvent(Event event) {

        if (event instanceof PlayBack) {
            // 실시간 재생정보 클래스 PlayBack
            PlayBack playBack = (PlayBack) event;

            mPlayerCurrentTimeTextView.setText(MusicInfoLoadUtil.getTime(String.valueOf(playBack.getCurrentTime())));
            mPlayerSeekBar.setProgress(playBack.getCurrentTime());

        } else if (event instanceof MusicEvent) {
            // 현재 곡 정보 클래스 MusicEvent
            mMediaPlayer = ((MusicEvent) event).getMediaPlayer();
            if(((MusicEvent) event).getMusicInfo() != null) {
                MusicInfo musicInfo = ((MusicEvent) event).getMusicInfo();
                Log.d(TAG, "getDuration : " + musicInfo.getDuration());
                Log.d(TAG, "MiniPlayerFragment : get Music Event");
                if(mPlayerDurationTextView != null) {
                    mPlayerDurationTextView.setText(MusicInfoLoadUtil.getTime(String.valueOf(musicInfo.getDuration())));
                    mPlayerAlbumArtImageView.setImageBitmap(MusicInfoLoadUtil.getBitmap(getActivity(), musicInfo.getUri(), 1));
                    mPlayerSeekBar.setMax(musicInfo.getDuration());
                    toggleFavorite(musicInfo.get_id());
                }
            }

        } else if(event instanceof SaveState) {
            if(((SaveState) event).getMusicInfo() != null) {
                Log.d(TAG, "PlayerFragment : get Save State");
                mSaveState = (SaveState) event;
            }
        }


    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mPlayerCurrentTimeTextView.setText(MusicInfoLoadUtil.getTime(String.valueOf(progress)));
            mMediaPlayer.pause();
            mMediaPlayer.seekTo(progress);
            mMediaPlayer.start();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
