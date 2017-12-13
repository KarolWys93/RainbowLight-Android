package com.wyskocki.karol.rainbowtable;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentColorSelected} interface
 * to handle interaction events.
 * Use the {@link RoundColorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoundColorFragment extends Fragment {

    ColorPicker colorPicker;
    SaturationBar saturationBar;
    ValueBar valueBar;

    private OnFragmentColorSelected mListener;

    public RoundColorFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment RoundColorFragment.
     */
    public static RoundColorFragment newInstance() {
        RoundColorFragment fragment = new RoundColorFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_round_color, container, false);

        colorPicker = (ColorPicker)view.findViewById(R.id.colorPicker);
        saturationBar = (SaturationBar)view.findViewById(R.id.saturationBar);
        valueBar = (ValueBar)view.findViewById(R.id.valueBar);

        colorPicker.setShowOldCenterColor(false);


        colorPicker.setOnColorSelectedListener(new ColorPicker.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                //colorPicker.setOldCenterColor(color);
                colorSelected(color);
            }
        });

        valueBar.setOnValueSelectListener(new ValueBar.OnValueSelectListener() {
            @Override
            public void onValueSelect(int value) {
                colorSelected(value);
            }
        });

        saturationBar.setOnSaturationSelectListener(new SaturationBar.OnSaturationSelectListener() {
            @Override
            public void onSaturationSelect(int value) {
                colorSelected(value);
            }
        });

        colorPicker.addSaturationBar(saturationBar);
        colorPicker.addValueBar(valueBar);
        valueBar.setColorPicker(colorPicker);
        saturationBar.setColorPicker(colorPicker);

        valueBar.setEnabled(false);

        ((ImageButton)view.findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rgbColorDialog();
            }
        });

        return view;
    }

    private void rgbColorDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_rgb_color, null);
        ((SeekBar)dialogView.findViewById(R.id.redBar)).setProgress(Color.red(colorPicker.getColor()));
        ((SeekBar)dialogView.findViewById(R.id.greenBar)).setProgress(Color.green(colorPicker.getColor()));
        ((SeekBar)dialogView.findViewById(R.id.blueBar)).setProgress(Color.blue(colorPicker.getColor()));
        builder.setTitle("Color");
        builder.setView(dialogView);
        builder.setCancelable(true);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int r = ((SeekBar) dialogView.findViewById(R.id.redBar)).getProgress();
                int g = ((SeekBar) dialogView.findViewById(R.id.greenBar)).getProgress();
                int b = ((SeekBar) dialogView.findViewById(R.id.blueBar)).getProgress();
                colorPicker.setColor(Color.argb(255, r,g,b));
                colorSelected(Color.argb(255, r, g, b));
            }
        });
        builder.create().show();
    }

    public void colorSelected(int color) {
        if (mListener != null) {
            mListener.colorSelected(color);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentColorSelected) {
            mListener = (OnFragmentColorSelected) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
