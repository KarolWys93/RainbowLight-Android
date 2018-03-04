package com.wyskocki.karol.rainbowtable;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements OnFragmentColorSelected {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager viewPager;
    private LedControler btLedControl;
    private Menu menu;

    private DeviceChooser deviceChooser;

    private BluetoothDevice btDevice;

    private final int REQUEST_ENABLE_BT = 1;

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

        deviceChooser = new DeviceChooser(this);
        deviceChooser.addListener(new DeviceChooser.OnSelectListener(){

            @Override
            public void onSelect(BluetoothDevice device) {
                btDevice = device;
                startConnection();
                ((MenuItem)menu.findItem(R.id.action_connect)).setTitle("Disconnect");
            }
        });

        if(savedInstanceState != null) {
            Log.i("savedInstanceState: ", "exist");
            if (savedInstanceState.getBoolean("wasConnected", false)) {
                Log.i("savedInstanceState", "was connected");
                //selectedDeviceName = savedInstanceState.getString("btDeviceName", null);
                //if (selectedDeviceName != null) {
                //    Log.i("savedInstanceState", "start connection");
                //   startConnection();
                //((MenuItem) menu.findItem(R.id.action_connect)).setTitle("Disconnect");
            }
        }
    }


    private void setupTabIcons(TabLayout tabLayout) {
        //tabLayout.getTabAt(0).setIcon(R.drawable.ic_adjust_white_24dp);
        //tabLayout.getTabAt(1).setIcon(R.drawable.ic_palette_white_24dp);
        //tabLayout.getTabAt(2).setIcon(R.drawable.ic_play_circle_outline_white_24dp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        this.menu = menu;
        if(btLedControl != null && btLedControl.isConnected())
            menu.findItem(R.id.action_connect).setTitle("Disconnect");
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
            if (!bt.isEnabled()){
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, REQUEST_ENABLE_BT);
            }else{
                if (btLedControl == null || !btLedControl.isConnected()) {

                    deviceChooser.showChooser();

                }else{
                    try {
                        btLedControl.close();
                        ((MenuItem)menu.findItem(R.id.action_connect)).setTitle("Connect");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (btLedControl != null) {
            outState.putBoolean("wasConnected", btLedControl.isConnected());
        }else {
            outState.putBoolean("wasConnected", false);
        }
        //outState.putString("btDeviceName", selectedDeviceName);
    }

    @Override
    protected void onDestroy() {
        if(btLedControl != null){
            try {
                btLedControl.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }



    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(RoundColorFragment.newInstance(), "Color ring");
        adapter.addFragment(PaletteFragment.newInstance(), "Palette");
        //adapter.addFragment(RoundColorFragment.newInstance(), "Animation");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void colorSelected(int color) {
        if(btLedControl != null && btLedControl.isConnected())
            try {
                btLedControl.sendColor(color);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getBaseContext(), "Connection problem!", Toast.LENGTH_LONG).show();
            }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK){
            startConnection();
        }
    }

    //TODO Przenieść do serwisu
    void startConnection(){
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        bt.cancelDiscovery();

        btLedControl = new LedControler(btDevice);
        try {
            btLedControl.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
