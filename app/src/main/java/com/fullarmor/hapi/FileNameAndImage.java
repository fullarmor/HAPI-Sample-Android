package com.fullarmor.hapi;


import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/*****************************************************************
 <copyright file="FileNameAndImage.java" company="FullArmor Corp.">
 Copyright (c) 2015 FullArmor Corporation. All rights reserved.
 </copyright>
 *****************************************************************/

    // This class is used by FileListAdaptor to display
    // info (name and icon) for a local android file or folder.

public class FileNameAndImage extends LinearLayout {

    // displays the image associated with file type`
    private ImageView mImage;
    // displays the file name
    private TextView mText;

    public FileNameAndImage(Context context) {
        super(context);
        setOrientation(HORIZONTAL);
        mImage = new ImageView(context);
        mText = new TextView(context);

        LayoutParams lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        lp.weight = 1;
        addView(mImage, lp);
        lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 3);
        addView(mText, lp);
    }


    public CharSequence getText() {
        return mText.getText();
    }


    public void setImageResource(int resId) {
        if (resId == -1) {
            // no image
            mImage.setVisibility(View.GONE);
            return;
        }
        mImage.setImageResource(resId);
    }


    public void setText(String aText) {
        mText.setText(aText);
    }

}
