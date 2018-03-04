package com.wyskocki.karol.rainbowtable;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by karol on 12.12.17.
 */

public class LedControler {

    private final boolean testMode = false;

    //instruction
    private final int SEND_COLOR = 1;
    private final int SEND_RAINBOW = 82;

    private static UUID PRIVATE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothDevice btDevice;
    private BluetoothSocket socket;
    private OutputStream outStream;


    public LedControler(BluetoothDevice device){
        this.btDevice = device;
    }

    public void sendColor(int color) throws IOException {
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

    public void connect() throws IOException {
        Log.i("BT connection: ", "Start");
        BluetoothSocket tmp = null;
        tmp = btDevice.createRfcommSocketToServiceRecord(PRIVATE_UUID);
        socket = tmp;

        Thread connectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.connect();
                } catch (IOException e) {
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        connectionThread.start();
        outStream = socket.getOutputStream();
    }

    public boolean isConnected(){
        return socket.isConnected();
    }

    public void close() throws IOException {
        outStream.close();
        socket.close();
    }
}