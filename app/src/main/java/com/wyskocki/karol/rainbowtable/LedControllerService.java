package com.wyskocki.karol.rainbowtable;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by karol on 04.03.18.
 */

public class LedControllerService extends Service {

    private final boolean testMode = false;

    //instruction
    private final int SEND_COLOR = 1;
    private final int SEND_RAINBOW = 82;

    private static UUID PRIVATE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outStream;

    private ConnectionListener listener;



    public void setColor(int color) throws IOException {
        byte red = (byte) Color.red(color);
        byte green = (byte) Color.green(color);
        byte blue = (byte) Color.blue(color);
        if(testMode){
            send(String.format("<%d %d %d %d> ",
                    SEND_COLOR,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)).getBytes());
        }else {
            send(new byte[]{SEND_COLOR, red, green, blue});
        }
    }

    public void sendAnimation(int speed, int amplitude) throws IOException {
        if(testMode){
            send(String.format("%d %d %d", SEND_RAINBOW, speed, amplitude).getBytes());
        }else {
            send(new byte[]{SEND_RAINBOW, (byte) speed, (byte) amplitude});
        }
    }


    private void send(byte[] frame) throws IOException {
        if(isConnected()){
            outStream.write(frame);
            outStream.flush();
        }
    }

    public void connect(BluetoothDevice device) throws IOException {

        //close previous connection (if any exist)
        close();

        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        bt.cancelDiscovery();

        this.device = device;
        writeToLog("Start connecting");

        socket = device.createRfcommSocketToServiceRecord(PRIVATE_UUID);
        new ConnectTask().execute(socket);

        outStream = socket.getOutputStream();
    }

    public boolean isConnected(){
        if (socket != null)
            return socket.isConnected();
        else
            return false;
    }

    public String getDeviceName(){
        return device.getName();
    }

    public void close() throws IOException {
        writeToLog("Close connection");
        if(outStream != null)
            outStream.close();

        if (socket != null)
            socket.close();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        writeToLog("Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        writeToLog("Service start command");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addConnectionListener(ConnectionListener listener){
        this.listener = listener;
    }

    public void removeConnectionListener(){
        this.listener = null;
    }

    private final IBinder binder = new LedControllerBinder();


    private class ConnectTask extends AsyncTask<BluetoothSocket, Void, Boolean>{

        @Override
        protected Boolean doInBackground(BluetoothSocket... bluetoothSockets) {
            try {
                bluetoothSockets[0].connect();

            } catch (IOException e) {
                try {
                    bluetoothSockets[0].close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return bluetoothSockets[0].isConnected();
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
            writeToLog("Connected successfully: "+bool);
            listener.onConnect(bool);
        }
    }

    interface ConnectionListener{
        void onConnect(boolean success);
    }

    public class LedControllerBinder extends Binder {
        LedControllerService getService() {
            return LedControllerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void writeToLog(String message){
        Log.d(LedControllerService.class.getName(), message);
    }

}
