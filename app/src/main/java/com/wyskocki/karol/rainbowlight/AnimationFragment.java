package com.wyskocki.karol.rainbowlight;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;


/**
 * AnimationFragment is {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentAnimationSelected} interface
 * to handle interaction events.
 * Use the {@link PaletteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AnimationFragment extends Fragment {

    private OnFragmentAnimationSelected mListener;

    public AnimationFragment() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AnimationFragment.
     */
    public static AnimationFragment newInstance() {
        AnimationFragment fragment = new AnimationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_animation, container, false);

        ((Button)view.findViewById(R.id.runBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), R.string.animation_run_notification,Toast.LENGTH_SHORT).show();
                animationSelected();
            }
        });
        // Inflate the layout for this fragment
        return view;
    }

    public void animationSelected() {

        int speed = ((SeekBar)getActivity().findViewById(R.id.speedBar)).getProgress();
        int brightness = ((SeekBar)getActivity().findViewById(R.id.brightnessBar)).getProgress();
        if (mListener != null) {
            mListener.animationSelected(speed,brightness);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentAnimationSelected) {
            mListener = (OnFragmentAnimationSelected) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentAnimationSelected");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
