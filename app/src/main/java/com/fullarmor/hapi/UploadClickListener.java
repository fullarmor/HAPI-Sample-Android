package com.fullarmor.hapi;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;
import java.io.File;

/*****************************************************************
 <copyright file="UploadClickListener.java" company="FullArmor Corp.">
 Copyright (c) 2015 FullArmor Corporation. All rights reserved.
 </copyright>
 *****************************************************************/
public class UploadClickListener implements View.OnClickListener {

    /** FileSelector for this listener */
    private final FileSelector mFileSelector;

    private final Context mContext;

   // Constructor - initialize members
    public UploadClickListener(final FileSelector fileSelector, final Context context) {
         mFileSelector = fileSelector;
        mContext = context;
    }

    @Override
    public void onClick(final View view) {
        final String text = mFileSelector.getSelectedFileName();
        if (checkFileName(text)) {
            final String filePath = mFileSelector.getCurrentFolder().getAbsolutePath() + File.separator + text;
            final File file = new File(filePath);
            int messageText = 0;
            // Check file access rights.

            if (!file.exists()) {
                messageText = R.string.missingFile;
            } else if (!file.canRead()) {
                messageText = R.string.accessDenied;
            }
            if (messageText != 0) {
                // Access denied.
                final Toast t = Toast.makeText(mContext, messageText, Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
            } else {
                // Access granted.
                mFileSelector.mOnHandleFileListener.handleFile(filePath);
                mFileSelector.dismiss();
            }
        }
    }

    /**
     * Validates file name (i.e. checks for empty file)
     *
     * @return True, if file name is valid
     */
    boolean checkFileName(String text) {
        if (text.length() == 0) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.information);
            builder.setMessage(R.string.fileNameFirstMessage);
            builder.setNeutralButton(R.string.okButtonText, null);
            builder.show();
            return false;
        }
        return true;
    }
}