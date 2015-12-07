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
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.database.MyPlaylistFacade;
import com.massivcode.androidmusicplayer.events.Event;
import com.massivcode.androidmusicplayer.events.ReloadPlaylist;
import com.massivcode.androidmusicplayer.models.MusicInfo;
import com.massivcode.androidmusicplayer.utils.MusicInfoLoadUtil;
import com.suwonsmartapp.abl.AsyncBitmapLoader;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;


public class AddPlaylistFragment extends DialogFragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = AddPlaylistFragment.class.getSimpleName();
    private Button mCancelButton, mSaveButton;
    private ListView mConfirmListView;
    private TextInputLayout mTil;
    private EditText mPlaylistNameEditText;


    private ArrayList<MusicInfo> mMusicInfoList;

    private MyPlaylistFacade mFacade;

    private ConfirmAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            ArrayList<Long> list = (ArrayList<Long>) getArguments().getSerializable("playlist");
            mMusicInfoList = MusicInfoLoadUtil.getMusicInfoByIds(getActivity(), list);
            mAdapter = new ConfirmAdapter(getActivity(), mMusicInfoList);
            mFacade = new MyPlaylistFacade(getActivity());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_playlist, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.WHITE));

        setCancelable(false);

        initViews(view);

        return view;
    }

    private void initViews(View view) {
        mCancelButton = (Button) view.findViewById(R.id.add_playlist_cancel_btn);
        mSaveButton = (Button) view.findViewById(R.id.add_playlist_save_btn);
        mConfirmListView = (ListView)view.findViewById(R.id.add_playlist_confirm_lv);
        mTil = (TextInputLayout)view.findViewById(R.id.add_playlist_til);
        mPlaylistNameEditText = (EditText)view.findViewById(R.id.add_playlist_name_et);

        mConfirmListView.setOnItemClickListener(this);
        mCancelButton.setOnClickListener(this);
        mSaveButton.setOnClickListener(this);

        mConfirmListView.setAdapter(mAdapter);
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

    public void onEvent(Event event) {

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.add_playlist_cancel_btn:
                dismiss();
                break;
            case R.id.add_playlist_save_btn:
                if(TextUtils.isEmpty(mPlaylistNameEditText.getText().toString())) {
                    mTil.setError(getActivity().getString(R.string.error_no_playlist_name));
                    mTil.setHint("");
                } else {
                    String playListName = mPlaylistNameEditText.getText().toString();
                    if(mFacade.isAlreadyExist(playListName)) {
                        mTil.setError(getActivity().getString(R.string.error_already_exists));
                        mTil.setHint("");
                    } else {
                        ArrayList<Long> idList = MusicInfoLoadUtil.getIdListByMusicInfoList(mMusicInfoList);
                        mFacade.addUserPlaylist(playListName, idList);
                        EventBus.getDefault().post(new ReloadPlaylist());
                        dismiss();
                        getActivity().finish();
                    }
                }
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MusicInfo selectedInfo = (MusicInfo) parent.getAdapter().getItem(position);
        Log.d(TAG, "선택한 음원의 정보 : " + selectedInfo.toString());
        mMusicInfoList.remove(position);
        mAdapter.swapData(mMusicInfoList);

        if(mMusicInfoList.size() == 0) {
            dismiss();
        }
    }

    private class ConfirmAdapter extends BaseAdapter implements AsyncBitmapLoader.BitmapLoadListener {

        private Context mContext;
        private ArrayList<MusicInfo> mList;
        private AsyncBitmapLoader mAsyncBitmapLoader;

        public ConfirmAdapter(Context context, ArrayList<MusicInfo> list) {
            mContext = context;
            mList = list;
            mAsyncBitmapLoader = new AsyncBitmapLoader(context);
            mAsyncBitmapLoader.setBitmapLoadListener(this);
        }

        public void swapData(ArrayList<MusicInfo> newList) {
            mList = newList;
            notifyDataSetChanged();
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_confirm_playlist, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.itemPlaylistAlbumImageView = (ImageView)convertView.findViewById(R.id.item_playlist_album_iv);
                viewHolder.itemPlaylistTitleTextView = (TextView)convertView.findViewById(R.id.item_playlist_title_tv);
                viewHolder.itemPlaylistArtistTextView = (TextView)convertView.findViewById(R.id.item_playlist_artist_tv);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder)convertView.getTag();
            }

            MusicInfo musicInfo = (MusicInfo) getItem(position);
            viewHolder.itemPlaylistArtistTextView.setText(musicInfo.getArtist());
            viewHolder.itemPlaylistTitleTextView.setText(musicInfo.getTitle());
            mAsyncBitmapLoader.loadBitmap(position, viewHolder.itemPlaylistAlbumImageView);

            return convertView;
        }

        @Override
        public Bitmap getBitmap(int position) {
            MusicInfo musicInfo = (MusicInfo) getItem(position);

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(mContext, musicInfo.getUri());

            byte[] albumArt = retriever.getEmbeddedPicture();


            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4; // 2의 배수

            Bitmap bitmap;
            if (null != albumArt) {
                bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length, options);
            } else {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_no_image);
            }

            // id 로부터 bitmap 생성
            return bitmap;
        }

        private class ViewHolder {
            ImageView itemPlaylistAlbumImageView;
            TextView itemPlaylistTitleTextView;
            TextView itemPlaylistArtistTextView;

        }
    }
}
