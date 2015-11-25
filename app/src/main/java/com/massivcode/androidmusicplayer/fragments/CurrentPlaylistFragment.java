package com.massivcode.androidmusicplayer.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.interfaces.Event;
import com.massivcode.androidmusicplayer.model.MusicInfo;
import com.massivcode.androidmusicplayer.util.MusicInfoUtil;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by Ray Choe on 2015-11-25.
 */
public class CurrentPlaylistFragment extends DialogFragment implements View.OnClickListener {

    private static final String TAG = CurrentPlaylistFragment.class.getSimpleName();
    private ListView mCurrentPlaylistListView;
    private Button mCurrentPlaylistCloseButton;

    private ArrayList<Long> mPlaylist;
    private ArrayList<MusicInfo> mMusicDataList;

    private CurrentPlaylistAdapter mAdapter;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPlaylist = (ArrayList<Long>) getArguments().getSerializable("data");

        new Thread(new Runnable() {
            @Override
            public void run() {
                mMusicDataList = MusicInfoUtil.getMusicInfoList(getActivity(), mPlaylist);
                mAdapter = new CurrentPlaylistAdapter(getActivity(), mMusicDataList);
                mCurrentPlaylistListView.setAdapter(mAdapter);
            }
        }).start();





    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_current_playlist, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.WHITE));

        setCancelable(false);

        initView(view);


        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "CurrentPlaylistFragment is attached");
        // EventBus 등록이 되어서 모든 이벤트를 수신 가능
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "CurrentPlaylistFragment is detached");
        // 해제 꼭 해주세요
        EventBus.getDefault().unregister(this);
    }

    // EventBus 용 이벤트 수신
    public void onEvent(Event event) {

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

        private Context mContext;
        private ArrayList<MusicInfo> mList;

        public CurrentPlaylistAdapter(Context context, ArrayList<MusicInfo> list) {
            mContext = context;
            mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if(convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_current_playlist, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.mCurrentPlaylistAlbumArtImageView = (ImageView)convertView.findViewById(R.id.item_current_album_iv);
                viewHolder.mCurrentPlaylistArtistTextView = (TextView)convertView.findViewById(R.id.item_current_artist_tv);
                viewHolder.mCurrentPlaylistTitleTextView = (TextView)convertView.findViewById(R.id.item_current_title_tv);

                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            MusicInfo musicInfo = (MusicInfo) getItem(position);
            viewHolder.mCurrentPlaylistAlbumArtImageView.setImageBitmap(MusicInfoUtil.getBitmap(mContext, musicInfo.getUri(), 4));
            viewHolder.mCurrentPlaylistArtistTextView.setText(musicInfo.getArtist());
            viewHolder.mCurrentPlaylistTitleTextView.setText(musicInfo.getTitle());

            return convertView;
        }

        private class ViewHolder {
            ImageView mCurrentPlaylistAlbumArtImageView;
            TextView mCurrentPlaylistTitleTextView;
            TextView mCurrentPlaylistArtistTextView;
        }
    }


}
