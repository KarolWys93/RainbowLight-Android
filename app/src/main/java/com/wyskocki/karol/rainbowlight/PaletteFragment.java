package com.wyskocki.karol.rainbowlight;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;


/**
 * PaletteFragment is {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentColorSelected} interface
 * to handle interaction events.
 * Use the {@link PaletteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PaletteFragment extends Fragment {

    //fields

    private OnFragmentColorSelected mListener;
    private ColorPaletteAdapter paletteAdapter;
    private GridView gridview;

    //constuctors

    public PaletteFragment() {}

    //Factory methods

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     * @return A new instance of fragment PaletteFragment.
     */
    public static PaletteFragment newInstance() {
        PaletteFragment fragment = new PaletteFragment();
        return fragment;
    }

    //Override methods

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_palette, container, false);

        gridview = (GridView) view.findViewById(R.id.paletteView);
        paletteAdapter = new ColorPaletteAdapter(getContext(), HSVColors());

        if(savedInstanceState != null) {
            paletteAdapter.setSelectedPos(savedInstanceState.getInt("SelectedColorPos", -1));
        }

        gridview.setAdapter(paletteAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                paletteAdapter.setSelectedPos(position);
                gridview.invalidateViews();
                onColorSelected(paletteAdapter.getSelectedColor());
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("SelectedColorPos", paletteAdapter.getSelectedPos());
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

    //Private methods

    private void onColorSelected(int color) {
        if (mListener != null) {
            mListener.colorSelected(color);
        }
    }

    /**
     * Custom method to generate hsv colors list
     * @return list of colors
     */
    private ArrayList HSVColors(){
        ArrayList<Integer> colors= new ArrayList<>();

        // Loop through hue channel, saturation and light full
        for(int h=0;h<=360;h+=20){
            colors.add(HSVColor(h, 1, 1));
        }

        // Loop through hue channel, different saturation and light full
        for(int h=0;h<=360;h+=20){
            colors.add(HSVColor(h, .25f, 1));
            colors.add(HSVColor(h, .5f, 1));
            colors.add(HSVColor(h, .75f, 1));
        }

        // Loop through hue channel, saturation full and light different
        for(int h=0;h<=360;h+=20){
            //colors.add(createColor(h, 1, .25f));
            colors.add(HSVColor(h, 1, .5f));
            colors.add(HSVColor(h, 1, .75f));
        }

        // Loop through the light channel, no hue no saturation
        // It will generate gray colors
        for(float b=0;b<=1;b+=.10f){
            colors.add(HSVColor(0, 0, b));
        }
        return colors;
    }

    /**
     * Create HSV color from values
     * @param hue
     * @param saturation
     * @param black
     * @return color
     */
    private int HSVColor(float hue, float saturation, float black){
        /*
            Hue is the variation of color
            Hue range 0 to 360

            Saturation is the depth of color
            Range is 0.0 to 1.0 float value
            1.0 is 100% solid color

            Value/Black is the lightness of color
            Range is 0.0 to 1.0 float value
            1.0 is 100% bright less of a color that means black
        */
        int color = Color.HSVToColor(255,new float[]{hue,saturation,black});
        return color;
    }

}
