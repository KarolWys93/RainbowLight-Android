package com.wyskocki.karol.rainbowtable;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

/**
 * Created by karol on 10.03.18.
 */

public class ColorRGBChooser {

    private int color = Color.RED;
    private String title;
    private AlertDialog dialog;
    private final View dialogView;


    private ChangeListener listener;

    interface ChangeListener{
        void onChange(int color);
    }


    public ColorRGBChooser(final Activity activity, String title){
        this.title = title;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_rgb_color, null);
        ((SeekBar)dialogView.findViewById(R.id.redBar)).setProgress(Color.red(color));
        ((SeekBar)dialogView.findViewById(R.id.greenBar)).setProgress(Color.green(color));
        ((SeekBar)dialogView.findViewById(R.id.blueBar)).setProgress(Color.blue(color));
        builder.setTitle(this.title);
        builder.setView(dialogView);
        builder.setCancelable(true);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int r = ((SeekBar) dialogView.findViewById(R.id.redBar)).getProgress();
                int g = ((SeekBar) dialogView.findViewById(R.id.greenBar)).getProgress();
                int b = ((SeekBar) dialogView.findViewById(R.id.blueBar)).getProgress();
                color = Color.argb(255, r, g, b);
                if(listener != null){
                    listener.onChange(color);
                }
            }
        });
        dialog = builder.create();
    }

    public void show(){
        show(color);
    }

    public void show(int color){
        this.color = color;
        ((SeekBar)dialogView.findViewById(R.id.redBar)).setProgress(Color.red(color));
        ((SeekBar)dialogView.findViewById(R.id.greenBar)).setProgress(Color.green(color));
        ((SeekBar)dialogView.findViewById(R.id.blueBar)).setProgress(Color.blue(color));
        dialog.show();
    }


    void setListener(ChangeListener listener){
        this.listener = listener;
    }

    void removeListener(){
        this.listener = null;
    }




}
