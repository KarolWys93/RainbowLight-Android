package com.wyskocki.karol.rainbowlight;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import java.util.List;


/**
 * RoundColorFragment is {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentColorSelected} interface
 * to handle interaction events.
 * Use the {@link RoundColorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoundColorFragment extends Fragment {

    //fields

    private ColorPicker colorPicker;
    private SaturationBar saturationBar;
    private ValueBar valueBar;

    private Sensor lightSensor;
    private SensorEventListener sensorListener;
    private boolean fragmentIsVisible = false;

    private OnFragmentColorSelected mListener;
    private ColorRGBChooser colorRGBChooser;


    //constructors

    public RoundColorFragment() {}

    //Factory methods

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

    //Override methods

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


        colorRGBChooser = new ColorRGBChooser(this.getActivity(), getString(R.string.rgb_color_chooser_title));
        colorRGBChooser.setListener(new ColorRGBChooser.ChangeListener() {
            @Override
            public void onChange(int color) {
                colorPicker.setColor(color);
                colorSelected(color);
            }
        });


        loadPreferences();

        if(savedInstanceState != null){
            setLightSensorEnable(savedInstanceState.getBoolean("autoValue", false));
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        CheckBox autoValueCB = (CheckBox) getActivity().findViewById(R.id.autoValueChBox);
        if(autoValueCB != null) {
            outState.putBoolean("autoValue", autoValueCB.isChecked());
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
        savePreferences();
        super.onDestroy();
    }

    //private methods

    /**
     * Show dialog with rgb color chooser
     */
    private void rgbColorDialog(){
        colorRGBChooser.show(colorPicker.getColor());
    }

    /**
     * Return light sensor. Sensor is used to automated color value.
     * @return light sensor
     */
    private Sensor getLightSensor(){
        Activity activity = getActivity();
        SensorManager sensorManager = (SensorManager) activity.getSystemService( Context.SENSOR_SERVICE );
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_LIGHT);
        if(sensors.isEmpty()){
            return null;
        }
        return sensors.get(0);
    }

    /**
     * Set enable auto value mode.
     * @param enable
     */
    private void setLightSensorEnable(Boolean enable){
        Activity activity = getActivity();
        SensorManager sensorManager = (SensorManager) activity.getSystemService( Context.SENSOR_SERVICE );

        if(enable){
            sensorListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float max = 40;
                    float value = 1/max * event.values[0] + 0.01f;
                    value = Math.min(value, 1.0f);
                    Log.i("Light value", " "+value);
                    valueBar.setValue(value);
                    if(fragmentIsVisible) {
                        colorSelected(colorPicker.getColor());
                    }
                }
                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {}
            };

            sensorManager.registerListener(sensorListener, lightSensor, SensorManager.SENSOR_DELAY_GAME);//SensorManager.SENSOR_DELAY_UI);
            Toast.makeText(getContext(), R.string.auto_value_mode_enabled_notificaton, Toast.LENGTH_SHORT).show();
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

    private void loadPreferences(){
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        colorPicker.setColor(preferences.getInt("recent_color", Color.RED));
    }

    private void savePreferences(){
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if(colorPicker != null) {
            editor.putInt("recent_color", colorPicker.getColor());
        }
        editor.commit();
    }
}
