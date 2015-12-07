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

package com.massivcode.androidmusicplayer.activities;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.fragments.AddPlaylistFragment;
import com.massivcode.androidmusicplayer.utils.MusicInfoLoadUtil;
import com.suwonsmartapp.abl.AsyncBitmapLoader;

import java.util.ArrayList;

public class AddPlaylistActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, View.OnFocusChangeListener, AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener, View.OnClickListener {

    private static final String TAG = AddPlaylistActivity.class.getSimpleName();
    private ListView mAddPlaylistListView;
    private TextView mNotifyTextView;
    private FloatingActionButton mAddPlaylistFab;

    private SearchView mSearchView;
    private SearchAdapter mSearchAdapter;

    private ArrayList<Long> mUserDefinitionPlaylist;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_playlist);
        setTitle(getString(R.string.add_playlist));

        mUserDefinitionPlaylist = new ArrayList<>();

        mAddPlaylistListView = (ListView) findViewById(R.id.add_playlist_lv);
        mAddPlaylistListView.setOnItemSelectedListener(this);
        mAddPlaylistListView.setOnItemClickListener(this);
        mNotifyTextView = (TextView) findViewById(R.id.add_playlist_tv);
        mAddPlaylistFab = (FloatingActionButton)findViewById(R.id.add_playlist_fab);
        mAddPlaylistFab.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_add_playlist, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) menuItem.getActionView();
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnQueryTextFocusChangeListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_songs:
                Log.d(TAG, "노래 눌림");
                Cursor cursor = getApplicationContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MusicInfoLoadUtil.projection, MediaStore.Audio.Media.ARTIST + " != ? ", new String[]{MediaStore.UNKNOWN_STRING}, null);
                mSearchAdapter = new SearchAdapter(getApplicationContext(), cursor, true);
                mAddPlaylistListView.setAdapter(mSearchAdapter);
                mNotifyTextView.setVisibility(View.GONE);
                mUserDefinitionPlaylist.clear();
                mAddPlaylistFab.setVisibility(View.GONE);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(AddPlaylistActivity.this, getString(R.string.keyword) + query, Toast.LENGTH_SHORT).show();
        Cursor result = MusicInfoLoadUtil.search(getApplicationContext(), query);
        mUserDefinitionPlaylist.clear();
        mAddPlaylistFab.setVisibility(View.GONE);
        if (result == null || result.getCount() == 0) {
            mNotifyTextView.setVisibility(View.VISIBLE);
            mNotifyTextView.setText(R.string.notify_no_search_result);
            if (result != null) {
                result.close();
            }
            Toast.makeText(AddPlaylistActivity.this, R.string.notify_no_search_result, Toast.LENGTH_SHORT).show();
        } else {
            mNotifyTextView.setVisibility(View.GONE);
            mSearchAdapter = new SearchAdapter(getApplicationContext(), result, true);
            mAddPlaylistListView.setAdapter(mSearchAdapter);
            mSearchAdapter.notifyDataSetChanged();

        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // 이미 리스트에 존재할 경우 제거
        if(mUserDefinitionPlaylist.contains(id)) {
            mUserDefinitionPlaylist.remove(id);
        }
        // 리스트에 존재하지 않을 경우 추가
        else {
            mUserDefinitionPlaylist.add(id);
        }

        mSearchAdapter.notifyDataSetChanged();

        if(mUserDefinitionPlaylist.size() == 0) {
            mAddPlaylistFab.setVisibility(View.GONE);
        } else {
            mAddPlaylistFab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        AddPlaylistFragment fragment = new AddPlaylistFragment();
        Bundle args = new Bundle();
        args.putSerializable("playlist", mUserDefinitionPlaylist);
        fragment.setArguments(args);
        fragment.show(getSupportFragmentManager(), "AddPlaylistFragment");
    }


    private class SearchAdapter extends CursorAdapter implements AsyncBitmapLoader.BitmapLoadListener {

        private LayoutInflater mInflater;
        private Context mContext;

        private AsyncBitmapLoader mAsyncBitmapLoader;


        public SearchAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mAsyncBitmapLoader = new AsyncBitmapLoader(context);
            mAsyncBitmapLoader.setBitmapLoadListener(this);

        }

        @Override
        public View newView(Context context, final Cursor cursor, ViewGroup parent) {
            final ViewHolder viewHolder = new ViewHolder();
            View view = mInflater.inflate(R.layout.item_search, parent, false);

            viewHolder.searchArtistTextView = (TextView)view.findViewById(R.id.item_search_artist_tv);
            viewHolder.searchTitleTextView = (TextView)view.findViewById(R.id.item_search_title_tv);
            viewHolder.searchAlbumImageView = (ImageView)view.findViewById(R.id.item_search_album_iv);
            viewHolder.searchBorder = (LinearLayout)view.findViewById(R.id.item_search_border);

            view.setTag(viewHolder);

            return view;
        }

        @Override
        public void bindView(View view, Context context, final Cursor cursor) {
            final ViewHolder viewHolder = (ViewHolder)view.getTag();
            viewHolder.searchArtistTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
            viewHolder.searchTitleTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
            mAsyncBitmapLoader.loadBitmap(cursor.getPosition(), viewHolder.searchAlbumImageView);

            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
            if(mUserDefinitionPlaylist.contains(id)) {
                viewHolder.searchBorder.setBackgroundResource(R.drawable.image_border);
            } else {
                viewHolder.searchBorder.setBackgroundResource(R.color.white);
            }

        }

        @Override
        public Bitmap getBitmap(int position) {
            // id 가져오기
            // DB의 _id == id
            long id = getItemId(position);

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(mContext, Uri.parse("content://media/external/audio/media/" + id));

            byte[] albumArt =  retriever.getEmbeddedPicture();

            // Bitmap 샘플링
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
            LinearLayout searchBorder;
            ImageView searchAlbumImageView;
            TextView searchArtistTextView;
            TextView searchTitleTextView;
        }
    }
}
