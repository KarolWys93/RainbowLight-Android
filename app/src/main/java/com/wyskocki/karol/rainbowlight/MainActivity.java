package com.wyskocki.karol.rainbowlight;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.wyskocki.karol.rainbowlight.ledcontroller.LedControllerService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnFragmentColorSelected, OnFragmentAnimationSelected {

    //UI fields

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager viewPager;
    private Menu menu;
    private DeviceChooser deviceChooser;

    //fields

    private BluetoothDevice btDevice;
    private LedControllerService ledService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            ledService.removeConnectionListener();
            ledService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            LedControllerService.LedControllerBinder binder = (LedControllerService.LedControllerBinder)service;
            ledService = binder.getService();
            ledService.setConnectionListener(connectionListener);
        }
    };
    private LedControllerService.ConnectionListener connectionListener = new LedControllerService.ConnectionListener() {
        @Override
        public void onConnect(boolean success) {
            if(success){
                Toast.makeText(getBaseContext(), R.string.bluetooth_connected_notification, Toast.LENGTH_LONG).show();
                ((MenuItem)menu.findItem(R.id.action_connect)).setTitle(R.string.menu_disconnect);
                ((MenuItem)menu.findItem(R.id.action_connect)).setEnabled(true);
            }else {
                Toast.makeText(getBaseContext(), R.string.bluetooth_error_while_connectiong_notification, Toast.LENGTH_LONG).show();
                ((MenuItem)menu.findItem(R.id.action_connect)).setTitle(R.string.menu_connect);
                ((MenuItem)menu.findItem(R.id.action_connect)).setEnabled(true);
            }
        }
    };

    //Intents ID

    private final int REQUEST_ENABLE_BT = 1;


    //Override methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the ViewPager with the sections adapter.
        viewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        //setupTabIcons(tabLayout);

        deviceChooser = new DeviceChooser(this, getString(R.string.device_chooser_title));
        deviceChooser.addListener(new DeviceChooser.OnSelectListener(){
            @Override
            public void onSelect(BluetoothDevice device) {
                btDevice = device;
                startConnection();
                menu.findItem(R.id.action_connect).setTitle(R.string.menu_connecting);
                menu.findItem(R.id.action_connect).setEnabled(false);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindWithService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        this.menu = menu;
        if(ledService != null && ledService.isConnected())
            menu.findItem(R.id.action_connect).setTitle(R.string.menu_disconnect);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_connect) {
            BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
            if (bt == null){
                Toast.makeText(getBaseContext(), R.string.no_bluetooth_notification, Toast.LENGTH_LONG).show();
                finish();
            }else {
                if (!bt.isEnabled()){
                    Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBT, REQUEST_ENABLE_BT);
                }else {
                    if (ledService == null || !ledService.isConnected()) {

                        deviceChooser.showChooser();

                    } else {
                        try {
                            writeToLog("Disconnect");
                            ledService.close();
                            ((MenuItem) menu.findItem(R.id.action_connect)).setTitle(R.string.menu_connect);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        if (id == R.id.action_about){
            Intent about = new Intent(this, AboutActivity.class);
            startActivity(about);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK){
            Toast.makeText(getBaseContext(), R.string.bluetooth_enabled_notification, Toast.LENGTH_LONG).show();
        }
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED){
            Toast.makeText(getBaseContext(), R.string.bluetooth_disabled_notification, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void colorSelected(int color) {
        if(ledService != null && ledService.isConnected()) {
            writeToLog("set Color");
            try {
                ledService.setColor(color);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getBaseContext(), R.string.bluetooth_problem_notification, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void animationSelected(int param1, int param2) {
        if(ledService != null && ledService.isConnected()){
            writeToLog("set Animation");
            try{
                ledService.setAnimation(param1, param2);
            }catch (IOException e){
                e.printStackTrace();
                Toast.makeText(getBaseContext(), R.string.bluetooth_problem_notification, Toast.LENGTH_LONG).show();
            }
        }
    }


    //private methods

    /**
     * This method connects MainActivity object with {@link LedControllerService}.
     * When service don't exist, it will be created.
     */
    private void bindWithService(){
        writeToLog("bind to the service");
        Intent serviceIntent = new Intent(this, LedControllerService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setupTabIcons(TabLayout tabLayout) {
//        tabLayout.getTabAt(0).setIcon(R.drawable.ic_adjust_white_24dp);
//        tabLayout.getTabAt(1).setIcon(R.drawable.ic_palette_white_24dp);
//        tabLayout.getTabAt(2).setIcon(R.drawable.ic_play_circle_outline_white_24dp);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(RoundColorFragment.newInstance(), getString(R.string.color_ring_tab_name));
        adapter.addFragment(PaletteFragment.newInstance(), getString(R.string.predef_palette_tab_name));
        adapter.addFragment(AnimationFragment.newInstance(), getString(R.string.animations_tab_name));
        viewPager.setAdapter(adapter);
    }

    /**
     * Starting connection with device
     */
    private void startConnection(){
        writeToLog("connect");
        try {
            ledService.connect(btDevice);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), R.string.bluetooth_error_while_connectiong_notification, Toast.LENGTH_LONG).show();
        }
    }

    private void writeToLog(String message){
        Log.d(MainActivity.class.getName(), message);
    }


    //Inner class
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }

        public void addFragment(Fragment fragment, String title){
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }
    }


}
