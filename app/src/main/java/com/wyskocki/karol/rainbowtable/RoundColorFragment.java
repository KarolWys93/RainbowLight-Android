package com.wyskocki.karol.rainbowtable;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;
import com.wyskocki.karol.dsp.DigitalFilter;
import com.wyskocki.karol.dsp.MeanFilter;

import java.util.List;


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

    private Sensor lightSensor;
    private SensorEventListener sensorListener;
    private DigitalFilter lightSensorFilter;
    private boolean fragmentIsVisible = false;

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
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
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
                colorSelected(colorPicker.getColor());
            }
        });

        valueBar.setOnValueSelectListener(new ValueBar.OnValueSelectListener() {
            @Override
            public void onValueSelect(int value) {
                colorSelected(colorPicker.getColor());
            }
        });

        saturationBar.setOnSaturationSelectListener(new SaturationBar.OnSaturationSelectListener() {
            @Override
            public void onSaturationSelect(int value) {
                colorSelected(colorPicker.getColor());
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

        lightSensor = getLightSensor();

        CheckBox autoValueCheckBox = view.findViewById(R.id.autoValueChBox);
        if (lightSensor == null){
            autoValueCheckBox.setEnabled(false);
        }else {
            setLightSensorEnable(autoValueCheckBox.isChecked());
            autoValueCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox checkBox = (CheckBox) v;
                    setLightSensorEnable(checkBox.isChecked());
                }
            });

        }

        if(savedInstanceState != null){
            setLightSensorEnable(savedInstanceState.getBoolean("autoValue", false));
        }

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


    private Sensor getLightSensor(){
        Activity activity = getActivity();
        SensorManager sensorManager = (SensorManager) activity.getSystemService( Context.SENSOR_SERVICE );
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_LIGHT);
        if(sensors.isEmpty()){
            return null;
        }
        return sensors.get(0);
    }

    private void setLightSensorEnable(Boolean enable){
        Activity activity = getActivity();
        SensorManager sensorManager = (SensorManager) activity.getSystemService( Context.SENSOR_SERVICE );

        if(enable){
            lightSensorFilter = new MeanFilter(16);
            sensorListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float max = 40;
                    float value = 1/max * event.values[0];
                    value = (float)lightSensorFilter.filter(Math.min(value, 1.0f));
                    valueBar.setValue(value);
                    if(fragmentIsVisible) {
                        colorSelected(colorPicker.getColor());
                    }
                }
                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {}
            };

            sensorManager.registerListener(sensorListener, lightSensor, SensorManager.SENSOR_DELAY_GAME);//SensorManager.SENSOR_DELAY_UI);
            Toast.makeText(getContext(), "Auto value mode enabled", Toast.LENGTH_SHORT).show();
        }else {
            if (sensorListener != null)
                sensorManager.unregisterListener(sensorListener);
        }
    }

    public void colorSelected(int color) {
        if (mListener != null) {
            mListener.colorSelected(color);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("autoValue", ((CheckBox)getActivity().findViewById(R.id.autoValueChBox)).isChecked());
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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        fragmentIsVisible = isVisibleToUser;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    @Override
    public void onDestroy() {
        setLightSensorEnable(false);
        super.onDestroy();
    }
}
