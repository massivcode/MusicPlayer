package com.massivcode.androidmusicplayer.fragments;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
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
import com.massivcode.androidmusicplayer.interfaces.Event;
import com.massivcode.androidmusicplayer.interfaces.MusicEvent;
import com.massivcode.androidmusicplayer.interfaces.Playback;
import com.massivcode.androidmusicplayer.model.MusicInfo;
import com.massivcode.androidmusicplayer.utils.MusicInfoUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by Ray Choe on 2015-11-24.
 */
public class PlayerFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = PlayerFragment.class.getSimpleName();

    private ImageView mPlayerAlbumArtImageView;
    private ImageButton mPlayerRepeatImageButton;
    private ImageButton mPlayerFavoriteImageButton;
    private ImageButton mPlayerShuffleImageButton;
    private SeekBar mPlayerSeekBar;
    private TextView mPlayerCurrentTimeTextView;
    private TextView mPlayerDurationTextView;

    private Handler mHandler = new Handler();

    private MediaPlayer mMediaPlayer;
    private Playback mPlayback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "PlayerFragment.onCreate()");
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

        if (event instanceof Playback) {
            // 실시간 재생정보 클래스 Playback
            mPlayback = (Playback) event;

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //TODO 계속 값을 갱신해야 하는 부분 : CurrentTextView, setProgress
                    mPlayerCurrentTimeTextView.setText(MusicInfoUtil.getTime(String.valueOf(mPlayback.getCurrentTime())));
                    mPlayerSeekBar.setProgress(mPlayback.getCurrentTime());
                }
            });

        } else if (event instanceof MusicEvent) {
            // 현재 곡 정보 클래스 MusicEvent
            mMediaPlayer = ((MusicEvent) event).getMediaPlayer();
            MusicInfo musicInfo = ((MusicEvent) event).getMusicInfo();

            mPlayerDurationTextView.setText(MusicInfoUtil.getTime(String.valueOf(musicInfo.getDuration())));
            mPlayerAlbumArtImageView.setImageBitmap(MusicInfoUtil.getBitmap(getActivity(), musicInfo.getUri(), 1));
            mPlayerSeekBar.setMax(musicInfo.getDuration());

        }


    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mPlayerCurrentTimeTextView.setText(MusicInfoUtil.getTime(String.valueOf(progress)));
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
