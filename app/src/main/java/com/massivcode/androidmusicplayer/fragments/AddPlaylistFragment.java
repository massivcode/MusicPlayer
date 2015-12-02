package com.massivcode.androidmusicplayer.fragments;

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
import com.massivcode.androidmusicplayer.models.MusicInfo;
import com.massivcode.androidmusicplayer.utils.MusicInfoLoadUtil;
import com.suwonsmartapp.abl.AsyncBitmapLoader;

import java.util.ArrayList;

/**
 * Created by Ray Choe on 2015-12-02.
 */
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
            Log.d(TAG, "받아온 리스트 : " + list.size());
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
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.add_playlist_cancel_btn:
                dismiss();
                break;
            case R.id.add_playlist_save_btn:
                if(TextUtils.isEmpty(mPlaylistNameEditText.getText().toString())) {
                    mTil.setError("재생목록 이름을 입력해주세요!");
                    mTil.setHint("");
                } else {
                    if(mFacade.isAlreadyExist(mPlaylistNameEditText.getText().toString())) {
                        mTil.setError("이미 존재하는 재생목록 입니다.");
                        mTil.setHint("");
                    } else {
                        ArrayList<Long> idList = MusicInfoLoadUtil.getIdListByMusicInfoList(mMusicInfoList);
                        mFacade.addUserPlaylist(mPlaylistNameEditText.getText().toString(), idList);
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

            Bitmap bitmap = null;
            if (null != albumArt) {
                bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length, options);
            } else {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
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
