package com.inclunav.iwayplus.layout_utilities;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

public class InstantAutoComplete extends AppCompatAutoCompleteTextView {
    private Drawable dRight;
    private Rect rBounds;

    public InstantAutoComplete(Context context) {
        super(context);
    }

    public InstantAutoComplete(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
    }

    public InstantAutoComplete(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    public void setCompoundDrawables(Drawable left, Drawable top,
                                     Drawable right, Drawable bottom)
    {
        if(right !=null)
        {
            dRight = right;
        }
        super.setCompoundDrawables(left, top, right, bottom);
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!this.isEnabled()){
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            performClick();
        }

        if(event.getAction() == MotionEvent.ACTION_UP && dRight!=null)
        {
            rBounds = dRight.getBounds();
            final int x = (int)event.getX();
            final int y = (int)event.getY();
            //System.out.println("x:/y: "+x+"/"+y);
            //System.out.println("bounds: "+bounds.left+"/"+bounds.right+"/"+bounds.top+"/"+bounds.bottom);
            //check to make sure the touch event was within the bounds of the drawable
            if(x>=(this.getRight()-rBounds.width()) && x<=(this.getRight()-this.getPaddingRight())
                    && y>=this.getPaddingTop() && y<=(this.getHeight()-this.getPaddingBottom()))
            {
                //System.out.println("touch");
                this.setText("");
//                event.setAction(MotionEvent.ACTION_CANCEL);//use this to prevent the keyboard from coming up
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        if(!this.isEnabled()){
            return false;
        }
        if (getFilter() != null && !isPopupShowing()) {
            performFiltering(getText(), 0);
            showDropDown();
        }
        return super.performClick();
    }
}