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

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.database.MyPlaylistContract;
import com.massivcode.androidmusicplayer.database.MyPlaylistFacade;
import com.massivcode.androidmusicplayer.fragments.CurrentPlaylistFragment;
import com.massivcode.androidmusicplayer.events.Event;
import com.massivcode.androidmusicplayer.events.FinishActivity;
import com.massivcode.androidmusicplayer.events.InitEvent;
import com.massivcode.androidmusicplayer.events.ReloadPlaylist;
import com.massivcode.androidmusicplayer.events.SaveState;
import com.massivcode.androidmusicplayer.managers.Manager;
import com.massivcode.androidmusicplayer.services.MusicService;
import com.massivcode.androidmusicplayer.utils.DataBackupUtil;
import com.massivcode.androidmusicplayer.utils.MusicInfoLoadUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, AdapterView.OnItemClickListener, ExpandableListView.OnChildClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String ACTION_NAME = "com.massivcode.androidmusicplayer.MainActivity";

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    private MyPlaylistFacade mFacade;


    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private NavigationAdapter mNavigationAdapter;

    private List<String> mMemuTitleList;

    private Intent mServiceIntent;

    private MusicService mMusicService;


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "serviceConnected");
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            mMusicService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private NavigationView mNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // EventBus 등록이 되어서 모든 이벤트를 수신 가능
        EventBus.getDefault().register(this);

        mFacade = new MyPlaylistFacade(getApplicationContext());


        mServiceIntent = new Intent(MainActivity.this, MusicService.class);
        bindService(mServiceIntent, mConnection, BIND_AUTO_CREATE);


        initViews();

        checkPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(mConnection);
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context


        // 해제 꼭 해주세요
        EventBus.getDefault().unregister(this);
    }

    // EventBus 용 이벤트 수신
    public void onEvent(Event event) {
        if (event instanceof FinishActivity) {
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMusicService != null) {
            if (mMusicService.getCurrentInfo() != null && mMusicService.getCurrentPlaylist() != null && mMusicService.getCurrentPosition() != -1) {
                EventBus.getDefault().post(new SaveState());
            }
        }
    }


    private void initViews() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mMemuTitleList = Arrays.asList(getResources().getStringArray(R.array.nav_menu_array));
        mNavigationAdapter = new NavigationAdapter(getSupportFragmentManager());

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.tab_player));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.tab_playlist));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.tab_artist));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.tab_songs));

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(mNavigationAdapter);
        // 뷰페이져에서 프래그먼트 4개까지 메모리에 올려놓는다.
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                toolbar.setTitle(mMemuTitleList.get(position));

                // 네비게이션 드로워도 변경
                mNavigationView.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if (mMusicService != null && mMusicService.getCurrentPlaylist() != null) {
                CurrentPlaylistFragment dialogFragment = new CurrentPlaylistFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("data", mMusicService.getCurrentPlaylist());
                bundle.putSerializable("map", mMusicService.getAllMusicData());
                dialogFragment.setArguments(bundle);
                dialogFragment.show(getSupportFragmentManager(), "ManageDbFragment");
            } else {
                Toast.makeText(MainActivity.this, R.string.notify_no_playing, Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        String title = item.getTitle().toString();
        int index = mMemuTitleList.indexOf(title);

        mViewPager.setCurrentItem(index, true);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void checkPermissions(String permission, int userPermission) {

        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {

            // 권한 체크 화면 보여주기
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // 사용자가 이전에 거부를 했을 경우
                ActivityCompat.requestPermissions(this, new String[]{permission}, userPermission);
            } else {
                // 권한이 없을 때 권한 요청
                ActivityCompat.requestPermissions(this, new String[]{permission}, userPermission);
            }
        }
        // 사용자가 이전에 승인을 했을 경우
        else {
            Log.d(TAG, "사용자가 이전에 승인을 했을 경우");
            switch (permission) {
                case Manifest.permission.READ_EXTERNAL_STORAGE:
                    EventBus.getDefault().post(new InitEvent());
                    break;
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    mFacade.createDb();
                    break;
            }
        }

    }

    /**
     * 최초 권한 승인시 호출
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult @ MainActivity");
        switch (requestCode) {
            case MainActivity.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d(TAG, "권한 승인됨");
                    EventBus.getDefault().post(new InitEvent());
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // 권한 거부 시 작업
                }
                return;
        }
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            // 하단 미니 플레이어의 미니앨범아트, 노래제목, 아티스트를 클릭하면 플레이어 페이지로 이동
            case R.id.player_miniAlbumArt_iv:
            case R.id.player_title_tv:
            case R.id.player_artist_tv:
                mViewPager.setCurrentItem(0);
                mTabLayout.setScrollPosition(0, 0, true);
                break;
            case R.id.player_previous_ib:
                if (mMusicService != null && mMusicService.isReady()) {

                    Intent nextIntent = new Intent(MainActivity.this, MusicService.class);
                    nextIntent.setAction(MusicService.ACTION_PLAY_PREVIOUS);
                    nextIntent.putExtra("position", mMusicService.getPositionAtPreviousOrNext(MusicService.ACTION_PLAY_PREVIOUS));
                    startService(nextIntent);
                }
                break;
            case R.id.player_play_ib:
                if (mMusicService != null && mMusicService.isReady()) {
                    Intent pauseIntent = new Intent(MainActivity.this, MusicService.class);
                    pauseIntent.setAction(MusicService.ACTION_PAUSE);
                    startService(pauseIntent);
                }
                break;
            case R.id.player_next_ib:
                if (mMusicService != null && mMusicService.isReady()) {

                    Intent nextIntent = new Intent(MainActivity.this, MusicService.class);
                    nextIntent.setAction(MusicService.ACTION_PLAY_NEXT);
                    nextIntent.putExtra("position", mMusicService.getPositionAtPreviousOrNext(MusicService.ACTION_PLAY_NEXT));
                    startService(nextIntent);
                }
                break;
            case R.id.player_shuffle_ib:
                if (v.isSelected()) {
                    v.setSelected(false);
                } else {
                    v.setSelected(true);
                }
                DataBackupUtil.getInstance(getApplicationContext()).saveIsShuffle(v.isSelected());
                break;
            case R.id.player_repeat_ib:
                if (v.isSelected()) {
                    v.setSelected(false);
                } else {
                    v.setSelected(true);
                }
                DataBackupUtil.getInstance(getApplicationContext()).saveIsRepeat(v.isSelected());
                break;
            case R.id.player_favorite_ib:
                if (mMusicService != null && mMusicService.isReady()) {
                    if (v.isSelected()) {
                        v.setSelected(false);
                    } else {
                        v.setSelected(true);
                    }

                    mFacade.toggleFavoriteList(mMusicService.getCurrentPlaylist().get(mMusicService.getCurrentPosition()));
                    EventBus.getDefault().post(new ReloadPlaylist());
                }
                break;
            case R.id.songs_playAll_btn:
                Intent playAllIntent = new Intent(MainActivity.this, MusicService.class);
                playAllIntent.setAction(MusicService.ACTION_PLAY);
                playAllIntent.putExtra("list", MusicInfoLoadUtil.getPlayAllList(MainActivity.this));
                playAllIntent.putExtra("position", 0);
                startService(playAllIntent);
                break;
            case R.id.fab:
                Intent addPlaylistIntent = new Intent(MainActivity.this, AddPlaylistActivity.class);
                startActivity(addPlaylistIntent);
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        switch (parent.getId()) {
            // Songs ListView 를 클릭할 경우 : 해당 곡만 재생
            case R.id.songs_listView: {
                ArrayList<Long> list = (ArrayList) MusicInfoLoadUtil.getSelectedSongPlaylist(MainActivity.this, (Cursor) parent.getAdapter().getItem(position));
                Intent intent = new Intent(MainActivity.this, MusicService.class);
                intent.setAction(MusicService.ACTION_PLAY);
                intent.putExtra("list", list);
                intent.putExtra("position", 0);
                startService(intent);
                break;
            }

            // 현재 재생목록의 리스트를 클릭할 경우 : 해당 포지션의 노래를 재생
            case R.id.current_playlistView: {
                Intent intent = new Intent(MainActivity.this, MusicService.class);
                intent.setAction(MusicService.ACTION_PLAY_SELECTED);
                intent.putExtra("position", position);
                startService(intent);
                break;
            }

        }


    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        switch (parent.getId()) {

            // 아티스트 프래그먼트의 확장 리스트뷰를 클릭하여 나오는 차일드 아이템을 클릭하면 해당 곡으로만 이루어진 리스트를 서비스에 전달하여 단일재생한다.
            // 이 부분도 해당 아티스트의 모든 노래를 재생하도록 변경
            case R.id.artist_ExlistView: {
                Cursor parentData = (Cursor) parent.getExpandableListAdapter().getGroup(groupPosition);
                String artist = parentData.getString(parentData.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                ArrayList<Long> list = MusicInfoLoadUtil.getArtistTrackInfoList(MainActivity.this, artist);
                Intent intent = new Intent(MainActivity.this, MusicService.class);
                intent.setAction(MusicService.ACTION_PLAY);
                intent.putExtra("list", list);
                intent.putExtra("position", childPosition);
                startService(intent);
                break;
            }
            case R.id.playlist_listView: {
                Cursor parentData = (Cursor) parent.getExpandableListAdapter().getGroup(groupPosition);
                parentData.moveToPosition(groupPosition);
                String playlistName = parentData.getString(parentData.getColumnIndexOrThrow(MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST));
                ArrayList<Long> list = MusicInfoLoadUtil.getMusicIdListFromPlaylistName(playlistName, getApplicationContext());
                Intent intent = new Intent(MainActivity.this, MusicService.class);
                intent.setAction(MusicService.ACTION_PLAY);
                intent.putExtra("list", list);
                intent.putExtra("position", childPosition);
                startService(intent);
                break;
            }

        }
        return false;
    }


    private class NavigationAdapter extends FragmentPagerAdapter {

        public NavigationAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return Manager.getInstance(position);
        }

        @Override
        public int getCount() {
            return Manager.FRAGMENTS.length;
        }
    }

}
