package com.fullarmor.hapi;

/*****************************************************************
 <copyright file="FileListAdapter.java" company="FullArmor Corp.">
 Copyright (c) 2015 FullArmor Corporation. All rights reserved.
 </copyright>
 *****************************************************************/

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.fullarmor.hapi.models.FileData;
import com.fullarmor.hapi.models.FileSystemEntry;

/**
 * Adapter used to display a list of local files on the android device
 */
public class FileListAdapter extends BaseAdapter {

    // list of files to display
    private final ArrayList<FileData> mFileDataArray;

    // activity context
    private final Context mContext;

    // constructor - initialize local vars
    public FileListAdapter(Context context, List<FileData> aFileDataArray) {
        mFileDataArray = (ArrayList<FileData>) aFileDataArray;
        mContext = context;
    }

    // gets the number of files in the list
    @Override
    public int getCount() {
        return mFileDataArray.size();
    }

    // gets the FileData object for the specified file
    @Override
    public Object getItem(int position) {
        return mFileDataArray.get(position);
    }

    // returns the id for the specified position
    @Override
    public long getItemId(int position) {
        return position;
    }

    // gets the view object for the specified row. This allows us to customized the image used for files/folders
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FileData item = mFileDataArray.get(position);
        FileNameAndImage tempView = new FileNameAndImage(mContext);
        tempView.setText(item.getFileName());
        int imgRes = -1;
        // set the image to use for this item
        switch (item.getFileType()) {
            // parent folder
            case FileData.PARENT: {
                imgRes = R.drawable.up_folder;
                break;
            }
            // directory
            case FileSystemEntry.ENTRYTYPE_FOLDER: {
                imgRes = R.drawable.iconfolder_96p;
                break;
            }
            // file
            case FileSystemEntry.ENTRYTYPE_FILE: {
                imgRes = R.drawable.iconfile_168p;
                break;
            }
        }
        tempView.setImageResource(imgRes);
        return tempView;
    }
}
