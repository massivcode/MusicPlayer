package com.massivcode.androidmusicplayer.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.adapters.PlaylistAdapter;
import com.massivcode.androidmusicplayer.database.MyPlaylistContract;
import com.massivcode.androidmusicplayer.database.MyPlaylistFacade;
import com.massivcode.androidmusicplayer.interfaces.Event;
import com.massivcode.androidmusicplayer.interfaces.MusicEvent;
import com.massivcode.androidmusicplayer.interfaces.PlayBack;
import com.massivcode.androidmusicplayer.interfaces.ReloadPlaylist;

import de.greenrobot.event.EventBus;

/**
 * Created by Ray Choe on 2015-11-23.
 */
public class PlaylistFragment extends Fragment implements AdapterView.OnItemLongClickListener {

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
        mListView.setOnItemLongClickListener(this);
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
            mNotifyNoDataTextView.setVisibility(View.GONE);
            mListView.setAdapter(mAdapter);
        }

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        int itemType = mListView.getPackedPositionType(id);
        if(itemType == 0) {
            Log.d(TAG, "그룹뷰가 롱클릭되었습니다.");
            Cursor cursor = mAdapter.getGroup(position);
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST));
            showConfirmDialog(name);
                Log.d(TAG, "이름 : " + name);
        } else {
            Log.d(TAG, "자식뷰가 롱클릭되었습니다.");
            Log.d(TAG, "포지션 : " + position);
        }
        return true;
    }

    private void showConfirmDialog(final String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("재생목록 삭제").setMessage(name + "을 삭제하시겠습니까?").setPositiveButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "삭제 눌림");
                mFacade.deleteUserPlaylist(name);
                mAdapter.changeCursor(mFacade.getAllUserPlaylist());

                if(mFacade.isAlreadyExist()) {
                    Log.d(TAG, "데이터가 있습니다.");
                    mNotifyNoDataTextView.setVisibility(View.GONE);
                } else {
                    Log.d(TAG, "데이터가 없습니다.");
                    mNotifyNoDataTextView.setVisibility(View.VISIBLE);
                }
            }
        });
        builder.setNegativeButton("취소", null);
        builder.show();
    }
}
