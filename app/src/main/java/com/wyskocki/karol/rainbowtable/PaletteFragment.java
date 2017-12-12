package com.wyskocki.karol.rainbowtable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentColorSelected} interface
 * to handle interaction events.
 * Use the {@link PaletteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PaletteFragment extends Fragment {

    private OnFragmentColorSelected mListener;
    private ImageAdapter imageAdapter;
    GridView gridview;

    public PaletteFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     * @return A new instance of fragment PaletteFragment.
     */
    public static PaletteFragment newInstance() {
        PaletteFragment fragment = new PaletteFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_palette, container, false);

        gridview = (GridView) view.findViewById(R.id.paletteView);
        imageAdapter = new ImageAdapter((getContext()));
        if(savedInstanceState != null) {
            imageAdapter.setSelectedPos(savedInstanceState.getInt("SelectedColorPos", -1));
        }
        gridview.setAdapter(imageAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                imageAdapter.setSelectedPos(position);
                gridview.invalidateViews();
                Toast.makeText(getContext(), "" + Integer.toHexString(imageAdapter.getSelectedColor()),Toast.LENGTH_SHORT).show();
            }
        });


        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("SelectedColorPos", imageAdapter.getSelectedPos());
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.colorSelected();
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

    class ImageAdapter extends BaseAdapter{

        private Context mContext;
        ArrayList colors = HSVColors();
        int selectedPos = -1;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        @Override
        public int getCount() {
            return colors.size();
        }

        @Override
        public Object getItem(int position) {
            return colors.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(size_dp(8), size_dp(8), size_dp(8), size_dp(8));
            } else {
                imageView = (ImageView) convertView;
            }

            Bitmap bmp = Bitmap.createBitmap(size_dp(64), size_dp(64), Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bmp);
            Paint paint = new Paint();
            paint.setColor((Integer) colors.get(position));
            paint.setAntiAlias(true);
            canvas.drawCircle(bmp.getHeight()/2, bmp.getWidth()/2,bmp.getHeight()/2, paint);

            if(position == selectedPos) {
                Bitmap checkIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_done_black_48dp);
                canvas.drawBitmap(checkIcon, null, new Rect(0, 0,bmp.getHeight(), bmp.getWidth()), paint);
            }

            imageView.setImageBitmap(bmp);
            return imageView;
        }

        public void setSelectedPos(int pos){
            selectedPos = pos;
        }

        public int getSelectedPos(){
            return selectedPos;
        }

        public int getSelectedColor(){
            if(selectedPos < 0 || selectedPos >= colors.size())
                return 0;

            return (int) colors.get(selectedPos);
        }

        // Custom method to generate hsv colors list
        public ArrayList HSVColors(){
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

        int size_dp(int size){
            Resources res = getResources();
            return (int)(size*res.getDisplayMetrics().density);
        }

        // Create HSV color from values
        public int HSVColor(float hue, float saturation, float black){
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
}
