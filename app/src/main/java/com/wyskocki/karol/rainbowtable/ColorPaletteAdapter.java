package com.wyskocki.karol.rainbowtable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by karol on 12.12.17.
 */

public class ColorPaletteAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList colors;
    private int selectedPos = -1;

    public ColorPaletteAdapter(Context c, ArrayList<Integer> colors) {
        mContext = c;
        this.colors = colors;
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
            Bitmap checkIcon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_done_black_48dp);
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

    int size_dp(int size){
        Resources res = mContext.getResources();
        return (int)(size*res.getDisplayMetrics().density);
    }
}
