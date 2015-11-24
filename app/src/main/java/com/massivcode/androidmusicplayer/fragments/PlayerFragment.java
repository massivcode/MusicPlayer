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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.interfaces.Event;
import com.massivcode.androidmusicplayer.interfaces.MusicEvent;
import com.massivcode.androidmusicplayer.model.MusicInfo;
import com.massivcode.androidmusicplayer.util.MusicInfoUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by Ray Choe on 2015-11-24.
 */
public class PlayerFragment extends Fragment {

    private static final String TAG = PlayerFragment.class.getSimpleName();

    private ImageView mPlayerAlbumArtImageView;
    private ImageButton mPlayerRepeatImageButton;
    private ImageButton mPlayerFavoriteImageButton;
    private ImageButton mPlayerShuffleImageButton;
    private SeekBar mPlayerSeekBar;
    private TextView mPlayerCurrentTimeTextView;
    private TextView mPlayerDurationTextView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        initView(view);

        return view;
    }

    private void initView(View v) {
        mPlayerAlbumArtImageView = (ImageView)v.findViewById(R.id.player_albumArt_iv);
        mPlayerRepeatImageButton = (ImageButton)v.findViewById(R.id.player_repeat_ib);
        mPlayerFavoriteImageButton = (ImageButton)v.findViewById(R.id.player_favorite_ib);
        mPlayerShuffleImageButton = (ImageButton)v.findViewById(R.id.player_shuffle_ib);
        mPlayerSeekBar = (SeekBar)v.findViewById(R.id.player_seekbar);
        mPlayerCurrentTimeTextView = (TextView)v.findViewById(R.id.player_current_time_tv);
        mPlayerDurationTextView = (TextView)v.findViewById(R.id.player_duration_tv);

        mPlayerRepeatImageButton.setOnClickListener((View.OnClickListener)getActivity());
        mPlayerFavoriteImageButton.setOnClickListener((View.OnClickListener)getActivity());
        mPlayerShuffleImageButton.setOnClickListener((View.OnClickListener)getActivity());
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
        Toast.makeText(getActivity(), "플레이어 : 이벤트 수신함", Toast.LENGTH_SHORT).show();
        MusicEvent musicEvent = (MusicEvent)event;
        MusicInfo musicInfo = musicEvent.getMusicInfo();
        Log.d(TAG, "event received");
        Log.d(TAG, "duration : " + musicInfo.getDuration());

        mPlayerDurationTextView.setText(musicInfo.getDuration());
        mPlayerAlbumArtImageView.setImageBitmap(MusicInfoUtil.getBitmap(getActivity(), musicInfo.getUri(), 4));

    }
}
