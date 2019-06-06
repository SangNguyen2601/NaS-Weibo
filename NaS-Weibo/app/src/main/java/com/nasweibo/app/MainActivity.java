package com.nasweibo.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.nasweibo.app.chat.ConversationListFragment;
import com.nasweibo.app.contact.ContactFragment;
import com.nasweibo.app.services.NewMessageService;
import com.nasweibo.app.services.PushStateOnlineService;
import com.nasweibo.app.settings.SettingFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    TextView textView;
    private TabLayout tabLayout = null;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private FloatingActionButton floatButton;
    private NewMessageService newMessageService;
    private boolean isBound = false;

    public static String CHAT_FRAGMENT = "CHAT";
    public static String CONTACT_FRAGMENT = "CONTACT";
    public static String SETTING = "SETTING";

    private static boolean isVisible = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NewMessageService.LocalBinder binder = (NewMessageService.LocalBinder) service;
            newMessageService = binder.getService();
            Log.d("Noti_MainActivity", "setShowNoti()");
            newMessageService.setShowNoti(false);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.hideOverflowMenu();

        viewPager = findViewById(R.id.viewpager);
//        floatButton = findViewById(R.id.fab);
        initTab();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(MainActivity.this, PushStateOnlineService.class);
        startService(intent);
        Intent intent2 = new Intent(this, NewMessageService.class);
        startService(intent2);
        bindService(intent2, serviceConnection, Context.BIND_AUTO_CREATE);
        isBound = true;
        isVisible = true;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (newMessageService != null) {
            Log.d("Noti_MainActivity::onStop()", "setShowNoti()");
            newMessageService.setShowNoti(true);
        }
        isVisible = false;
    }

    public static boolean checkVisible(){
        return isVisible;
    }
    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PushStateOnlineService.class));
        if(isBound){
            unbindService(serviceConnection);
            isBound = false;
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private void initTab() {
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorIndivateTab));
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
    }

    private void setupTabIcons() {
        int[] tabIcons = {
                R.drawable.ic_tab_chat,
                R.drawable.ic_tab_contact,
                R.drawable.ic_tab_setting
        };

        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new ConversationListFragment(), CHAT_FRAGMENT);
        adapter.addFrag(new ContactFragment(), CONTACT_FRAGMENT);
        adapter.addFrag(new SettingFragment(), SETTING);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {

            // return null to display only the icon
            return null;
        }
    }
}
