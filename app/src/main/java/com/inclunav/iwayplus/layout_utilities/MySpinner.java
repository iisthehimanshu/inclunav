package com.inclunav.iwayplus.layout_utilities;

import android.content.Context;
import android.util.AttributeSet;


import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

//had to create this library because spinner onItemSelected
//doesn't get called when clicked item remains the same

public class MySpinner extends SearchableSpinner {

    public MySpinner(Context context)
    { super(context); }

    public MySpinner(Context context, AttributeSet attrs)
    { super(context, attrs); }

    public MySpinner(Context context, AttributeSet attrs, int defStyle)
    { super(context, attrs, defStyle); }

    @Override public void
    setSelection(int position, boolean animate)
    {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position, animate);
        if (sameSelected) {
            // Spinner does not call the OnItemSelectedListener if the same item is selected, so do it manually now
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }

    @Override public void
    setSelection(int position)
    {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position);
        if (sameSelected) {
            // Spinner does not call the OnItemSelectedListener if the same item is selected, so do it manually now
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }
}