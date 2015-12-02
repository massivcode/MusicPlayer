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
import android.util.SparseArray;
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

/**
 * Created by massivCode on 2015-12-01.
 */
public class AddPlaylistActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, View.OnFocusChangeListener, AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener, View.OnClickListener {

    private static final String TAG = AddPlaylistActivity.class.getSimpleName();
    private ListView mAddPlaylistListView;
    private TextView mNotifyTextView;
    private FloatingActionButton mAddPlaylistFab;

    private SearchView mSearchView;
    private SearchAdapter mSearchAdapter;

    private ArrayList<Long> mUserDefinitionPlaylist;


    private SparseArray<Boolean> mCheckedArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_playlist);
        setTitle("재생목록 추가");

        mUserDefinitionPlaylist = new ArrayList<>();

        mAddPlaylistListView = (ListView) findViewById(R.id.add_playlist_lv);
        mAddPlaylistListView.setOnItemSelectedListener(this);
        mAddPlaylistListView.setOnItemClickListener(this);
        mNotifyTextView = (TextView) findViewById(R.id.add_playlist_tv);
        mAddPlaylistFab = (FloatingActionButton)findViewById(R.id.add_playlist_fab);
        mAddPlaylistFab.setOnClickListener(this);

        mCheckedArray = new SparseArray<>();
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
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            if (mMusicService != null && mMusicService.getCurrentPlaylist() != null) {
//                CurrentPlaylistFragment dialogFragment = new CurrentPlaylistFragment();
//                Bundle bundle = new Bundle();
//                Log.d(TAG, "getCurrentPlaylist.size : " + mMusicService.getCurrentPlaylist().size());
//                bundle.putSerializable("data", mMusicService.getCurrentPlaylist());
//                bundle.putSerializable("map", mMusicService.getAllMusicData());
//                dialogFragment.setArguments(bundle);
//                dialogFragment.show(getSupportFragmentManager(), "ManageDbFragment");
//            } else {
//                Toast.makeText(MainActivity.this, "재생 중인 노래가 없습니다.", Toast.LENGTH_SHORT).show();
//            }
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(AddPlaylistActivity.this, "검색어 : " + query, Toast.LENGTH_SHORT).show();
        Cursor result = MusicInfoLoadUtil.search(getApplicationContext(), query);

        if (result == null || result.getCount() == 0) {
            mNotifyTextView.setVisibility(View.VISIBLE);
            mNotifyTextView.setText("검색 결과가 없습니다!");
            result.close();
            Toast.makeText(AddPlaylistActivity.this, "검색 결과가 없습니다!", Toast.LENGTH_SHORT).show();
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
            Log.d(TAG, id + " 가 제거되었습니다.");
            mUserDefinitionPlaylist.remove(id);
            Log.d(TAG, "현재 리스트의 개수 : " + mUserDefinitionPlaylist.size());
        }
        // 리스트에 존재하지 않을 경우 추가
        else {
            mUserDefinitionPlaylist.add(id);
            Log.d(TAG, id + " 가 추가되었습니다.");
            Log.d(TAG, "현재 리스트의 개수 : " + mUserDefinitionPlaylist.size());
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
        Toast.makeText(AddPlaylistActivity.this, "FAB 클릭됨", Toast.LENGTH_SHORT).show();
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
            LinearLayout searchBorder;
            ImageView searchAlbumImageView;
            TextView searchArtistTextView;
            TextView searchTitleTextView;
        }
    }
}
