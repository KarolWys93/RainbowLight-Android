package com.wyskocki.karol.rainbowtable;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by karol on 04.03.18.
 */

public class DeviceChooser {

    private Context context;
    private Set<BluetoothDevice> devicesList;
    private String title;
    private BluetoothDevice selectedDevice;
    private OnSelectListener listener;


    public DeviceChooser(Context context, String title){
        this.context = context;
        this.title = title;
    }

    public DeviceChooser(Context context){
        this(context, "Choose device");
    }


    public void showChooser(){
        devicesList = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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

    public BluetoothDevice getSelectedDevice(){
        return selectedDevice;
    }

    public void addListener(OnSelectListener listener){
        this.listener = listener;
    }

    public void removeListener(){
        this.listener = null;
    }

    public interface OnSelectListener  {
        void onSelect(BluetoothDevice device);
    }
}
