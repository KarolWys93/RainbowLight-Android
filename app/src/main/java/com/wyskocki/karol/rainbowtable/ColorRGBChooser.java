package com.wyskocki.karol.rainbowtable;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

/**
 * ColorRGBChooser class shows dialog window.
 * This color selector contains three sliders that represent
 * one of the three primary colors: red, green and blue.
 * Using this sliders, user can select any color.
 * <br/><br/>
 * Created by karol on 10.03.18.
 */

public class ColorRGBChooser {

    //fields

    private int color = Color.RED;
    private String title;
    private AlertDialog dialog;
    private final View dialogView;
    private ChangeListener listener;

    //Constructors

    /**
     * ColorRGBChooser constructor.
     * @param activity activity that creates dialog
     * @param title title of dialog window
     */
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
        builder.setPositiveButton(R.string.rgb_chooser_confirm_button, new DialogInterface.OnClickListener() {
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

    //interfaces

    /**
     * Interface used to allow color chooser dialog to run some code when color was selected.
     */
    public interface ChangeListener{

        /**
         * This method will be invoked when the "OK" button in the dialog is clicked..
         * @param color selected color
         */
        void onChange(int color);
    }

    //public methods

    /**
     * This method shows dialog window. Sliders use parameters of last selected color.
     * If it is first call of method, red will be used as parameter for sliders.
     */
    public void show(){
        show(color);
    }

    /**
     * This method shows dialog window. Sliders positions will be set using color
     * passed as parameter.
     * @param color color used to set sliders position.
     */
    public void show(int color){
        this.color = color;
        ((SeekBar)dialogView.findViewById(R.id.redBar)).setProgress(Color.red(color));
        ((SeekBar)dialogView.findViewById(R.id.greenBar)).setProgress(Color.green(color));
        ((SeekBar)dialogView.findViewById(R.id.blueBar)).setProgress(Color.blue(color));
        dialog.show();
    }

    /**
     * Return selected color. If no one was selected, return red.
     * @return
     */
    @NonNull
    public int getColor(){
        return color;
    }

    /**
     * Sets a listener to be invoked when new color will be chosen
     * @param listener
     */
    public void setListener(ChangeListener listener){
        this.listener = listener;
    }

    /**
     * Remove listener
     */
    public void removeListener(){
        this.listener = null;
    }




}
