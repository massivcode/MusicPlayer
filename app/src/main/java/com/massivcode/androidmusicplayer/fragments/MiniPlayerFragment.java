package com.massivcode.androidmusicplayer.fragments;

import android.app.Activity;
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
import android.widget.TextView;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.interfaces.Event;
import com.massivcode.androidmusicplayer.interfaces.MusicEvent;
import com.massivcode.androidmusicplayer.interfaces.Playback;
import com.massivcode.androidmusicplayer.model.MusicInfo;
import com.massivcode.androidmusicplayer.util.MusicInfoUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by Ray Choe on 2015-11-23.
 */
public class MiniPlayerFragment extends Fragment {

    private static final String TAG = MiniPlayerFragment.class.getSimpleName();
    private ImageView mPlayerMiniAlbumArtImageView;
    private TextView mPlayerTitleTextView;
    private TextView mPlayerArtistTextView;
    private ImageButton mPlayerPreviousImageButton;
    private ImageButton mPlayerPlayImageButton;
    private ImageButton mPlayerNextImageButton;

    private Handler mHandler = new Handler();

    public MiniPlayerFragment() {
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
        mPlayerNextImageButton.setOnClickListener((View.OnClickListener)getActivity());

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // EventBus 등록이 되어서 모든 이벤트를 수신 가능
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // 해제 꼭 해주세요
        EventBus.getDefault().unregister(this);
    }

    // EventBus 용 이벤트 수신
    public void onEvent(Event event) {
        // 미니 플레이어 : 앨범아트, 제목, 아티스트 -> 최초 업데이트

        if(event instanceof MusicEvent) {
            MusicEvent musicEvent = (MusicEvent)event;
            MusicInfo musicInfo = musicEvent.getMusicInfo();

            mPlayerMiniAlbumArtImageView.setImageBitmap(MusicInfoUtil.getBitmap(getActivity(), musicInfo.getUri(), 4));
            mPlayerArtistTextView.setText(musicInfo.getArtist());
            mPlayerTitleTextView.setText(musicInfo.getTitle());
        } else if(event instanceof Playback) {
            Log.d(TAG, "Playback is coming");
            final Playback playback = (Playback) event;

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(playback.isPlaying()) {
                        Log.d(TAG, "playback.isPlaying() : true");
                        mPlayerPlayImageButton.setSelected(true);
                    } else {
                        Log.d(TAG, "playback.isPlaying() : false");
                        mPlayerPlayImageButton.setSelected(false);
                    }
                }
            });

        }


    }

}
