package com.wyskocki.karol.rainbowlight;

/**
 * Interface used to allow fragments to notify activity, when color was chosen.
 * <br/><br/>
 * Created by karol on 11.12.17.
 */

public interface OnFragmentColorSelected {

    /**
     * This method was invoked, when color was selected by fragment.
     * @param color selected color
     */
    void colorSelected(int color);
}
