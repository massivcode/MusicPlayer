package com.massivcode.androidmusicplayer.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
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
import android.widget.Toast;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.interfaces.Event;
import com.massivcode.androidmusicplayer.interfaces.MusicEvent;
import com.massivcode.androidmusicplayer.managers.Manager;
import com.massivcode.androidmusicplayer.model.MusicInfo;
import com.massivcode.androidmusicplayer.services.MusicService;
import com.massivcode.androidmusicplayer.util.MusicInfoUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private NavigationAdapter mNavigationAdapter;

    private List<String> mMemuTitleList;

    private Intent mServiceIntent;

    private MusicService mMusicService;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            mMusicService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // EventBus 등록이 되어서 모든 이벤트를 수신 가능
        EventBus.getDefault().register(this);

        checkPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, 1);

        mServiceIntent = new Intent(MainActivity.this, MusicService.class);
        bindService(mServiceIntent, mConnection, BIND_AUTO_CREATE);

        initViews();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 해제 꼭 해주세요
        EventBus.getDefault().unregister(this);
    }

    // EventBus 용 이벤트 수신
    public void onEvent(Event event) {
        Toast.makeText(MainActivity.this, "액티비티 : 이벤트 수신함", Toast.LENGTH_SHORT).show();
        MusicEvent musicEvent = (MusicEvent)event;
        Log.d(TAG, "test : " + musicEvent.getMusicInfo().getTitle());
    }

    private void initViews() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mMemuTitleList = Arrays.asList(getResources().getStringArray(R.array.nav_menu_array));
        mNavigationAdapter = new NavigationAdapter(getSupportFragmentManager());

        mTabLayout = (TabLayout)findViewById(R.id.tab_layout);
        mTabLayout.addTab(mTabLayout.newTab().setText("플레이어"));
        mTabLayout.addTab(mTabLayout.newTab().setText("재생목록"));
        mTabLayout.addTab(mTabLayout.newTab().setText("아티스트"));
        mTabLayout.addTab(mTabLayout.newTab().setText("노래"));

        mViewPager = (ViewPager)findViewById(R.id.view_pager);
        mViewPager.setAdapter(mNavigationAdapter);
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
                navigationView.getMenu().getItem(position).setChecked(true);
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

    private void checkPermissions(String permission, int userPermission) {

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
                break;
            case R.id.player_previous_ib:
                break;
            case R.id.player_play_ib:
                if(mMusicService != null & mMusicService.isReady()) {
                    Intent pauseIntent = new Intent(MainActivity.this, MusicService.class);
                    pauseIntent.setAction(MusicService.ACTION_PAUSE);
                    startService(pauseIntent);
                }
                break;
            case R.id.player_next_ib:
                break;
            case R.id.player_shuffle_ib:
                break;
            case R.id.player_repeat_ib:
                break;
            case R.id.player_favorite_ib:
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MusicInfo info = MusicInfoUtil.getSelectedMusicInfo(MainActivity.this, (Cursor) parent.getAdapter().getItem(position));
        ArrayList<Uri> list = MusicInfoUtil.makePlaylist(info);
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        intent.setAction(MusicService.ACTION_PLAY);
        intent.putExtra("list", list);
        intent.putExtra("position", 0);
        startService(intent);
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
