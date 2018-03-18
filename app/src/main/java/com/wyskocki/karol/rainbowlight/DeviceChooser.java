package com.wyskocki.karol.rainbowlight;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Set;

/**
 * DeviceChooser class shows dialog window.
 * This window allows the user to select the bt device from the list of devices
 * that were previously paired.
 * <br/><br/>
 * Created by karol on 04.03.18.
 */

public class DeviceChooser {


    //fields
    private Activity activity;
    private Set<BluetoothDevice> devicesList;
    private String title;
    private BluetoothDevice selectedDevice;
    private OnSelectListener listener;


    //constructors

    /**
     * DeviceChooser constructor
     * @param activity activity that creates dialog
     * @param title title of window
     */
    public DeviceChooser(Activity activity, String title){
        this.activity = activity;
        this.title = title;
    }

    /**
     * DeviceChooser constructor. Title of window is set as "Choose device"
     * @param activity activity that creates dialog
     */
    public DeviceChooser(Activity activity){
        this(activity, "Choose device");
    }


    //public methods

    /**
     * This method shows dialog window.
     */
    public void showChooser(){
        devicesList = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        final ArrayList<CharSequence> deviceNames = new ArrayList<>();

        for (BluetoothDevice device : devicesList) {
            deviceNames.add(device.getName());
        }

        builder.setItems(deviceNames.toArray(new CharSequence[0]),new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String deviceName = (String) deviceNames.get(which);
                for(BluetoothDevice bt : devicesList){
                    if(bt.getName().equals(deviceName)){
                        selectedDevice = bt;
                        break;
                    }
                }
                Log.d("Device Chooser", "Selected device: " + selectedDevice.getName());
                if(listener != null){
                    Log.d("Device Chooser", "Call listener");
                    listener.onSelect(selectedDevice);
                }
            }
        });

        selectedDevice = null;
        Log.d("Device Chooser", "Chooser will be show");
        builder.create().show();
    }

    /**
     * This function return bluetooth device, which was selected before.
     * @return selected device. Can be null.
     */
    @Nullable
    public BluetoothDevice getSelectedDevice(){
        return selectedDevice;
    }

    /**
     * Sets a listener to be invoked when new color will be chosen
     * @param listener
     */
    public void addListener(OnSelectListener listener){
        this.listener = listener;
    }

    /**
     * Remove listener
     */
    public void removeListener(){
        this.listener = null;
    }

    //interfaces

    /**
     * Interface used to allow chooser dialog to run some code when device was selected.
     */
    public interface OnSelectListener  {
        /**
         * This method will be invoked when any device was selected.
         * @param device selected bluetooth device
         */
        void onSelect(BluetoothDevice device);
    }
}
